package org.sosly.witchcraft.entities.ai;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;
import net.minecraft.world.entity.ai.goal.Goal;
import org.sosly.witchcraft.entities.tools.FlyingBroom;

import java.util.EnumSet;

public class SafelyDescend extends AbstractFlyingGoal {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final float MAX_HOVER_HEIGHT = 5.0f;
    private static final float MIN_HOVER_HEIGHT = 1.0f;
    private static final float HOVER_TARGET_HEIGHT = 1.25f;
    private static final double TICKS_PER_SECOND = 20.0;
    private static final double BASE_SPEED_MULTIPLIER = 1.0;
    private static final double EASING_DISTANCE = 4.0;
    private static final double MIN_SPEED_MULTIPLIER = 0.2;
    private static final double NEAR_TARGET_THRESHOLD = 0.1;
    private float targetY;

    public SafelyDescend(FlyingBroom broom) {
        super(broom);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }
    
    @Override
    public boolean canUse() {
        if (!broom.getPassengers().isEmpty()) {
            return false;
        }

        if (broom.getNavigation().isInProgress()) {
            return false;
        }

        if (broom.hasSummonTarget()) {
            return false;
        }
        
        return isAboveTargetHeight() || isBelowTargetHeight();
    }
    
    @Override
    public boolean canContinueToUse() {
        if (!broom.getPassengers().isEmpty()) {
            return false;
        }

        if (broom.getNavigation().isInProgress()) {
            return false;
        }

        if (broom.hasSummonTarget()) {
            return false;
        }

        return !isNearTargetHeight();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        double groundLevel = raycastToGround();
        if (groundLevel < broom.level().getMinBuildHeight() - 50) {
            LOGGER.warn("Raycast to ground returned extremely low value {} for broom at {}, may indicate raycast failure", 
                groundLevel, broom.position());
        }
        
        targetY = (float) (groundLevel + HOVER_TARGET_HEIGHT);
        
        int minBuildHeight = broom.level().getMinBuildHeight();
        int maxBuildHeight = broom.level().getMaxBuildHeight();
        
        float originalTargetY = targetY;
        targetY = Math.max(targetY, minBuildHeight + 1);
        targetY = Math.min(targetY, maxBuildHeight - 1);
        
        if (Math.abs(originalTargetY - targetY) > 5) {
            LOGGER.warn("SafelyDescend target height clamped from {} to {} due to world bounds at {}", 
                originalTargetY, targetY, broom.position());
        }
    }

    @Override
    public void tick() {
        double currentY = broom.getY();
        double speed = broom.getAttributeValue(Attributes.FLYING_SPEED);
        
        if (speed <= 0) {
            LOGGER.error("Broom at {} has invalid flying speed: {}", broom.position(), speed);
            return;
        }
        
        double baseMoveDistance = speed / TICKS_PER_SECOND * BASE_SPEED_MULTIPLIER;
        
        double distanceToTarget = Math.abs(targetY - currentY);
        
        if (distanceToTarget <= baseMoveDistance) {
            broom.setPos(broom.getX(), targetY, broom.getZ());
            return;
        }
        
        double speedMultiplier = calculateEasedSpeedMultiplier(distanceToTarget);
        double moveDistance = baseMoveDistance * speedMultiplier;

        double direction = targetY > currentY ? 1 : -1;
        double newY = currentY + (direction * moveDistance);
        broom.setPos(broom.getX(), newY, broom.getZ());
    }

    private boolean isAboveTargetHeight() {
        double ground = raycastToGround();
        double currentY = broom.getY();
        return currentY > ground + MAX_HOVER_HEIGHT;
    }

    private boolean isBelowTargetHeight() {
        double ground = raycastToGround();
        double currentY = broom.getY();
        return currentY < ground + MIN_HOVER_HEIGHT;
    }

    private boolean isNearTargetHeight() {
        double currentY = broom.getY();
        return Math.abs(currentY - targetY) <= NEAR_TARGET_THRESHOLD;
    }
    
    private double calculateEasedSpeedMultiplier(double distanceToTarget) {
        if (distanceToTarget >= EASING_DISTANCE) {
            return 1.0;
        }
        
        double easingFactor = distanceToTarget / EASING_DISTANCE;
        return MIN_SPEED_MULTIPLIER + (1.0 - MIN_SPEED_MULTIPLIER) * easingFactor;
    }
}