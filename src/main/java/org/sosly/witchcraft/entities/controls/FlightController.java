package org.sosly.witchcraft.entities.controls;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class FlightController {
    private Vec3 currentMotion = Vec3.ZERO;
    private double currentVerticalMotion = 0.0;
    private Boolean originalSprintToggle = null;
    
    private static final double ACCELERATION = 0.08;
    private static final double DECELERATION = 0.92;
    private static final double VERTICAL_ACCELERATION = 0.1;
    private static final double VERTICAL_DECELERATION = 0.9;

    public Vec3 calculateMotion(float strafe, float forward, LivingEntity controllingPassenger, Vec3 travelVector, double speed, float yaw) {
        Vec3 targetMotion = calculateTargetMotion(strafe, forward, speed, yaw);
        currentMotion = applyHorizontalAcceleration(currentMotion, targetMotion);

        double targetVerticalMotion = calculateVerticalMotion(controllingPassenger, travelVector, speed);
        currentVerticalMotion = applyVerticalAcceleration(currentVerticalMotion, targetVerticalMotion);

        return new Vec3(currentMotion.x, currentVerticalMotion, currentMotion.z);
    }

    private Vec3 calculateTargetMotion(float strafe, float forward, double speed, float yaw) {
        if (Math.abs(strafe) <= 0.1F && Math.abs(forward) <= 0.1F) {
            return Vec3.ZERO;
        }

        float yawRadians = (float) Math.toRadians(yaw);
        double motionX = -Math.sin(yawRadians) * forward + Math.cos(yawRadians) * strafe;
        double motionZ = Math.cos(yawRadians) * forward + Math.sin(yawRadians) * strafe;

        double scaledSpeed = speed / 20.0;
        return new Vec3(motionX, 0, motionZ).normalize().scale(scaledSpeed);
    }

    private Vec3 applyHorizontalAcceleration(Vec3 current, Vec3 target) {
        if (target.equals(Vec3.ZERO)) {
            return current.scale(DECELERATION);
        }

        Vec3 diff = target.subtract(current);
        Vec3 acceleration = diff.scale(ACCELERATION);
        return current.add(acceleration);
    }

    private double applyVerticalAcceleration(double current, double target) {
        if (Math.abs(target) < 0.01) {
            return current * VERTICAL_DECELERATION;
        }

        double diff = target - current;
        double acceleration = diff * VERTICAL_ACCELERATION;
        return current + acceleration;
    }

    private double calculateVerticalMotion(LivingEntity controllingPassenger, Vec3 travelVector, double speed) {
        if (controllingPassenger instanceof LocalPlayer localPlayer) {
            return calculateLocalPlayerVerticalMotion(localPlayer, speed);
        }
        return 0;
    }

    private double calculateLocalPlayerVerticalMotion(LocalPlayer localPlayer, double speed) {
        if (Minecraft.getInstance().options.keySprint.isDown()) {
            return -speed / 20.0;
        }
        if (localPlayer.input.jumping) {
            return speed / 20.0;
        }
        return 0;
    }

    public void saveAndDisableSprintToggle() {
        if (originalSprintToggle != null) {
            return;
        }
        
        originalSprintToggle = Minecraft.getInstance().options.toggleSprint().get();
        Minecraft.getInstance().options.toggleSprint().set(false);
    }

    public void restoreSprintToggle() {
        if (originalSprintToggle == null) {
            return;
        }
        
        Minecraft.getInstance().options.toggleSprint().set(originalSprintToggle);
        originalSprintToggle = null;
    }
    

    public void handlePlayerControlledTravel(PathfinderMob entity, Vec3 travelVector, double speed) {
        LivingEntity controllingPassenger = entity.getControllingPassenger();
        if (controllingPassenger == null) {
            return;
        }
        
        if (entity.getNavigation().isInProgress()) {
            entity.getNavigation().stop();
        }

        entity.setYRot(controllingPassenger.getYRot());
        entity.yRotO = entity.getYRot();
        entity.setXRot(controllingPassenger.getXRot() * 0.5F);
        entity.yBodyRot = entity.getYRot();
        entity.yHeadRot = entity.getYRot();

        float strafe = controllingPassenger.xxa * 0.5F;
        float forward = controllingPassenger.zza;

        Vec3 finalMotion = calculateMotion(strafe, forward, controllingPassenger, travelVector, speed, entity.getYRot());
        entity.setDeltaMovement(finalMotion);
        entity.move(MoverType.SELF, entity.getDeltaMovement());

        entity.calculateEntityAnimation(false);
    }
}