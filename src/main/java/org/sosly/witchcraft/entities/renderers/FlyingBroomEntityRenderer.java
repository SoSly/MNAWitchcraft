package org.sosly.witchcraft.entities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.entities.FlyingBroomEntity;
import org.sosly.witchcraft.items.ItemRegistry;

public class FlyingBroomEntityRenderer extends EntityRenderer<FlyingBroomEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Witchcraft.MOD_ID, "textures/entity/flying_broom.png");
    private static final ResourceLocation HANDLE_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "entity/flying_broom_handle");
    private static final ResourceLocation CONNECTORS_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "entity/flying_broom_connectors");
    
    private BakedModel handleModel;
    private BakedModel connectorsModel;

    public FlyingBroomEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FlyingBroomEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - entityYaw));
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(2.0F, 2.0F, 2.0F);

        renderBroomWithTintedRibbon(entity, poseStack, buffer, packedLight);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderBroomWithTintedRibbon(FlyingBroomEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        loadModelsIfNeeded();
        
        poseStack.pushPose();
        poseStack.translate(-0.25D, -0.125D, -0.25D);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        
        renderHandle(entity, poseStack, buffer, packedLight);
        renderConnectors(entity, poseStack, buffer, packedLight);
        renderTintedRibbon(entity, poseStack, buffer, packedLight);
        renderTintedBrush(entity, poseStack, buffer, packedLight);
        
        poseStack.popPose();
    }
    
    private void renderHandle(FlyingBroomEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (handleModel == null) {
            return;
        }
        
        renderBakedModelWithWoodTexture(handleModel, entity.getHandleWood(), poseStack, buffer, packedLight);
    }
    
    private void loadModelsIfNeeded() {
        if (handleModel == null) {
            handleModel = Minecraft.getInstance().getModelManager().getModel(HANDLE_MODEL);
        }
        if (connectorsModel == null) {
            connectorsModel = Minecraft.getInstance().getModelManager().getModel(CONNECTORS_MODEL);
        }
    }
    
    private void renderConnectors(FlyingBroomEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (connectorsModel == null) {
            return;
        }
        
        RandomSource random = RandomSource.create();
        random.setSeed(42);
        
        var quads = connectorsModel.getQuads(null, null, random, ModelData.EMPTY, null);
        RenderType renderType = RenderType.solid();
        VertexConsumer consumer = buffer.getBuffer(renderType);
        
        for (var quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
    
    private void renderBakedModel(BakedModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        RenderType renderType = RenderType.solid();
        VertexConsumer consumer = buffer.getBuffer(renderType);
        RandomSource random = RandomSource.create();
        random.setSeed(42);
        
        var quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        for (var quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }

    private void renderTintedRibbon(FlyingBroomEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BakedModel ribbonModel = getRibbonModel();
        if (ribbonModel == null) {
            return;
        }
        
        TintedMultiBufferSource tintedBuffer = new TintedMultiBufferSource(buffer, entity.getRibbonColor());
        RandomSource random = RandomSource.create();
        renderBakedModel(ribbonModel, poseStack, tintedBuffer, packedLight, random);
    }

    private void renderTintedBrush(FlyingBroomEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BakedModel brushModel = getBrushModel();
        if (brushModel == null) {
            return;
        }
        
        int brushColor = getBrushColorForTier(entity.getBrushTier());
        TintedMultiBufferSource tintedBuffer = new TintedMultiBufferSource(buffer, brushColor);
        RandomSource random = RandomSource.create();
        renderBakedModel(brushModel, poseStack, tintedBuffer, packedLight, random);
    }

    private void renderBakedModel(BakedModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight, RandomSource random) {
        var quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        if (quads.isEmpty()) {
            return;
        }
        
        RenderType renderType = RenderType.solid();
        VertexConsumer consumer = buffer.getBuffer(renderType);
        
        for (var quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }

    private BakedModel getRibbonModel() {
        ResourceLocation modelLocation = new ResourceLocation(Witchcraft.MOD_ID, "entity/flying_broom_ribbon");
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
                return null;
        }
        
        
        return model;
    }

    private BakedModel getBrushModel() {
        ResourceLocation modelLocation = new ResourceLocation(Witchcraft.MOD_ID, "entity/flying_broom_brush");
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        
        if (model == Minecraft.getInstance().getModelManager().getMissingModel()) {
            return null;
        }
        
        return model;
    }
    
    private int getBrushColorForTier(int tier) {
        return switch (tier) {
            case 1 -> 0xE7C77B;
            case 2 -> 0xD4AF37;
            case 3 -> 0xC0C0C0;
            default -> 0xE7C77B;
        };
    }
    
    private void renderBakedModelWithWoodTexture(BakedModel model, ResourceLocation woodType, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        ResourceLocation woodTexture = getWoodTexture(woodType);
        WoodTexturedMultiBufferSource texturedBuffer = new WoodTexturedMultiBufferSource(buffer, woodTexture);
        
        RandomSource random = RandomSource.create();
        random.setSeed(42);
        
        var quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        RenderType renderType = RenderType.solid();
        VertexConsumer consumer = texturedBuffer.getBuffer(renderType);
        
        for (var quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
    
    private ResourceLocation getWoodTexture(ResourceLocation woodType) {
        if (woodType.getPath().equals("crimson") || woodType.getPath().equals("warped")) {
            return new ResourceLocation(woodType.getNamespace(), "textures/block/stripped_" + woodType.getPath() + "_stem.png");
        } else if (woodType.getPath().equals("bamboo")) {
            return new ResourceLocation(woodType.getNamespace(), "textures/block/stripped_bamboo_block.png");
        } else {
            return new ResourceLocation(woodType.getNamespace(), "textures/block/stripped_" + woodType.getPath() + "_log.png");
        }
    }

    private static class WoodTexturedMultiBufferSource implements MultiBufferSource {
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

    private static class TintedMultiBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final int ribbonTint;

        public TintedMultiBufferSource(MultiBufferSource delegate, int ribbonTint) {
            this.delegate = delegate;
            this.ribbonTint = ribbonTint;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            VertexConsumer consumer = delegate.getBuffer(renderType);
            return new TintedVertexConsumer(consumer, ribbonTint);
        }
    }

    private static class TintedVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final int tintColor;
        private final float red;
        private final float green;
        private final float blue;

        public TintedVertexConsumer(VertexConsumer delegate, int tintColor) {
            this.delegate = delegate;
            this.tintColor = tintColor;
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

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FlyingBroomEntity entity) {
        return TEXTURE;
    }
}