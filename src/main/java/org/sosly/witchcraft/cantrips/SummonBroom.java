package org.sosly.witchcraft.cantrips;

import com.mna.api.cantrips.ICantrip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.capabilities.IBroomCapability;
import org.sosly.witchcraft.capabilities.broom.BroomProvider;
import org.sosly.witchcraft.entities.tools.FlyingBroom;
import org.sosly.witchcraft.utils.ChunkLoader;

import java.util.UUID;

public class SummonBroom {
    public static ResourceLocation ID =  new ResourceLocation(Witchcraft.MOD_ID, "summon_broom");
    public static ResourceLocation ICON = new ResourceLocation(Witchcraft.MOD_ID, "textures/gui/cantrips/summon_broom.png");
    public static ResourceLocation ADVANCEMENT = new ResourceLocation(Witchcraft.MOD_ID, "summon_broom");
    public static int TIER = 3;

    public static void execute(Player player, ICantrip cantrip, InteractionHand hand) {
        IBroomCapability broom = player.getCapability(BroomProvider.BROOM).orElse(null);
        if (!broom.hasBondedBroom()) {
            player.sendSystemMessage(Component.translatable("cantrip.mnaw.summon_broom.no_broom"));
            return;
        }

        UUID broomId = broom.getBondedBroomId();
        if (broomId == null) {
            return;
        }

        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity entity = serverLevel.getEntity(broomId);
        if (!(entity instanceof FlyingBroom)) {
            entity = findBroomAcrossChunks(serverLevel, broomId);
        }

        if (!(entity instanceof FlyingBroom)) {
            entity = findBroomWithChunkLoading(serverLevel, broomId, broom);
        }

        if (!(entity instanceof FlyingBroom broomEntity)) {
            player.sendSystemMessage(Component.translatable("cantrip.mnaw.summon_broom.not_found"));
            return;
        }

        if (!broomEntity.level().dimension().equals(player.level().dimension())) {
            player.sendSystemMessage(Component.translatable("cantrip.mnaw.summon_broom.wrong_dimension"));
            return;
        }

        broomEntity.ejectPassengers();
        
        Vec3 playerPos = player.position();
        Vec3 broomPos = broomEntity.position();
        double distance = playerPos.distanceTo(broomPos);

        if (distance > 32.0) {
            Vec3 direction = broomPos.subtract(playerPos).normalize();
            Vec3 teleportPos = playerPos.add(direction.scale(32.0));
            
            double safeHeight = findSafeHeight(serverLevel, teleportPos.x, teleportPos.z, playerPos.y);
            teleportPos = new Vec3(teleportPos.x, safeHeight, teleportPos.z);
            broomEntity.setPos(teleportPos);
        }

        Vec3 targetPos = playerPos.add(0, 1.5, 0);
        broomEntity.setSummonTarget(targetPos);
        
        player.sendSystemMessage(Component.translatable("cantrip.mnaw.summon_broom.success"));
    }

    private static Entity findBroomAcrossChunks(ServerLevel level, UUID entityId) {
        for (Entity entity : level.getAllEntities()) {
            if (entity.getUUID().equals(entityId) && entity instanceof FlyingBroom) {
                return entity;
            }
        }
        
        return null;
    }

    private static Entity findBroomWithChunkLoading(ServerLevel level, UUID entityId, IBroomCapability broomCap) {
        if (broomCap.getLastKnownPosition() == null || broomCap.getLastKnownDimension() == null) {
            return null;
        }

        if (!broomCap.getLastKnownDimension().equals(level.dimension().location())) {
            return null;
        }

        ChunkLoader chunkLoader = new ChunkLoader(level, entityId);
        try {
            Vec3 cachedPos = Vec3.atCenterOf(broomCap.getLastKnownPosition());
            chunkLoader.loadChunksAround(cachedPos);
            
            Entity entity = level.getEntity(entityId);
            if (entity instanceof FlyingBroom) {
                return entity;
            }
            
            return null;
        } finally {
            chunkLoader.unloadAll();
        }
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