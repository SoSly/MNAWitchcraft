package org.sosly.witchcraft.blocks;

import com.mna.api.blocks.interfaces.ICutoutBlock;
import com.mna.api.blocks.interfaces.ITranslucentBlock;
import com.mna.api.items.MACreativeTabs;
import com.mna.api.items.TieredBlockItem;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.blocks.alchemy.WaterWitchsCauldronBlock;
import org.sosly.witchcraft.blocks.alchemy.EmptyWitchsCauldronBlock;
import org.sosly.witchcraft.blocks.alchemy.MoonlightWitchsCauldronBlock;
import org.sosly.witchcraft.blocks.alchemy.CondensedMoonlightCauldronBlock;
import org.sosly.witchcraft.blocks.sympathy.BoundPoppetBlock;
import org.sosly.witchcraft.blocks.sympathy.PoppetBlock;
import org.sosly.witchcraft.items.sympathy.BoundPoppetItem;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Witchcraft.MOD_ID);
    public static final RegistryObject<BoundPoppetBlock> BOUND_POPPET = BLOCKS.register("bound_poppet", BoundPoppetBlock::new);
    public static final RegistryObject<PoppetBlock> POPPET = BLOCKS.register("poppet", PoppetBlock::new);
    public static final RegistryObject<CondensedMoonlightCauldronBlock> CONDENSED_MOONLIGHT_CAULDRON = BLOCKS.register("condensed_moonlight_cauldron", 
            () -> new CondensedMoonlightCauldronBlock(Block.Properties.copy(net.minecraft.world.level.block.Blocks.CAULDRON)));
    public static final RegistryObject<WaterWitchsCauldronBlock> WITCHS_CAULDRON = BLOCKS.register("witchs_cauldron", WaterWitchsCauldronBlock::new);
    public static final RegistryObject<MoonlightWitchsCauldronBlock> MOONLIGHT_WITCHS_CAULDRON = BLOCKS.register("moonlight_witchs_cauldron", MoonlightWitchsCauldronBlock::new);
    public static final RegistryObject<EmptyWitchsCauldronBlock> EMPTY_WITCHS_CAULDRON = BLOCKS.register("empty_witchs_cauldron", EmptyWitchsCauldronBlock::new);

    @SubscribeEvent
    public static void onRegisterItems(RegisterEvent event) {
        event.register(ForgeRegistries.ITEMS.getRegistryKey(), (helper) -> {
            Item.Properties properties = new Item.Properties();

            BoundPoppetItem boundPoppetItem = new BoundPoppetItem(BOUND_POPPET.get());
            helper.register(ForgeRegistries.BLOCKS.getKey(BOUND_POPPET.get()), boundPoppetItem);
            TieredBlockItem poppetItem = new TieredBlockItem(POPPET.get(), properties);
            helper.register(ForgeRegistries.BLOCKS.getKey(POPPET.get()), poppetItem);
            Item.Properties cauldronProperties = new Item.Properties();
            TieredBlockItem emptyCauldronItem = new TieredBlockItem(EMPTY_WITCHS_CAULDRON.get(), cauldronProperties);
            helper.register(ForgeRegistries.BLOCKS.getKey(EMPTY_WITCHS_CAULDRON.get()), emptyCauldronItem);
        });
    }

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(CONDENSED_MOONLIGHT_CAULDRON.get(), RenderType.translucent());
        
        BlockRegistry.BLOCKS.getEntries().stream().map(RegistryObject::get).forEach(block -> {
            if (block instanceof ICutoutBlock || block instanceof FlowerPotBlock) {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
                return;
            }
            if (block instanceof ITranslucentBlock) {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.translucent());
            }
        });
    }

    @SubscribeEvent
    public static void fillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == MACreativeTabs.GENERAL) {
            BLOCKS.getEntries().stream().map(RegistryObject::get).forEach((block) -> {
                ItemStack stack = new ItemStack(block);
                if (!stack.isEmpty() && stack.getCount() == 1) {
                    event.accept(stack);
                }
            });
        }
    }
}
