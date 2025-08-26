package org.sosly.witchcraft.entities.ai;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import org.sosly.witchcraft.entities.tools.FlyingBroom;

import java.util.EnumSet;

public class ReturnToOwner extends Goal {
    private static final double TARGET_REACH_DISTANCE = 4.0;
    private static final double CLOSE_DISTANCE_THRESHOLD = 1.5;
    private static final double MAX_SECONDS_TO_TARGET = 5;
    private static final double MAX_SPEED_MODIFIER = 2.0;


    private int lastMoved = -1;
    private final FlyingBroom broom;
    private double speedModifier = 1.0;


    public ReturnToOwner(FlyingBroom broom) {
        this.broom = broom;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return broom.hasSummonTarget();
    }

    @Override
    public boolean canContinueToUse() {
        if (!broom.hasSummonTarget()) {
            return false;
        }

        Vec3 summonTarget = broom.getSummonTarget();
        return summonTarget != null && !hasReachedTarget(summonTarget);
    }

    @Override
    public void start() {
        Vec3 summonTarget = broom.getSummonTarget();
        if (summonTarget == null) {
            return;
        }

        speedModifier = calculateSpeedModifier(summonTarget);
    }

    @Override
    public void tick() {
        if (lastMoved >= broom.tickCount - 3) {
            return;
        }
        lastMoved = broom.tickCount;

        Vec3 summonTarget = broom.getSummonTarget();
        if (summonTarget == null) {
            return;
        }

        if (broom.getNavigation().isInProgress()) {
            return;
        }

        broom.getNavigation().moveTo(summonTarget.x, summonTarget.y, summonTarget.z, speedModifier);
    }
    
    private boolean hasReachedTarget(Vec3 target) {
        return broom.position().distanceTo(target) < TARGET_REACH_DISTANCE ||
                calculateHorizontalDistance(target) < CLOSE_DISTANCE_THRESHOLD;
    }
    
    private double calculateHorizontalDistance(Vec3 target) {
        double deltaX = broom.getX() - target.x;
        double deltaZ = broom.getZ() - target.z;
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    private double calculateSpeedModifier(Vec3 target) {
        double distance = broom.position().distanceTo(target);
        double speed = broom.getAttributeValue(Attributes.FLYING_SPEED);
        double speedModifier = distance / (speed * MAX_SECONDS_TO_TARGET);
        return Math.max(1.0, Math.min(speedModifier, MAX_SPEED_MODIFIER));
    }

    @Override
    public void stop() {
        broom.clearSummonTarget();
        broom.getNavigation().stop();
        speedModifier = 1.0;

        System.out.println("ReturnToOwner stopped");
    }
}
