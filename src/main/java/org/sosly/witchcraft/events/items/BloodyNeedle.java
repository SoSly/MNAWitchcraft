package org.sosly.witchcraft.events.items;

import com.mna.items.ItemInit;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.items.ItemRegistry;
import org.sosly.witchcraft.items.sympathy.BoundPoppetItem;
import org.sosly.witchcraft.utils.SympathyHelper;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodyNeedle {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onCraftWithPoppet(PlayerEvent.ItemCraftedEvent event) {
        Level level = event.getEntity().level();

        ItemStack craftedItem = event.getCrafting();
        if (!(craftedItem.getItem() instanceof BoundPoppetItem)) {
            return;
        }

        ItemStack needle = ItemStack.EMPTY;
        for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
            ItemStack stack = event.getInventory().getItem(i);
            if (stack.getItem() == ItemRegistry.BLOODY_NEEDLE.get()) {
                needle = stack;
            }
        }

        if (needle.isEmpty()) {
            LOGGER.warn("Player {} attempted to craft bound poppet without bloody needle", event.getEntity().getName().getString());
            event.setCanceled(true);
            return;
        }

        CompoundTag tag = needle.getTag();
        if (tag == null) {
            LOGGER.error("Bloody needle missing NBT data for player {}", event.getEntity().getName().getString());
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
            return;
        }
        if (!SympathyHelper.isBound(needle)) {
            LOGGER.warn("Player {} attempted to use unbound bloody needle", event.getEntity().getName().getString());
            event.setResult(Event.Result.DENY);
            event.setCanceled(true);
            return;
        }

        UUID target = tag.getUUID("target");
        String type = tag.getString("type");
        ((BoundPoppetItem)craftedItem.getItem()).setTarget(level, target, type, craftedItem);
    }

    @SubscribeEvent
    public static void onWash(PlayerInteractEvent event) {
        Player player = event.getEntity();
        ItemStack heldItem = player.getItemInHand(player.getUsedItemHand());
        if (heldItem.getItem() != ItemRegistry.BLOODY_NEEDLE.get()) {
            return;
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(player.level(), player);
        if (hitResult.getType() == BlockHitResult.Type.MISS) {
            return;
        }

        BlockPos pos = hitResult.getBlockPos();
        Direction direction = hitResult.getDirection();
        BlockPos offsetPos = pos.relative(direction);
        if (!player.mayUseItemAt(offsetPos, direction, heldItem)) {
            return;
        }
        BlockState blockState = player.level().getBlockState(pos);

        if (!blockState.getBlock().equals(Blocks.WATER) && !blockState.getBlock().equals(Blocks.WATER_CAULDRON)) {
            return;
        }

        ItemStack cleanNeedle = new ItemStack(ItemInit.VINTEUM_NEEDLE.get());
        player.setItemInHand(player.getUsedItemHand(), cleanNeedle);

        player.level().playSound(player, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.5F,
                2.6F + (player.level().random.nextFloat() - player.level().random.nextFloat()) * 0.8F);
    }

    private static BlockHitResult getPlayerPOVHitResult(Level level, Player player) {
        float f = player.getXRot();
        float f1 = player.getYRot();
        Vec3 vec3 = player.getEyePosition();
        float f2 = Mth.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f3 = Mth.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
        float f4 = -Mth.cos(-f * ((float)Math.PI / 180F));
        float f5 = Mth.sin(-f * ((float)Math.PI / 180F));
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d0 = player.getBlockReach();
        Vec3 vec31 = vec3.add((double)f6 * d0, (double)f5 * d0, (double)f7 * d0);
        return level.clip(new ClipContext(vec3, vec31, ClipContext.Block.OUTLINE, ClipContext.Fluid.WATER, player));
    }
}
