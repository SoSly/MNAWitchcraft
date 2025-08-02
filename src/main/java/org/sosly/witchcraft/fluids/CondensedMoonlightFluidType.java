package org.sosly.witchcraft.fluids;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class CondensedMoonlightFluidType extends FluidType {
    
    public CondensedMoonlightFluidType() {
        super(Properties.create()
                .density(1000)
                .viscosity(1000)
                .canSwim(true)
                .canDrown(true)
                .canPushEntity(true)
                .supportsBoating(true)
                .lightLevel(6));
    }
    
    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            @Override
            public int getTintColor() {
                return 0xAAC0D0E6;
            }
            
            @Override
            public @NotNull ResourceLocation getStillTexture() {
                return new ResourceLocation("minecraft:block/water_still");
            }
            
            @Override
            public @NotNull ResourceLocation getFlowingTexture() {
                return new ResourceLocation("minecraft:block/water_flow");
            }
            
            @Override
            public @Nullable ResourceLocation getOverlayTexture() {
                return new ResourceLocation("minecraft:block/water_overlay");
            }
            
            @Override
            public @NotNull Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.67f, 0.75f, 0.82f);
            }
            
            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
            }
        });
    }
}