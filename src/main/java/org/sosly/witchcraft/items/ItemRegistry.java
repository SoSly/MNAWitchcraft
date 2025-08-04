package org.sosly.witchcraft.items;

import com.mna.api.items.MACreativeTabs;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.items.alchemy.PotionAmuletItem;
import org.sosly.witchcraft.items.alchemy.PotionPouchItem;
import org.sosly.witchcraft.items.alchemy.WitchEyeItem;
import org.sosly.witchcraft.items.armor.MoonthreadArmorItem;
import org.sosly.witchcraft.items.armor.MoonthreadArmorMaterial;
import org.sosly.witchcraft.items.sympathy.AntiSympathyCharmItem;
import org.sosly.witchcraft.items.sympathy.BloodyNeedleItem;
import org.sosly.witchcraft.items.grimoire.CovenGrimoire;
import org.sosly.witchcraft.items.fluids.CondensedMoonlightBucketItem;
import org.sosly.witchcraft.items.fluids.CondensedMoonlightBottleItem;
import org.sosly.witchcraft.fluids.FluidRegistry;
import org.sosly.witchcraft.items.FlyingBroomItem;

@Mod.EventBusSubscriber(modid= Witchcraft.MOD_ID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Witchcraft.MOD_ID);
    public static final RegistryObject<Item> ANTISYMPATHY_CHARM = ITEMS.register("antisympathy_charm", AntiSympathyCharmItem::new);
    public static final RegistryObject<Item> BLOODY_NEEDLE = ITEMS.register("bloody_needle", BloodyNeedleItem::new);
    public static final RegistryObject<Item> MOONTHREAD_ARMOR_CHEST = ITEMS.register("moonthread_armor_chest", () -> new MoonthreadArmorItem(
            MoonthreadArmorMaterial.MOONTHREAD, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> MOONTHREAD_ARMOR_LEGGINGS = ITEMS.register("moonthread_armor_leggings", () -> new MoonthreadArmorItem(
            MoonthreadArmorMaterial.MOONTHREAD, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> MOONTHREAD_ARMOR_HEAD = ITEMS.register("moonthread_armor_helmet", () -> new MoonthreadArmorItem(
            MoonthreadArmorMaterial.MOONTHREAD, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> MOONTHREAD_ARMOR_BOOTS = ITEMS.register("moonthread_armor_boots", () -> new MoonthreadArmorItem(
            MoonthreadArmorMaterial.MOONTHREAD, ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<Item> POTION_AMULET = ITEMS.register("potion_amulet", PotionAmuletItem::new);
    public static final RegistryObject<Item> POTION_POUCH = ITEMS.register("potion_pouch", PotionPouchItem::new);
    public static final RegistryObject<Item> TRANSMUTED_SILVER_NUGGET = ITEMS.register("transmuted_silver_nugget", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> WITCH_EYE = ITEMS.register("witch_eye", WitchEyeItem::new);
    public static final RegistryObject<Item> GRIMOIRE_COVEN = ITEMS.register("grimoire_coven", CovenGrimoire::new);
    public static final RegistryObject<Item> CONDENSED_MOONLIGHT_BUCKET = ITEMS.register("condensed_moonlight_bucket", 
            () -> new CondensedMoonlightBucketItem(FluidRegistry.CONDENSED_MOONLIGHT_SOURCE, new Item.Properties()));
    public static final RegistryObject<Item> CONDENSED_MOONLIGHT_BOTTLE = ITEMS.register("condensed_moonlight_bottle", 
            () -> new CondensedMoonlightBottleItem(new Item.Properties()));
    public static final RegistryObject<Item> MOONTHREAD = ITEMS.register("moonthread", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLYING_BROOM = ITEMS.register("flying_broom", FlyingBroomItem::new);

    @SubscribeEvent
    public static void FillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == MACreativeTabs.GENERAL) {
            ITEMS.getEntries().stream().map(RegistryObject::get).forEach(event::accept);
        }
    }
}
