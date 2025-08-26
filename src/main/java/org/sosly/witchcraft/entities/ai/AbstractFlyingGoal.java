package org.sosly.witchcraft.entities.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.sosly.witchcraft.entities.tools.FlyingBroom;

public abstract class AbstractFlyingGoal extends Goal {
    protected final FlyingBroom broom;
    private static final int RAYCAST_CACHE_TICKS = 10;
    
    private double cachedGroundLevel = 0;
    private Vec3 cachedRaycastPosition = Vec3.ZERO;
    private int lastRaycastTick = -1;
    
    protected AbstractFlyingGoal(FlyingBroom broom) {
        this.broom = broom;
    }
    
    protected Vec3 raycast(Vec3 start, Vec3 end) {
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, broom);
        BlockHitResult result = broom.level().clip(context);
        
        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getLocation();
        }
        
        return end;
    }
    
    protected double raycastToGround() {
        return raycastToGround(broom.position());
    }
    
    protected double raycastToGround(Vec3 fromPosition) {
        if (shouldUseCachedRaycast(fromPosition)) {
            return cachedGroundLevel;
        }
        
        Vec3 end = new Vec3(fromPosition.x, fromPosition.y - 100, fromPosition.z);
        Vec3 hitLocation = raycast(fromPosition, end);
        double groundLevel = hitLocation.equals(end) ? fromPosition.y - 50 : hitLocation.y;
        
        updateRaycastCache(fromPosition, groundLevel);
        return groundLevel;
    }
    
    private boolean shouldUseCachedRaycast(Vec3 currentPosition) {
        if (lastRaycastTick == -1) {
            return false;
        }
        
        int ticksSinceLastRaycast = broom.tickCount - lastRaycastTick;
        if (ticksSinceLastRaycast >= RAYCAST_CACHE_TICKS) {
            return false;
        }
        
        double distanceMoved = currentPosition.distanceTo(cachedRaycastPosition);
        return distanceMoved < 2.0;
    }
    
    private void updateRaycastCache(Vec3 position, double groundLevel) {
        this.cachedGroundLevel = groundLevel;
        this.cachedRaycastPosition = position;
        this.lastRaycastTick = broom.tickCount;
    }
}