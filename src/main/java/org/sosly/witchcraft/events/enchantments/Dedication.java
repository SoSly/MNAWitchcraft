package org.sosly.witchcraft.events.enchantments;

import com.mojang.logging.LogUtils;
import com.mna.api.events.RuneforgeEnchantEvent;
import com.mna.effects.EffectInit;
import com.mna.items.sorcery.MagicStaff;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.blocks.alchemy.WitchsCauldronBlockEntity;
import org.sosly.witchcraft.compat.MysticAlchemyCompat;
import org.sosly.witchcraft.enchantments.EnchantmentRegistry;
import org.sosly.witchcraft.enchantments.staves.DedicationEnchantment;

import java.util.concurrent.atomic.AtomicBoolean;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Dedication {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void onClick(PlayerInteractEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        if (!(stack.getItem() instanceof MagicStaff staff)) {
            return;
        }

        if (EnchantmentHelper.getEnchantments(stack).get(EnchantmentRegistry.DEDICATION.get()) == null) {
            return;
        }

        BlockPos pos = event.getPos();
        if (pos == null) {
            return;
        }

        if (tryRechargeCrucible(level, pos, stack)) {
            return;
        }

        tryRechargeWitchsCauldron(level, pos, stack);
    }

    private static boolean tryRechargeCrucible(Level level, BlockPos pos, ItemStack stack) {
        var crucible = MysticAlchemyCompat.getCrucibleTile(level, pos);
        if (crucible == null) {
            return false;
        }

        AtomicBoolean success = new AtomicBoolean(false);
        MysticAlchemyCompat.getProminentEffects(crucible).forEach((effect, strength) -> {
            if (effect.equals(EffectInit.INSTANT_MANA.get()) || effect.equals(EffectInit.MANA_REGEN.get())) {
                DedicationEnchantment.addMana(stack, (int) (strength * 1.0F));
                success.set(true);
            }
        });

        if (!success.get()) {
            return false;
        }
        
        BlockState state = level.getBlockState(pos);
        int existingLevel = state.getValue(LayeredCauldronBlock.LEVEL);
        if (existingLevel == 1) {
            level.setBlock(pos, MysticAlchemyCompat.getEmptyCrucibleState(), 3);
        } else {
            level.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, existingLevel - 1), 3);
        }
        return true;
    }

    private static void tryRechargeWitchsCauldron(Level level, BlockPos pos, ItemStack stack) {
        var blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof WitchsCauldronBlockEntity cauldron)) {
            return;
        }

        if (cauldron.isEmpty() || !cauldron.isPotion()) {
            return;
        }

        AtomicBoolean success = new AtomicBoolean(false);
        cauldron.getProminentEffects().forEach((effect, strength) -> {
            if (effect.equals(EffectInit.INSTANT_MANA.get()) || effect.equals(EffectInit.MANA_REGEN.get())) {
                DedicationEnchantment.addMana(stack, (int) (strength * 1.0F));
                success.set(true);
            }
        });

        if (success.get()) {
            cauldron.drain();
        }
    }

    @SubscribeEvent
    public static void onRuneforgedEnchantment(RuneforgeEnchantEvent event) {
        ItemStack staff = event.getOutput();
        EnchantmentHelper.getEnchantments(staff).forEach((enchantment, level) -> {
            if (enchantment.equals(EnchantmentRegistry.DEDICATION.get())) {
                EnchantmentRegistry.DEDICATION.get().applyEnchantment(staff, event.getCrafter(), level);
            }
        });
    }

    @SubscribeEvent
    public static void onGrindstone(GrindstoneEvent.OnPlaceItem event) {
        ItemStack top = event.getTopItem();
        ItemStack bottom = event.getBottomItem();
        ItemStack output = event.getOutput();

        if (output.isEmpty() && (top.isEmpty() ^ bottom.isEmpty())) {
            output = (top.isEmpty() ? bottom : top).copy();
        }

        if (!output.isEmpty()) {
            ItemStack staff = output;
            EnchantmentHelper.getEnchantments(output).forEach((enchantment, level) -> {
                if (enchantment.equals(EnchantmentRegistry.DEDICATION.get())) {
                    EnchantmentRegistry.DEDICATION.get().removeEnchantment(staff);
                    EnchantmentHelper.setEnchantments(EnchantmentHelper.getEnchantments(ItemStack.EMPTY), staff);
                    event.setOutput(staff);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onUseStaff(PlayerInteractEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        if (!(stack.getItem() instanceof MagicStaff staff)) {
            return;
        }

        if (EnchantmentHelper.getEnchantments(stack).get(EnchantmentRegistry.DEDICATION.get()) == null) {
            return;
        }

        if (!DedicationEnchantment.hasEnoughCharges(stack, event.getEntity())) {
            event.setCanceled(true);
            Component message = Component.translatable("enchantment.mnaw.dedication/insufficient_charges", stack.getDisplayName());
            event.getEntity().sendSystemMessage(message);
        }
    }
}
