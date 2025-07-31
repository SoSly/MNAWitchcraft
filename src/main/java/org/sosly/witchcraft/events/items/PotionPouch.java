package org.sosly.witchcraft.events.items;

import com.mna.api.events.RunicAnvilShouldActivateEvent;
import com.mna.blocks.BlockInit;
import com.mna.blocks.tileentities.RunicAnvilTile;
import com.mna.items.ItemInit;
import com.mna.items.ritual.PractitionersPatch;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.items.ItemRegistry;
import org.sosly.witchcraft.items.alchemy.PotionPouchItem;

import java.util.List;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PotionPouch {
    private static List<PractitionersPatch> allowedPatches = List.of();

    @SubscribeEvent
    public static void onRunicAnvil(RunicAnvilShouldActivateEvent event) {
        if (allowedPatches.isEmpty()) {
            allowedPatches = List.of(ItemInit.PATCH_DEPTH.get(),
                    ItemInit.PATCH_DEPTH_2.get(), ItemInit.PATCH_CONVEYANCE.get(), ItemInit.PATCH_SPEED.get(),
                    ItemInit.PATCH_SPEED_2.get(), ItemInit.PATCH_SPEED_3.get(), ItemInit.PATCH_COLLECTION.get());
        }

        ItemStack stack = event.pattern;
        ItemStack stack1 = event.material;
        if (stack.getItem() == ItemRegistry.POTION_POUCH.get()) {
            if (allowedPatches.contains(stack1.getItem())) {
                event.setResult(Event.Result.ALLOW);
            }
        }
    }

    @SubscribeEvent
    public static void onUseSewingKit(PlayerInteractEvent event) {
        Level level = event.getLevel();
        if (level.isClientSide()) {
            return;
        }

        ItemStack itemInHand = event.getItemStack();
        if (itemInHand.getItem() != ItemInit.SORCEROUS_SEWING_SET.get()) {
            return;
        }

        BlockPos pos = event.getPos();
        Block block = level.getBlockState(pos).getBlock();
        if (block != BlockInit.RUNIC_ANVIL.get()) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof RunicAnvilTile)) {
            return;
        }

        RunicAnvilTile runicAnvilTile = (RunicAnvilTile) blockEntity;
        ItemStack baseItem = runicAnvilTile.getItem(0);
        if (baseItem.getItem() != ItemRegistry.POTION_POUCH.get()) {
            return;
        }

        ItemStack material = runicAnvilTile.getItem(1);
        if (!((PotionPouchItem)baseItem.getItem()).addPatchToPouch(baseItem, material)) {
            return;
        }

        runicAnvilTile.setItem(0, baseItem);
        runicAnvilTile.setItem(1, ItemStack.EMPTY);
        itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
        event.setCanceled(true);
    }
}
