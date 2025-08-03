package org.sosly.witchcraft.blocks.alchemy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class WitchsCauldronRenderer implements BlockEntityRenderer<WitchsCauldronBlockEntity> {
    
    private static final ResourceLocation WATER_TEXTURE = new ResourceLocation("textures/block/water_still.png");
    
    public WitchsCauldronRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    @Override
    public void render(WitchsCauldronBlockEntity blockEntity, float partialTicks, PoseStack poseStack, 
            MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        
        if (blockEntity.isEmpty()) {
            return;
        }
        
        int level = blockEntity.getFluidLevel();
        if (level <= 0) {
            return;
        }
        
        ResourceLocation texture = WATER_TEXTURE;
        
        VertexConsumer builder = bufferSource.getBuffer(RenderType.beaconBeam(texture, true));
        Matrix4f mat = poseStack.last().pose();
        
        float yPos = 0.375f + 0.1875f * level;
        
        int frames = 16;
        float frameSize = 1f / frames;
        long frame = (blockEntity.getLevel().getGameTime() / 3) % frames;
        float min_u = 0;
        float max_u = 1;
        float min_v = frameSize * frame;
        float max_v = frameSize * (frame + 1);
        
        long color = blockEntity.getPotionColor();
        float[] rgb = colorFromLong(color);
        
        float minX = 2f / 16f;
        float maxX = 14f / 16f;
        float minZ = 2f / 16f;
        float maxZ = 14f / 16f;
        
        addVertex(builder, mat, maxX, yPos, minZ, max_u, min_v, rgb, combinedLight);
        addVertex(builder, mat, minX, yPos, minZ, min_u, min_v, rgb, combinedLight);
        addVertex(builder, mat, minX, yPos, maxZ, min_u, max_v, rgb, combinedLight);
        addVertex(builder, mat, maxX, yPos, maxZ, max_u, max_v, rgb, combinedLight);
    }
    
    private float[] colorFromLong(long color) {
        return new float[] {
            ((color >> 16) & 0xFF) / 255.0f,
            ((color >> 8) & 0xFF) / 255.0f,
            (color & 0xFF) / 255.0f
        };
    }
    
    private static void addVertex(VertexConsumer builder, Matrix4f pos, float x, float y, float z, 
            float u, float v, float[] rgb, int combinedLight) {
        builder
            .vertex(pos, x, y, z)
            .color(rgb[0], rgb[1], rgb[2], 1f)
            .uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(combinedLight)
            .normal(0, 1, 0)
            .endVertex();
    }
}