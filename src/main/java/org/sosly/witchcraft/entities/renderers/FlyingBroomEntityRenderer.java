package org.sosly.witchcraft.entities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.entities.FlyingBroomEntity;
import org.sosly.witchcraft.items.ItemRegistry;

public class FlyingBroomEntityRenderer extends EntityRenderer<FlyingBroomEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Witchcraft.MOD_ID, "textures/entity/flying_broom.png");

    public FlyingBroomEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FlyingBroomEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        poseStack.translate(0.5D, 0.5D, -0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - entityYaw));
        poseStack.scale(2.0F, 2.0F, 2.0F);

        ItemStack broomStack = new ItemStack(ItemRegistry.FLYING_BROOM.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(
                broomStack, 
                ItemDisplayContext.GROUND, 
                packedLight, 
                OverlayTexture.NO_OVERLAY, 
                poseStack, 
                buffer, 
                entity.level(), 
                entity.getId()
        );
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FlyingBroomEntity entity) {
        return TEXTURE;
    }
}