package org.sosly.witchcraft.items.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.Witchcraft;

public class FlyingBroomItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation HANDLE_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "item/flying_broom_handle");
    private static final ResourceLocation RIBBON_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "item/flying_broom_ribbon");
    private static final ResourceLocation BRUSH_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "item/flying_broom_brush");
    private static final ResourceLocation CONNECTORS_MODEL = new ResourceLocation(Witchcraft.MOD_ID, "item/flying_broom_connectors");
    
    private BakedModel handleModel;
    private BakedModel ribbonModel;
    private BakedModel brushModel;
    private BakedModel connectorsModel;

    public FlyingBroomItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, 
                           MultiBufferSource buffer, int packedLight, int packedOverlay) {
        loadModelsIfNeeded();
        
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        
        renderHandle(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        renderConnectors(stack, poseStack, buffer, packedLight, packedOverlay);
        renderTintedRibbon(stack, poseStack, buffer, packedLight, packedOverlay);
        renderTintedBrush(stack, poseStack, buffer, packedLight, packedOverlay);
        
        poseStack.popPose();
        
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }
    
    private void loadModelsIfNeeded() {
        if (handleModel == null) {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(HANDLE_MODEL);
            if (model != Minecraft.getInstance().getModelManager().getMissingModel()) {
                handleModel = model;
            }
        }
        if (ribbonModel == null) {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(RIBBON_MODEL);
            if (model != Minecraft.getInstance().getModelManager().getMissingModel()) {
                ribbonModel = model;
            }
        }
        if (brushModel == null) {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(BRUSH_MODEL);
            if (model != Minecraft.getInstance().getModelManager().getMissingModel()) {
                brushModel = model;
            }
        }
        if (connectorsModel == null) {
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(CONNECTORS_MODEL);
            if (model != Minecraft.getInstance().getModelManager().getMissingModel()) {
                connectorsModel = model;
            }
        }
    }
    
    private void renderHandle(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, 
                            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (handleModel == null) {
            return;
        }
        
        BakedModel resolvedHandleModel = handleModel.getOverrides().resolve(handleModel, stack, null, null, 0);
        if (resolvedHandleModel == null) {
            resolvedHandleModel = handleModel;
        }
        
        BakedModel transformedHandleModel = ForgeHooksClient.handleCameraTransforms(poseStack, resolvedHandleModel, displayContext, false);
        renderHandleWithWoodTexture(transformedHandleModel, stack, poseStack, buffer, packedLight, packedOverlay);
    }
    
    private void renderHandleWithWoodTexture(BakedModel model, ItemStack stack, PoseStack poseStack, 
                                           MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (model == null) {
            return;
        }
        
        poseStack.pushPose();
        
        ResourceLocation woodType = getHandleWood(stack);
        ResourceLocation woodTexture = getWoodTexture(woodType);
        WoodTexturedMultiBufferSource texturedBuffer = new WoodTexturedMultiBufferSource(buffer, woodTexture);

        VertexConsumer consumer = texturedBuffer.getBuffer(RenderType.solid());
        RandomSource random = RandomSource.create();
        random.setSeed(42);
        
        var quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        for (var quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
        }
        
        poseStack.popPose();
    }
    
    private void renderConnectors(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, 
                                int packedLight, int packedOverlay) {
        if (connectorsModel == null) {
            return;
        }
        
        poseStack.pushPose();
        poseStack.translate(0, 0, 0); // Placeholder transform for you to adjust
        
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        RandomSource random = RandomSource.create();
        random.setSeed(42);
        
        var quads = connectorsModel.getQuads(null, null, random, ModelData.EMPTY, null);
        for (var quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
        }
        
        poseStack.popPose();
    }
    
    private void renderTintedRibbon(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, 
                                  int packedLight, int packedOverlay) {
        if (ribbonModel == null) {
            return;
        }
        
        int ribbonColor = getRibbonColor(stack);
        TintedMultiBufferSource tintedBuffer = new TintedMultiBufferSource(buffer, ribbonColor);
        
        poseStack.pushPose();
        renderBakedModel(ribbonModel, poseStack, tintedBuffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
    
    private void renderTintedBrush(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, 
                                 int packedLight, int packedOverlay) {
        if (brushModel == null) {
            return;
        }
        
        int brushColor = getBrushColor(stack);
        TintedMultiBufferSource tintedBuffer = new TintedMultiBufferSource(buffer, brushColor);
        
        poseStack.pushPose();
        renderBakedModel(brushModel, poseStack, tintedBuffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
    
    private void renderBakedModel(BakedModel model, PoseStack poseStack, MultiBufferSource buffer, 
                                int packedLight, int packedOverlay) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.solid());
        RandomSource random = RandomSource.create();
        random.setSeed(42);
        
        var quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
        for (var quad : quads) {
            consumer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
        }
    }
    
    private int getRibbonColor(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("RibbonColor")) {
            return nbt.getInt("RibbonColor");
        }
        return 16383998;
    }
    
    private int getBrushColor(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("BrushTier")) {
            int tier = nbt.getInt("BrushTier");
            return switch (tier) {
                case 1 -> 0xE7C77B;
                case 2 -> 0xD4AF37;
                case 3 -> 0xC0C0C0;
                default -> 0xE7C77B;
            };
        }
        return 0xE7C77B;
    }
    
    private ResourceLocation getHandleWood(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null && nbt.contains("HandleWood")) {
            return new ResourceLocation(nbt.getString("HandleWood"));
        }
        return new ResourceLocation("minecraft:oak");
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

    private static class TintedVertexConsumer implements VertexConsumer {
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