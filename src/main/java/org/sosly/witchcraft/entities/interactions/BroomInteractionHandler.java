package org.sosly.witchcraft.entities.interactions;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.entities.tools.FlyingBroom;

public class BroomInteractionHandler {
    private final FlyingBroom broom;

    public BroomInteractionHandler(FlyingBroom broom) {
        this.broom = broom;
    }

    public InteractionResult handleInteraction(Player player, Vec3 vec, InteractionHand hand) {
        if (broom.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            return handlePickup(player);
        }

        return handleMount(player);
    }

    private InteractionResult handleMount(Player player) {
        if (!broom.canRide(player) || !broom.canAddPassenger(player)) {
            Witchcraft.LOGGER.warn("Broom mount denied for {} (owner: {})",
                    player.getName().getString(),
                    broom.getOwnerUUID() != null ? "set" : "none");
            return InteractionResult.PASS;
        }

        player.startRiding(broom);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handlePickup(Player player) {
        if (!broom.isOwner(player)) {
            Witchcraft.LOGGER.warn("Broom pickup denied for {} (owner: {})",
                    player.getName().getString(),
                    broom.getOwnerUUID() != null ? "set" : "none");
            return InteractionResult.FAIL;
        }

        ItemStack broomItem = broom.createItemWithData();
        broom.clearPlayerBond(player);
        giveItemToPlayer(player, broomItem);
        broom.discard();

        return InteractionResult.SUCCESS;
    }

    private void giveItemToPlayer(Player player, ItemStack item) {
        if (!player.getInventory().add(item)) {
            player.drop(item, false);
        }
    }
}