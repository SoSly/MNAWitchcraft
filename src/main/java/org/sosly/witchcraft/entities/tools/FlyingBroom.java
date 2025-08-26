package org.sosly.witchcraft.entities.tools;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.api.capabilities.IBroomCapability;
import org.sosly.witchcraft.capabilities.broom.BroomProvider;
import org.sosly.witchcraft.config.ServerConfig;
import org.sosly.witchcraft.data.FlyingBroomData;
import org.sosly.witchcraft.entities.ai.Hover;
import org.sosly.witchcraft.entities.ai.ReturnToOwner;
import org.sosly.witchcraft.entities.ai.SafelyDescend;
import org.sosly.witchcraft.entities.controls.FlightController;
import org.sosly.witchcraft.utils.ChunkLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class FlyingBroom extends PathfinderMob {
    private static final EntityDataAccessor<Float> DATA_HOVER_OFFSET = SynchedEntityData.defineId(FlyingBroom.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_RIBBON_COLOR = SynchedEntityData.defineId(FlyingBroom.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_HANDLE_WOOD = SynchedEntityData.defineId(FlyingBroom.class, EntityDataSerializers.STRING);
    private final FlightController flightController = new FlightController();
    private UUID ownerUUID;
    private Vec3 summonTarget = null;
    private ChunkLoader chunkLoader = null;
    private FlyingBroomData broomData = new FlyingBroomData();

    public FlyingBroom(EntityType<? extends FlyingBroom> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.blocksBuilding = true;
        this.noCulling = true;
        this.setNoGravity(true);

        Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(ServerConfig.flyingBroomSpeed);
        Objects.requireNonNull(this.getAttribute(Attributes.FLYING_SPEED)).setBaseValue(ServerConfig.flyingBroomSpeed);
    }

    private void cacheBroomLocation(Player player) {
        LazyOptional<IBroomCapability> cap = player.getCapability(BroomProvider.BROOM);
        cap.ifPresent(broom -> {
            if (this.uuid.equals(broom.getBondedBroomId())) {
                broom.setLastKnownPosition(this.blockPosition());
                broom.setLastKnownDimension(this.level().dimension().location());
            }
        });
    }

    @Override
    protected boolean canRide(@NotNull Entity entity) {
        return entity instanceof Player player && (ownerUUID == null || player.getUUID().equals(ownerUUID));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource damageSource) {
        return false;
    }

    @Override
    public void checkDespawn() {
        // Do nothing - we don't want the broom to despawn automatically
    }

    private void clearPlayerBond(Player player) {
        LazyOptional<IBroomCapability> cap = player.getCapability(BroomProvider.BROOM);
        cap.ifPresent(broom -> broom.setBondedBroomId(null));
    }

    private void clearOwnerBondOnDeath() {
        Player owner = this.level().getPlayerByUUID(ownerUUID);
        if (owner == null) {
            return;
        }

        LazyOptional<IBroomCapability> cap = owner.getCapability(BroomProvider.BROOM);
        cap.ifPresent(broom -> {
            if (this.uuid.equals(broom.getBondedBroomId())) {
                broom.setBondedBroomId(null);
                broom.setLastKnownPosition(null);
                broom.setLastKnownDimension(null);
            }
        });
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.FLYING_SPEED, 0)
                ;
    }

    private ItemStack createItemWithData() {
        return broomData.toItemStack();
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(false);
        return flyingpathnavigation;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HOVER_OFFSET, 0.0F);
        this.entityData.define(DATA_RIBBON_COLOR, FlyingBroomData.DEFAULT_RIBBON_COLOR);
        this.entityData.define(DATA_HANDLE_WOOD, FlyingBroomData.DEFAULT_HANDLE_WOOD.toString());
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (ownerUUID != null && !this.level().isClientSide) {
            clearOwnerBondOnDeath();
        }

        flightController.restoreSprintToggle();
        cleanupChunkLoader();
        super.die(damageSource);
    }

    @Override
    protected void dropAllDeathLoot(@NotNull DamageSource damageSource) {
    }

    @Override
    public boolean fireImmune() {
        return false;
    }

    private void giveItemToPlayer(Player player, ItemStack item) {
        if (!player.getInventory().add(item)) {
            player.drop(item, false);
        }
    }

    private InteractionResult handleMountAttempt(Player player) {
        if (!canRide(player) || !canAddPassenger(player)) {
            Witchcraft.LOGGER.warn("Broom mount denied for {} (owner: {})",
                    player.getName().getString(),
                    ownerUUID != null ? "set" : "none");
            return InteractionResult.PASS;
        }

        player.startRiding(this);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handlePickupAttempt(Player player) {
        if (!isOwner(player)) {
            Witchcraft.LOGGER.warn("Broom pickup denied for {} (owner: {})",
                    player.getName().getString(),
                    ownerUUID != null ? "set" : "none");
            return InteractionResult.FAIL;
        }

        ItemStack broomItem = createItemWithData();
        clearPlayerBond(player);
        giveItemToPlayer(player, broomItem);
        this.discard();

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 vec, @NotNull InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            return handlePickupAttempt(player);
        }

        return handleMountAttempt(player);
    }

    @Override
    protected boolean isImmobile() {
        return false;
    }

    private boolean isOwner(Player player) {
        return ownerUUID != null && player.getUUID().equals(ownerUUID);
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource damageSource) {
        return super.isInvulnerableTo(damageSource)
                || damageSource.is(DamageTypes.IN_WALL)
                || damageSource.is(DamageTypes.DROWN)
                || damageSource.is(DamageTypes.FALL)
                ;
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull Entity.MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
        if (passenger instanceof Player) {
            moveFunction.accept(passenger, this.getX(), this.getY() - 0.5, this.getZ());
        }
    }
    private void regenerate() {
        if (this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0F);
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ReturnToOwner(this));
        this.goalSelector.addGoal(3, new SafelyDescend(this));
        this.goalSelector.addGoal(4, new Hover(this));
    }

    private void syncDataToClient() {
        this.entityData.set(DATA_RIBBON_COLOR, broomData.getRibbonColor());
        this.entityData.set(DATA_HANDLE_WOOD, broomData.getHandleWood().toString());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        updateChunkLoading();
        regenerate();
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        LivingEntity controllingPassenger = getControllingPassenger();
        if (controllingPassenger == null) {
            super.travel(travelVector);
            return;
        }

        flightController.handlePlayerControlledTravel(this, travelVector, ServerConfig.flyingBroomSpeed);
    }

    // Chunk Loading
    private void initializeChunkLoader() {
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (chunkLoader == null) {
            chunkLoader = new ChunkLoader(serverLevel, this.getUUID());
        }
    }

    private void updateChunkLoading() {
        if (chunkLoader == null || summonTarget == null) {
            return;
        }

        chunkLoader.loadChunksAround(this.position());
    }

    private void cleanupChunkLoader() {
        if (chunkLoader != null) {
            chunkLoader.unloadAll();
            chunkLoader = null;
        }
    }


    // Setters and Getters
    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }

        compound.put("BroomData", broomData.toNBT());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
        }

        if (compound.contains("BroomData")) {
            this.broomData = FlyingBroomData.fromNBT(compound.getCompound("BroomData"));
        } else {
            this.broomData = new FlyingBroomData();
        }

        syncDataToClient();
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return Collections.emptyList();
    }
    public void setBroomData(FlyingBroomData data) {
        this.broomData = data;
        syncDataToClient();
    }

    public int getBrushTier() {
        return broomData.getBrushTier();
    }

    public ResourceLocation getHandleWood() {
        if (this.level().isClientSide) {
            return new ResourceLocation(this.entityData.get(DATA_HANDLE_WOOD));
        }
        return broomData.getHandleWood();
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        if (!this.getPassengers().isEmpty()) {
            return false;
        }
        if (!(passenger instanceof Player player)) {
            return false;
        }
        return ownerUUID == null || player.getUUID().equals(ownerUUID);
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger) {
        super.addPassenger(passenger);
        if (this.level().isClientSide && passenger instanceof LocalPlayer) {
            flightController.saveAndDisableSprintToggle();
        }
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        super.removePassenger(passenger);
        if (this.level().isClientSide && passenger instanceof Player) {
            flightController.restoreSprintToggle();
        }

        if (!this.level().isClientSide && passenger instanceof Player player) {
            cacheBroomLocation(player);
        }
    }

    public int getRibbonColor() {
        if (this.level().isClientSide) {
            return this.entityData.get(DATA_RIBBON_COLOR);
        }
        return broomData.getRibbonColor();
    }

    public boolean hasSummonTarget() {
        return summonTarget != null;
    }

    @Nullable
    public Vec3 getSummonTarget() {
        return summonTarget;
    }

    public void setSummonTarget(Vec3 target) {
        if (target == null) {
            return;
        }
        this.summonTarget = target;
        initializeChunkLoader();
    }

    public void clearSummonTarget() {
        summonTarget = null;
        cleanupChunkLoader();
    }

    public void setOwner(@Nullable Player player) {
        this.ownerUUID = player != null ? player.getUUID() : null;
    }
}
