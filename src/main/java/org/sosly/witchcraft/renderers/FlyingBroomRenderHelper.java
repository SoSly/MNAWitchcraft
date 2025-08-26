package org.sosly.witchcraft.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.Witchcraft;

public class FlyingBroomRenderHelper {
    private static final ResourceLocation HANDLE_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "item/flying_broom_handle");
    private static final ResourceLocation CONNECTORS_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "item/flying_broom_connectors");
    private static final ResourceLocation RIBBON_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "item/flying_broom_ribbon");
    private static final ResourceLocation BRUSH_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "item/flying_broom_brush");

    public static BakedModel getHandleModel() {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(HANDLE_MODEL);
        return model == Minecraft.getInstance().getModelManager().getMissingModel() ? null : model;
    }

    public static BakedModel getConnectorsModel() {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(CONNECTORS_MODEL);
        return model == Minecraft.getInstance().getModelManager().getMissingModel() ? null : model;
    }

    public static BakedModel getRibbonModel() {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(RIBBON_MODEL);
        return model == Minecraft.getInstance().getModelManager().getMissingModel() ? null : model;
    }

    public static BakedModel getBrushModel() {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(BRUSH_MODEL);
        return model == Minecraft.getInstance().getModelManager().getMissingModel() ? null : model;
    }

    public static ResourceLocation getWoodTexture(ResourceLocation woodType) {
        String location = switch (woodType.getPath()) {
            case "crimson" -> "textures/block/stripped_" + woodType.getPath() + "_stem.png";
            case "warped" -> "textures/block/stripped_" + woodType.getPath() + "_stem.png";
            case "bamboo" -> "textures/block/stripped_bamboo_block.png";
            default -> "textures/block/stripped_" + woodType.getPath() + "_log.png";
        };

        return new ResourceLocation(woodType.getNamespace(), location);
    }

    public static int getBrushColorForTier(int tier) {
        return switch (tier) {
            case 1 -> 0xE7C77B;
            case 2 -> 0xD4AF37;
            case 3 -> 0xC0C0C0;
            default -> 0xE7C77B;
        };
    }

    public static void renderBakedModel(BakedModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) {
            return;
        }
        
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        RandomSource random = RandomSource.create();
        random.setSeed(42);
        
        var quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        for (var quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
        }
    }

    public static void renderBakedModelWithWoodTexture(BakedModel model, ResourceLocation woodType, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) {
            return;
        }
        
        ResourceLocation woodTexture = getWoodTexture(woodType);
        WoodTexturedMultiBufferSource texturedBuffer = new WoodTexturedMultiBufferSource(buffer, woodTexture);
        
        RandomSource random = RandomSource.create();
        random.setSeed(42);
        
        var quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        RenderType renderType = RenderType.solid();
        VertexConsumer consumer = texturedBuffer.getBuffer(renderType);
        
        for (var quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
        }
    }

    public static void renderBakedModelWithTint(BakedModel model, int tintColor, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) {
            return;
        }
        
        TintedMultiBufferSource tintedBuffer = new TintedMultiBufferSource(buffer, tintColor);
        RandomSource random = RandomSource.create();
        random.setSeed(42);
        
        renderBakedModel(model, poseStack, tintedBuffer, packedLight, packedOverlay);
    }

    public static class WoodTexturedMultiBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final ResourceLocation woodTexture;

        public WoodTexturedMultiBufferSource(MultiBufferSource delegate, ResourceLocation woodTexture) {
            this.delegate = delegate;
            this.woodTexture = woodTexture;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            RenderType texturedRenderType = RenderType.entitySolid(woodTexture);
            return delegate.getBuffer(texturedRenderType);
        }
    }

    public static class TintedMultiBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final int tintColor;

        public TintedMultiBufferSource(MultiBufferSource delegate, int tintColor) {
            this.delegate = delegate;
            this.tintColor = tintColor;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            VertexConsumer consumer = delegate.getBuffer(renderType);
            return new TintedVertexConsumer(consumer, tintColor);
        }
    }

    public static class TintedVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final float red;
        private final float green;
        private final float blue;

        public TintedVertexConsumer(VertexConsumer delegate, int tintColor) {
            this.delegate = delegate;
            this.red = ((tintColor >> 16) & 0xFF) / 255.0F;
            this.green = ((tintColor >> 8) & 0xFF) / 255.0F;
            this.blue = (tintColor & 0xFF) / 255.0F;
        }

        @Override
        public @NotNull VertexConsumer vertex(double x, double y, double z) {
            return delegate.vertex(x, y, z);
        }

        @Override
        public @NotNull VertexConsumer color(int red, int green, int blue, int alpha) {
            float tintedRed = (red / 255.0F) * this.red;
            float tintedGreen = (green / 255.0F) * this.green;
            float tintedBlue = (blue / 255.0F) * this.blue;
            
            int finalRed = (int)(tintedRed * 255);
            int finalGreen = (int)(tintedGreen * 255);
            int finalBlue = (int)(tintedBlue * 255);
            
            return delegate.color(finalRed, finalGreen, finalBlue, alpha);
        }

        @Override
        public @NotNull VertexConsumer uv(float u, float v) {
            return delegate.uv(u, v);
        }

        @Override
        public @NotNull VertexConsumer overlayCoords(int u, int v) {
            return delegate.overlayCoords(u, v);
        }

        @Override
        public @NotNull VertexConsumer uv2(int u, int v) {
            return delegate.uv2(u, v);
        }

        @Override
        public @NotNull VertexConsumer normal(float x, float y, float z) {
            return delegate.normal(x, y, z);
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int alpha) {
            delegate.defaultColor(red, green, blue, alpha);
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}