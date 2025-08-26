package org.sosly.witchcraft.api.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public interface IFlyingBroomRenderer {
    void renderHandle(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
    void renderConnectors(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
    void renderTintedRibbon(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
    void renderTintedBrush(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay);
    
    ResourceLocation getHandleWood();
    int getRibbonColor();
    int getBrushTier();
}