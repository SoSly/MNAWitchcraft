package org.sosly.witchcraft;

import com.mna.api.guidebook.RegisterGuidebooksEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sosly.witchcraft.blocks.BlockRegistry;
import org.sosly.witchcraft.blocks.EntityRegistry;
import org.sosly.witchcraft.commands.CommandRegistry;
import org.sosly.witchcraft.effects.EffectRegistry;
import org.sosly.witchcraft.enchantments.EnchantmentRegistry;
import org.sosly.witchcraft.entities.EntityTypeRegistry;
import org.sosly.witchcraft.fluids.FluidRegistry;
import org.sosly.witchcraft.guis.ContainerRegistry;
import org.sosly.witchcraft.guis.ScreenRegistry;
import org.sosly.witchcraft.items.ItemRegistry;
import org.sosly.witchcraft.recipes.RecipeSerializerRegistry;
import org.sosly.witchcraft.cantrips.Cantrips;

@Mod(Witchcraft.MOD_ID)
public class Witchcraft {
    public static final String MOD_ID = "mnaw";
    public static final Logger LOGGER = LogManager.getLogger();

    public Witchcraft() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();

        BlockRegistry.BLOCKS.register(modbus);
        ContainerRegistry.CONTAINERS.register(modbus);
        EffectRegistry.EFFECTS.register(modbus);
        EnchantmentRegistry.ENCHANTMENTS.register(modbus);
        EntityRegistry.BLOCK_ENTITIES.register(modbus);
        EntityTypeRegistry.ENTITY_TYPES.register(modbus);
        FluidRegistry.FLUIDS.register(modbus);
        FluidRegistry.FLUID_TYPES.register(modbus);
        ItemRegistry.ITEMS.register(modbus);
        RecipeSerializerRegistry.SERIALIZERS.register(modbus);

        MinecraftForge.EVENT_BUS.register(CommandRegistry.class);
        MinecraftForge.EVENT_BUS.register(this);
        modbus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        if (FMLEnvironment.dist.isClient()) {
            modbus.register(ScreenRegistry.class);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Cantrips.registerCantrips();
        });
    }

    @SubscribeEvent
    public void onRegisterGuidebooks(RegisterGuidebooksEvent event) {
        event.getRegistry().addGuidebookPath(new ResourceLocation(MOD_ID, "guide"));
        LOGGER.info("guide registered");
    }
}
