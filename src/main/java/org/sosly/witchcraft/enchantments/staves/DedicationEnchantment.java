package org.sosly.witchcraft.enchantments.staves;

import com.mna.api.items.ItemUtils;
import com.mna.api.spells.base.ISpellDefinition;
import com.mna.enchantments.base.MAEnchantmentBase;
import com.mna.enchantments.framework.EnchantmentEnumExtender;
import com.mna.items.sorcery.ItemStaff;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.sosly.witchcraft.ServerConfig;

public class DedicationEnchantment extends MAEnchantmentBase {
    private static final int chargeModifier = 40;
    public DedicationEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }
    public boolean canEnchant(ItemStack stack) {
        return EnchantmentEnumExtender.Staves().canEnchant(stack.getItem());
    }

    public int getMinLevel() {
        return 1;
    }

    public int getMaxLevel() {
        return 5;
    }

    public void applyEnchantment(ItemStack stack, Player player, int level) {
        if (stack.isEmpty()) {
            return;
        }

        if (!(stack.getItem() instanceof ItemStaff staff)) {
            return;
        }

        ISpellDefinition spell = staff.getSpell(stack, player);
        if (!spell.isValid()) {
            return;
        }

        stack.getOrCreateTag().putInt("dedication", level);

        float tierModifier = 1.0F + ((level - 1) * (float)ServerConfig.dedicationTierMultiplier);
        float charges = ServerConfig.dedicationCharges * chargeModifier * tierModifier;
        if (ItemUtils.getCharges(stack) == 0) {
            ItemUtils.writeCharges(stack, (int) charges);
        }
        ItemUtils.writeMaxCharges(stack, (int) charges);
    }

    public static boolean hasEnoughCharges(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!(stack.getItem() instanceof ItemStaff staff)) {
            return false;
        }

        int manaCost = (int) (staff.getSpell(stack, player).getManaCost() * chargeModifier);
        return ItemUtils.getCharges(stack) > manaCost;
    }

    public void removeEnchantment(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        if (!(stack.getItem() instanceof ItemStaff staff)) {
            return;
        }

        stack.getOrCreateTag().remove("dedication");
        stack.getOrCreateTag().remove("charges");
        stack.getOrCreateTag().remove("max_charges");
    }

    public static void addMana(ItemStack stack, int magnitude) {
        if (stack.isEmpty()) {
            return;
        }

        if (!(stack.getItem() instanceof ItemStaff staff)) {
            return;
        }

        int charges = ItemUtils.getCharges(stack);
        int maxCharges = ItemUtils.getMaxCharges(stack);
        int mana = magnitude * (maxCharges / 5);
        if (charges + mana > maxCharges) {
            ItemUtils.writeCharges(stack, maxCharges);
        } else {
            ItemUtils.writeCharges(stack, charges + mana);
        }
    }
}
