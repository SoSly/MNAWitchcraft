package org.sosly.witchcraft.entities.ai;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import org.slf4j.Logger;
import org.sosly.witchcraft.entities.tools.FlyingBroom;

import java.util.EnumSet;

public class Hover extends AbstractFlyingGoal {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double HOVER_RANGE = 4.0;
    private static final double PREFERRED_HOVER_HEIGHT = 1.25;
    private static final double MOVE_TO_HOVER_SPEED = 0.3;
    private static final double WAVE_FREQUENCY = 0.05;
    private static final double WAVE_AMPLITUDE = 0.125;
    private static final int YAW_CHANGE_INTERVAL = 100;
    private static final double MAX_YAW_OFFSET = 8.0;
    private static final double PITCH_FREQUENCY = 0.1;
    private static final double MAX_PITCH_OFFSET = 15.0;
    
    private double targetHoverHeight = 0.0;
    private boolean isTransitioning = false;
    private float baseYaw = 0.0f;
    private float basePitch = 0.0f;
    private float currentYawOffset = 0.0f;

    public Hover(FlyingBroom broom) {
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

        double groundLevel = raycastToGround();
        if (groundLevel < broom.level().getMinBuildHeight() - 50) {
            LOGGER.warn("Hover raycast returned suspicious ground level {} for broom at {}", 
                groundLevel, broom.position());
            return false;
        }
        
        double distanceToGround = broom.getY() - groundLevel;
        return distanceToGround <= HOVER_RANGE;
    }
    
    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        double groundLevel = raycastToGround();
        if (groundLevel < broom.level().getMinBuildHeight() - 50) {
            LOGGER.error("Hover start failed - invalid ground level {} at {}", groundLevel, broom.position());
            return;
        }
        
        targetHoverHeight = groundLevel + PREFERRED_HOVER_HEIGHT;
        if (targetHoverHeight > broom.level().getMaxBuildHeight()) {
            LOGGER.warn("Hover height {} exceeds world limit, clamping to {}", 
                targetHoverHeight, broom.level().getMaxBuildHeight() - 1);
            targetHoverHeight = broom.level().getMaxBuildHeight() - 1;
        }
        
        isTransitioning = true;
        baseYaw = broom.getYRot();
        basePitch = broom.getXRot();
    }
    
    @Override
    public void tick() {
        if (isTransitioning) {
            moveToHoverHeight();
        } else {
            performHoverAnimation();
        }
    }

    private void moveToHoverHeight() {
        double currentY = broom.getY();
        double deltaY = targetHoverHeight - currentY;
        
        if (Math.abs(deltaY) < 0.1) {
            isTransitioning = false;
            return;
        }
        
        double moveDistance = Math.min(Math.abs(deltaY), MOVE_TO_HOVER_SPEED);
        double newY = currentY + (Math.signum(deltaY) * moveDistance);
        
        broom.setPos(broom.getX(), newY, broom.getZ());
    }

    private void performHoverAnimation() {
        double verticalOffset = Math.sin(broom.tickCount * WAVE_FREQUENCY) * WAVE_AMPLITUDE;
        double newY = targetHoverHeight + verticalOffset;
        
        if (broom.tickCount % YAW_CHANGE_INTERVAL == 0) {
            currentYawOffset = (float) ((broom.getRandom().nextDouble() - 0.5) * 2 * MAX_YAW_OFFSET);
        }
        float newYaw = baseYaw + currentYawOffset;
        
        double pitchOffset = Math.sin(broom.tickCount * PITCH_FREQUENCY) * MAX_PITCH_OFFSET;
        float newPitch = basePitch + (float) pitchOffset;
        
        broom.setPos(broom.getX(), newY, broom.getZ());
        broom.setYRot(newYaw);
        broom.setXRot(newPitch);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }


    @Override
    public void stop() {
        isTransitioning = false;
    }
}