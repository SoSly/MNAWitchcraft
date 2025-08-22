package org.sosly.witchcraft.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

public class FlyingBroomEntity extends PathfinderMob {
    private static final EntityDataAccessor<Float> DATA_HOVER_OFFSET = SynchedEntityData.defineId(FlyingBroomEntity.class, EntityDataSerializers.FLOAT);
    
    private UUID ownerUUID;

    public FlyingBroomEntity(EntityType<? extends FlyingBroomEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 0, true);
        this.moveControl.setWantedPosition(this.getX(), this.getY(), this.getZ(), 1);
        this.lookControl.setLookAt(Vec3.ZERO);
        this.blocksBuilding = true;
        this.noCulling = true;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 5.0D)
                .add(Attributes.FLYING_SPEED, 5.0D);
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        // Flying brooms don't have equipment slots, so do nothing
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HOVER_OFFSET, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        regenerate();
        seekTheGround();
    }

    private boolean isOnGround() {
        double ground = findGroundLevel();

        // Check if the broom is too low or too high above the ground
        return !(this.getY() > ground + 1.25 || this.getY() < ground + .5);
    }

    private void seekTheGround() {
        if (!this.getPassengers().isEmpty() || this.tickCount % 20 != 0 || this.isOnGround()) {
            return;
        }

        double targetHoverY = findGroundLevel() + 1.25;
        this.getMoveControl().setWantedPosition(this.getX(), targetHoverY, this.getZ(), 1);
    }
    
    private double findGroundLevel() {
        Vec3 start = new Vec3(this.getX(), this.getY(), this.getZ());
        Vec3 end = new Vec3(this.getX(), this.getY() - 100, this.getZ()); // Cast ray 100 blocks down
        
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this);
        BlockHitResult result = this.level().clip(context);
        
        if (result.getType() == HitResult.Type.BLOCK) {
            return result.getLocation().y;
        }

        // If no ground found, just use current Y minus some distance
        return this.getY() - 50;
    }

    public void setOwner(@Nullable Player player) {
        this.ownerUUID = player != null ? player.getUUID() : null;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    @Override
    protected boolean canRide(@NotNull Entity entity) {
        return entity instanceof Player player && (ownerUUID == null || player.getUUID().equals(ownerUUID));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
        }
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        super.travel(travelVector);
    }

    @Override
    protected void registerGoals() {
        // Don't register any goals - prevents look behavior
    }

    @Override
    protected boolean isImmobile() {
        return false;
    }

    private void regenerate() {
        if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0F);
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return damageSource.is(DamageTypes.IN_WALL) || damageSource.is(DamageTypes.DROWN) || super.isInvulnerableTo(damageSource);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
