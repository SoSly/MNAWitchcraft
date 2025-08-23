package org.sosly.witchcraft.cantrips;

import com.mna.api.cantrips.ICantrip;
import com.mna.cantrips.CantripRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;
import org.sosly.witchcraft.entities.FlyingBroomEntity;

import java.util.UUID;

public class Cantrips {
    private static final ResourceLocation BOLT = new ResourceLocation("mna", "manaweave_patterns/bolt");
    private static final ResourceLocation CIRCLE = new ResourceLocation("mna", "manaweave_patterns/circle");
    private static final ResourceLocation INVERSE_TRIANGLE = new ResourceLocation("mna", "manaweave_patterns/inverted_triangle");

    public static void registerCantrips() {
        CantripRegistry.INSTANCE.registerCantrip(
            new ResourceLocation(Witchcraft.MOD_ID, "cantrips/summon_broom"),
            new ResourceLocation(Witchcraft.MOD_ID, "textures/gui/cantrips/summon_broom.png"), 
            3, 
            Cantrips::summonBroom,
            ItemStack.EMPTY, 
            INVERSE_TRIANGLE, CIRCLE, BOLT
        ).setRequiredAdvancement(new ResourceLocation(Witchcraft.MOD_ID, "summon_broom"));
    }

    public static void summonBroom(Player player, ICantrip cantrip, InteractionHand hand) {
        LazyOptional<ICovenCapability> cap = player.getCapability(CovenProvider.COVEN);
        if (!cap.isPresent()) {
            return;
        }

        ICovenCapability coven = cap.orElse(null);
        if (coven == null || !coven.hasBondedBroom()) {
            player.sendSystemMessage(Component.translatable("cantrip.mnaw.summon_broom.no_broom"));
            return;
        }

        UUID broomId = coven.getBondedBroomId();
        if (broomId == null) {
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity entity = serverLevel.getEntity(broomId);
        if (!(entity instanceof FlyingBroomEntity broom)) {
            player.sendSystemMessage(Component.translatable("cantrip.mnaw.summon_broom.not_found"));
            return;
        }

        if (!broom.level().dimension().equals(player.level().dimension())) {
            player.sendSystemMessage(Component.translatable("cantrip.mnaw.summon_broom.wrong_dimension"));
            return;
        }

        broom.ejectPassengers();
        
        Vec3 playerPos = player.position();
        Vec3 broomPos = broom.position();
        double distance = playerPos.distanceTo(broomPos);

        if (distance > 64.0) {
            Vec3 direction = broomPos.subtract(playerPos).normalize();
            Vec3 teleportPos = playerPos.add(direction.scale(64.0));
            
            double safeHeight = findSafeHeight(serverLevel, teleportPos.x, teleportPos.z, playerPos.y);
            teleportPos = new Vec3(teleportPos.x, safeHeight, teleportPos.z);
            broom.setPos(teleportPos);
        }

        Vec3 targetPos = playerPos.add(0, 1.5, 0);
        broom.flyTowardsTarget(targetPos);
        
        player.sendSystemMessage(Component.translatable("cantrip.mnaw.summon_broom.success"));
    }

    private static double findSafeHeight(ServerLevel level, double x, double z, double playerY) {
        Vec3 start = new Vec3(x, Math.max(playerY + 20, level.getMaxBuildHeight() - 1), z);
        Vec3 end = new Vec3(x, level.getMinBuildHeight(), z);
        
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        BlockHitResult result = level.clip(context);
        
        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getLocation().y + 3.0;
        }
        
        return Math.max(playerY + 10, level.getSeaLevel() + 10);
    }
}