package org.sosly.witchcraft.renderers.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
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
import org.sosly.witchcraft.data.FlyingBroomData;
import org.sosly.witchcraft.renderers.FlyingBroomRenderHelper;

public class FlyingBroomItemRenderer extends BlockEntityWithoutLevelRenderer implements IFlyingBroomRenderer {
    private static FlyingBroomItemRenderer INSTANCE;
    
    private ItemStack currentStack;
    private ItemDisplayContext currentDisplayContext;

    private FlyingBroomItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }
    
    public static FlyingBroomItemRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FlyingBroomItemRenderer(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
            );
        }
        return INSTANCE;
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
            return FlyingBroomData.DEFAULT_HANDLE_WOOD;
        }
        FlyingBroomData data = FlyingBroomData.fromItemStack(currentStack);
        return data.getHandleWood();
    }

    @Override
    public int getRibbonColor() {
        if (currentStack == null) {
            return FlyingBroomData.DEFAULT_RIBBON_COLOR;
        }
        FlyingBroomData data = FlyingBroomData.fromItemStack(currentStack);
        return data.getRibbonColor();
    }

    @Override
    public int getBrushTier() {
        if (currentStack == null) {
            return FlyingBroomData.DEFAULT_BRUSH_TIER;
        }
        FlyingBroomData data = FlyingBroomData.fromItemStack(currentStack);
        return data.getBrushTier();
    }
    
}