package org.sosly.witchcraft.renderers.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.sosly.witchcraft.api.renderers.IFlyingBroomRenderer;
import org.sosly.witchcraft.renderers.FlyingBroomRenderHelper;

public class FlyingBroomItemRenderer extends BlockEntityWithoutLevelRenderer implements IFlyingBroomRenderer {
    private ItemStack currentStack;
    private ItemDisplayContext currentDisplayContext;

    public FlyingBroomItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, 
                           MultiBufferSource buffer, int packedLight, int packedOverlay) {
        this.currentStack = stack;
        this.currentDisplayContext = displayContext;
        
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        
        renderHandle(poseStack, buffer, packedLight, packedOverlay);
        renderConnectors(poseStack, buffer, packedLight, packedOverlay);
        renderTintedRibbon(poseStack, buffer, packedLight, packedOverlay);
        renderTintedBrush(poseStack, buffer, packedLight, packedOverlay);
        
        poseStack.popPose();
        
        super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
    }
    
    
    @Override
    public void renderHandle(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BakedModel handleModel = FlyingBroomRenderHelper.getHandleModel();
        if (handleModel == null) {
            return;
        }
        
        BakedModel transformedHandleModel = ForgeHooksClient.handleCameraTransforms(poseStack, handleModel, currentDisplayContext, false);
        FlyingBroomRenderHelper.renderBakedModelWithWoodTexture(transformedHandleModel, getHandleWood(), poseStack, buffer, packedLight, packedOverlay);
    }
    
    
    @Override
    public void renderConnectors(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 0);
        
        BakedModel connectorsModel = FlyingBroomRenderHelper.getConnectorsModel();
        FlyingBroomRenderHelper.renderBakedModel(connectorsModel, poseStack, buffer, packedLight, packedOverlay);
        
        poseStack.popPose();
    }
    
    @Override
    public void renderTintedRibbon(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        BakedModel ribbonModel = FlyingBroomRenderHelper.getRibbonModel();
        FlyingBroomRenderHelper.renderBakedModelWithTint(ribbonModel, getRibbonColor(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }
    
    @Override
    public void renderTintedBrush(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        BakedModel brushModel = FlyingBroomRenderHelper.getBrushModel();
        int brushColor = FlyingBroomRenderHelper.getBrushColorForTier(getBrushTier());
        FlyingBroomRenderHelper.renderBakedModelWithTint(brushModel, brushColor, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getHandleWood() {
        if (currentStack == null) {
            return new ResourceLocation("minecraft:oak");
        }
        CompoundTag nbt = currentStack.getTag();
        if (nbt != null && nbt.contains("HandleWood")) {
            return new ResourceLocation(nbt.getString("HandleWood"));
        }
        return new ResourceLocation("minecraft:oak");
    }

    @Override
    public int getRibbonColor() {
        if (currentStack == null) {
            return 16383998;
        }
        CompoundTag nbt = currentStack.getTag();
        if (nbt != null && nbt.contains("RibbonColor")) {
            return nbt.getInt("RibbonColor");
        }
        return 16383998;
    }

    @Override
    public int getBrushTier() {
        if (currentStack == null) {
            return 1;
        }
        CompoundTag nbt = currentStack.getTag();
        if (nbt != null && nbt.contains("BrushTier")) {
            return nbt.getInt("BrushTier");
        }
        return 1;
    }
    
}