package org.sosly.witchcraft.events.items;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.items.alchemy.PotionAmuletItem;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PotionAmulet {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation potionLocation = new ResourceLocation("minecraft:potion");

    @SubscribeEvent
    public static void onCraftWithPotion(PlayerEvent.ItemCraftedEvent event) {
        ItemStack crafting = event.getCrafting();
        if (!(crafting.getItem() instanceof PotionAmuletItem)) {
            return;
        }

        ItemStack potionStack = ItemStack.EMPTY;
        for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
            ItemStack stack = event.getInventory().getItem(i);
            if (stack.getItem() == ForgeRegistries.ITEMS.getValue(potionLocation)) {
                potionStack = stack;
            }
        }

        if (potionStack.isEmpty()) {
            LOGGER.warn("No potion found in crafting recipe for potion amulet by player {}", event.getEntity().getName().getString());
            PotionAmuletItem.clean(crafting);
            return;
        }

        List<MobEffectInstance> effects = PotionUtils.getMobEffects(potionStack);
        if (effects.isEmpty()) {
            LOGGER.warn("Potion {} has no effects for amulet crafting by player {}", 
                ForgeRegistries.ITEMS.getKey(potionStack.getItem()), event.getEntity().getName().getString());
            return;
        }

        PotionAmuletItem.setImmune(crafting, effects);
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        Optional<ItemStack> amulet = PotionAmuletItem.getStack(player);
        if (amulet.isEmpty()) {
            return;
        }

        if (PotionAmuletItem.isImmune(amulet.get(), event.getEffectInstance())) {
            event.setResult(MobEffectEvent.Result.DENY);
        }
    }
}
