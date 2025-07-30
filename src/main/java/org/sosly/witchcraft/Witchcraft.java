package org.sosly.witchcraft;

import com.mna.api.guidebook.RegisterGuidebooksEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sosly.witchcraft.blocks.BlockRegistry;
import org.sosly.witchcraft.blocks.EntityRegistry;
import org.sosly.witchcraft.commands.CommandRegistry;
import org.sosly.witchcraft.guis.ContainerRegistry;
import org.sosly.witchcraft.guis.ScreenRegistry;
import org.sosly.witchcraft.effects.EffectRegistry;
import org.sosly.witchcraft.enchantments.EnchantmentRegistry;
import org.sosly.witchcraft.items.ItemRegistry;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Witchcraft.MOD_ID)
public class Witchcraft {
    public static final String MOD_ID = "mnaw";
    public static final Logger LOGGER = LogManager.getLogger(Witchcraft.class);

    public Witchcraft() {
        IEventBus modbus = FMLJavaModLoadingContext.get().getModEventBus();

        BlockRegistry.BLOCKS.register(modbus);
        ContainerRegistry.CONTAINERS.register(modbus);
        EffectRegistry.EFFECTS.register(modbus);
        EnchantmentRegistry.ENCHANTMENTS.register(modbus);
        EntityRegistry.BLOCK_ENTITIES.register(modbus);
        ItemRegistry.ITEMS.register(modbus);

        MinecraftForge.EVENT_BUS.register(CommandRegistry.class);
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        if (FMLEnvironment.dist.isClient()) {
            modbus.register(ScreenRegistry.class);
        }
    }

    @SubscribeEvent
    public void onRegisterGuidebooks(RegisterGuidebooksEvent event) {
        event.getRegistry().addGuidebookPath(new ResourceLocation(MOD_ID, "guide"));
        LOGGER.info("guide registered");
    }
}
