package org.sosly.witchcraft.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.entities.tools.FlyingBroom;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityTypeRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Witchcraft.MOD_ID);

    public static final RegistryObject<EntityType<FlyingBroom>> FLYING_BROOM = ENTITY_TYPES.register("flying_broom",
            () -> EntityType.Builder.<FlyingBroom>of(FlyingBroom::new, MobCategory.MISC)
                    .sized(0.5F, 0.25F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .setShouldReceiveVelocityUpdates(true)
                    .build("flying_broom"));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(FLYING_BROOM.get(), FlyingBroom.createAttributes().build());
    }
}
