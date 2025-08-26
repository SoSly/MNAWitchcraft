package org.sosly.witchcraft.renderers.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.api.renderers.IFlyingBroomRenderer;
import org.sosly.witchcraft.data.FlyingBroomData;
import org.sosly.witchcraft.entities.tools.FlyingBroom;
import org.sosly.witchcraft.renderers.FlyingBroomRenderHelper;

public class FlyingBroomEntityRenderer extends EntityRenderer<FlyingBroom> implements IFlyingBroomRenderer {
    private FlyingBroom currentEntity;

    public FlyingBroomEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FlyingBroom entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - entityYaw));
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(2.0F, 2.0F, 2.0F);

        renderBroomWithTintedRibbon(entity, poseStack, buffer, packedLight);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderBroomWithTintedRibbon(FlyingBroom entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.currentEntity = entity;
        
        poseStack.pushPose();
        poseStack.translate(-0.25D, -0.125D, -0.25D);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        
        renderHandle(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        renderConnectors(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        renderTintedRibbon(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        renderTintedBrush(poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        
        poseStack.popPose();
    }
    
    @Override
    public void renderHandle(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BakedModel handleModel = FlyingBroomRenderHelper.getHandleModel();
        FlyingBroomRenderHelper.renderBakedModelWithWoodTexture(handleModel, getHandleWood(), poseStack, buffer, packedLight, packedOverlay);
    }
    
    
    @Override
    public void renderConnectors(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BakedModel connectorsModel = FlyingBroomRenderHelper.getConnectorsModel();
        FlyingBroomRenderHelper.renderBakedModel(connectorsModel, poseStack, buffer, packedLight, packedOverlay);
    }
    

    @Override
    public void renderTintedRibbon(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BakedModel ribbonModel = FlyingBroomRenderHelper.getRibbonModel();
        FlyingBroomRenderHelper.renderBakedModelWithTint(ribbonModel, getRibbonColor(), poseStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void renderTintedBrush(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BakedModel brushModel = FlyingBroomRenderHelper.getBrushModel();
        int brushColor = FlyingBroomRenderHelper.getBrushColorForTier(getBrushTier());
        FlyingBroomRenderHelper.renderBakedModelWithTint(brushModel, brushColor, poseStack, buffer, packedLight, packedOverlay);
    }


    @Override
    public ResourceLocation getHandleWood() {
        return currentEntity != null ? currentEntity.getHandleWood() : FlyingBroomData.DEFAULT_HANDLE_WOOD;
    }

    @Override
    public int getRibbonColor() {
        return currentEntity != null ? currentEntity.getRibbonColor() : FlyingBroomData.DEFAULT_RIBBON_COLOR;
    }

    @Override
    public int getBrushTier() {
        return currentEntity != null ? currentEntity.getBrushTier() : FlyingBroomData.DEFAULT_BRUSH_TIER;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FlyingBroom entity) {
        return new ResourceLocation("minecraft", "textures/item/stick.png");
    }
}