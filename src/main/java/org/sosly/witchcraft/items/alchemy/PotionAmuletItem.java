package org.sosly.witchcraft.items.alchemy;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.sosly.witchcraft.enchantments.EnchantmentRegistry;
import org.sosly.witchcraft.items.ItemRegistry;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PotionAmuletItem extends Item implements ICurioItem {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public PotionAmuletItem() {
        super(new Properties().stacksTo(1));
    }

    public static Optional<ItemStack> getStack(Player player) {
        Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory(player).resolve();
        if (curios.isEmpty()) {
            LOGGER.warn("Could not access Curios inventory for player {}", player.getName().getString());
            return Optional.empty();
        }

        Optional<SlotResult> result = curios.get().findFirstCurio(ItemRegistry.POTION_AMULET.get());
        return result.map(SlotResult::stack);
    }

    public static List<MobEffectInstance> getImmune(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return List.of();
        }

        int count = tag.getInt("immunityCount");
        List<MobEffectInstance> effects = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            CompoundTag effectTag = tag.getCompound("immunity" + i);
            MobEffectInstance instance = MobEffectInstance.load(effectTag);
            if (instance == null) {
                LOGGER.error("Failed to load MobEffectInstance from NBT tag immunity{} in potion amulet", i);
                continue;
            }
            effects.add(instance);
        }

        return effects;
    }

    public static boolean isImmune(ItemStack stack, MobEffectInstance effect) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }

        int count = tag.getInt("immunityCount");
        for (int i = 0; i < count; i++) {
            CompoundTag effectTag = tag.getCompound("immunity" + i);
            MobEffectInstance instance = MobEffectInstance.load(effectTag);
            if (instance == null) {
                LOGGER.error("Failed to load MobEffectInstance from NBT tag immunity{} during immunity check", i);
                continue;
            }
            if (instance.getEffect() == effect.getEffect()) {
                return true;
            }
        }

        return false;
    }

    public static void setImmune(ItemStack stack, List<MobEffectInstance> effect) {
        CompoundTag tag = stack.getOrCreateTag();
        int i = 0;
        for (MobEffectInstance instance : effect) {
            CompoundTag effectTag = new CompoundTag();
            instance.save(effectTag);
            tag.put("immunity" + i++, effectTag);
        }
        tag.putInt("immunityCount", i);

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        enchants.put(EnchantmentRegistry.POTION_IMMUNE.get(), 1);
        EnchantmentHelper.setEnchantments(enchants, stack);
    }

    public static void clean(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }

        tag.remove("immunityCount");
        for (int i = 0; i < tag.getInt("immunityCount"); i++) {
            tag.remove("immunity" + i);
        }

        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        enchants.remove(EnchantmentRegistry.POTION_IMMUNE.get());
        EnchantmentHelper.setEnchantments(enchants, stack);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return ICurioItem.super.getAttributesTooltip(tooltips, stack);
        }
        
        int count = tag.getInt("immunityCount");
        for (int i = 0; i < count; i++) {
            CompoundTag effectTag = tag.getCompound("immunity" + i);
            MobEffectInstance instance = MobEffectInstance.load(effectTag);
            if (instance == null) {
                continue;
            }

            Component component = Component.translatable("item.mnaw.potion_amulet/tooltip", instance.getEffect().getDisplayName())
                .withStyle(ChatFormatting.ITALIC)
                .withStyle(ChatFormatting.LIGHT_PURPLE);
            tooltips.add(component);
        }

        return ICurioItem.super.getAttributesTooltip(tooltips, stack);
    }
}
