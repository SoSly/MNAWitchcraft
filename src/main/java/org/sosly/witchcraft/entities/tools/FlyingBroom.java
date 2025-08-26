package org.sosly.witchcraft.entities.tools;

import net.minecraft.client.Minecraft;
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
import net.minecraftforge.items.ItemStackHandler;
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
import org.sosly.witchcraft.utils.ChunkLoader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class FlyingBroom extends PathfinderMob {
    private static final EntityDataAccessor<Float> DATA_HOVER_OFFSET = SynchedEntityData.defineId(FlyingBroom.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_RIBBON_COLOR = SynchedEntityData.defineId(FlyingBroom.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_HANDLE_WOOD = SynchedEntityData.defineId(FlyingBroom.class, EntityDataSerializers.STRING);

    private UUID ownerUUID;
    private Vec3 summonTarget = null;
    private Boolean originalSprintToggle = null;
    private ChunkLoader chunkLoader = null;

    private FlyingBroomData broomData = new FlyingBroomData();


    private Vec3 currentMotion = Vec3.ZERO;
    private double currentVerticalMotion = 0.0;
    private static final double ACCELERATION = 0.08;
    private static final double DECELERATION = 0.92;
    private static final double VERTICAL_ACCELERATION = 0.1;
    private static final double VERTICAL_DECELERATION = 0.9;

    public FlyingBroom(EntityType<? extends FlyingBroom> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.blocksBuilding = true;
        this.noCulling = true;
        this.setNoGravity(true);

        Objects.requireNonNull(this.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(ServerConfig.flyingBroomSpeed);
        Objects.requireNonNull(this.getAttribute(Attributes.FLYING_SPEED)).setBaseValue(ServerConfig.flyingBroomSpeed);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.FLYING_SPEED, 0)
            ;
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
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        updateChunkLoading();
        regenerate();
    }

    public void setOwner(@Nullable Player player) {
        this.ownerUUID = player != null ? player.getUUID() : null;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public int getBrushTier() {
        return broomData.getBrushTier();
    }

    public void setBrushTier(int tier) {
        this.broomData = new FlyingBroomData(tier, broomData.getHandleWood(), broomData.getRibbonColor(), broomData.getStorageLevel());
        copyInventoriesToNewData();
    }

    public ResourceLocation getHandleWood() {
        if (this.level().isClientSide) {
            return new ResourceLocation(this.entityData.get(DATA_HANDLE_WOOD));
        }
        return broomData.getHandleWood();
    }

    public void setHandleWood(ResourceLocation wood) {
        this.broomData = broomData.withNewWood(wood);
        this.entityData.set(DATA_HANDLE_WOOD, wood.toString());
    }

    public int getRibbonColor() {
        if (this.level().isClientSide) {
            return this.entityData.get(DATA_RIBBON_COLOR);
        }
        return broomData.getRibbonColor();
    }

    public void setRibbonColor(int color) {
        this.broomData = broomData.withNewColor(color);
        this.entityData.set(DATA_RIBBON_COLOR, color);
    }

    public int getStorageLevel() {
        return broomData.getStorageLevel();
    }

    public void setStorageLevel(int level) {
        this.broomData = broomData.upgradeStorage(level);
    }


    public ItemStackHandler getCharmHandler() {
        return broomData.getCharmHandler();
    }

    public ItemStackHandler getStorageHandler() {
        return broomData.getStorageHandler();
    }
    
    public FlyingBroomData getBroomData() {
        return broomData;
    }
    
    public void setBroomData(FlyingBroomData data) {
        this.broomData = data;
        syncDataToClient();
    }
    
    private void copyInventoriesToNewData() {
        // This method is called when we create a new data object but need to preserve inventories
        // The data class handles inventory copying in its withXXX methods
    }
    
    private void syncDataToClient() {
        this.entityData.set(DATA_RIBBON_COLOR, broomData.getRibbonColor());
        this.entityData.set(DATA_HANDLE_WOOD, broomData.getHandleWood().toString());
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

    private boolean isOwner(Player player) {
        return ownerUUID != null && player.getUUID().equals(ownerUUID);
    }

    private ItemStack createItemWithData() {
        return broomData.toItemStack();
    }

    private void clearPlayerBond(Player player) {
        LazyOptional<IBroomCapability> cap = player.getCapability(BroomProvider.BROOM);
        cap.ifPresent(broom -> broom.setBondedBroomId(null));
    }

    private void giveItemToPlayer(Player player, ItemStack item) {
        if (!player.getInventory().add(item)) {
            player.drop(item, false);
        }
    }

    @Override
    protected boolean canRide(@NotNull Entity entity) {
        return entity instanceof Player player && (ownerUUID == null || player.getUUID().equals(ownerUUID));
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        if (!this.getPassengers().isEmpty()) {
            return false;
        }
        if (!(passenger instanceof Player player)) {
            return false;
        }
        if (ownerUUID != null && !player.getUUID().equals(ownerUUID)) {
            return false;
        }
        
        return true;
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger) {
        super.addPassenger(passenger);
        if (this.level().isClientSide && passenger instanceof LocalPlayer) {
            saveAndDisableSprintToggle();
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        super.removePassenger(passenger);
        if (this.level().isClientSide && passenger instanceof Player) {
            restoreSprintToggle();
        }
        
        if (!this.level().isClientSide && passenger instanceof Player player) {
            cacheBroomLocation(player);
        }
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        return this.getFirstPassenger() instanceof LivingEntity living ? living : null;
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull Entity.MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
        if (passenger instanceof Player) {
            moveFunction.accept(passenger, this.getX(), this.getY() - 0.5, this.getZ());
        }
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

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        LivingEntity controllingPassenger = getControllingPassenger();
        if (controllingPassenger == null) {
            super.travel(travelVector);
            return;
        }
        
        if (this.getNavigation().isInProgress()) {
            Witchcraft.LOGGER.warn("Navigation still in progress while player is riding broom!");
            this.getNavigation().stop();
        }

        this.setYRot(controllingPassenger.getYRot());
        this.yRotO = this.getYRot();
        this.setXRot(controllingPassenger.getXRot() * 0.5F);
        this.setRot(this.getYRot(), this.getXRot());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.getYRot();

        float strafe = controllingPassenger.xxa * 0.5F;
        float forward = controllingPassenger.zza;

        Vec3 targetMotion = calculateTargetMotion(strafe, forward, controllingPassenger);
        updateSprintingState(controllingPassenger);
        currentMotion = applyHorizontalAcceleration(currentMotion, targetMotion);

        double targetVerticalMotion = calculateVerticalMotion(controllingPassenger, travelVector);
        currentVerticalMotion = applyVerticalAcceleration(currentVerticalMotion, targetVerticalMotion);

        Vec3 finalMotion = new Vec3(currentMotion.x, currentVerticalMotion, currentMotion.z);
        this.setDeltaMovement(finalMotion);
        this.move(MoverType.SELF, this.getDeltaMovement());

        this.calculateEntityAnimation(false);
    }

    private Vec3 calculateTargetMotion(float strafe, float forward, LivingEntity controllingPassenger) {
        if (Math.abs(strafe) <= 0.1F && Math.abs(forward) <= 0.1F) {
            return Vec3.ZERO;
        }

        float yaw = (float) Math.toRadians(this.getYRot());
        double motionX = -Math.sin(yaw) * forward + Math.cos(yaw) * strafe;
        double motionZ = Math.cos(yaw) * forward + Math.sin(yaw) * strafe;

        double speed = ServerConfig.flyingBroomSpeed / 20.0;

        return new Vec3(motionX, 0, motionZ).normalize().scale(speed);
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

    private double calculateVerticalMotion(LivingEntity controllingPassenger, Vec3 travelVector) {
        if (controllingPassenger instanceof LocalPlayer localPlayer) {
            return calculateLocalPlayerVerticalMotion(localPlayer);
        }
        return 0;
    }

    private double calculateLocalPlayerVerticalMotion(LocalPlayer localPlayer) {
        if (Minecraft.getInstance().options.keySprint.isDown()) {
            return -ServerConfig.flyingBroomSpeed / 20.0;
        }
        if (localPlayer.input.jumping) {
            return ServerConfig.flyingBroomSpeed / 20.0;
        }
        return 0;
    }

    private void saveAndDisableSprintToggle() {
        if (originalSprintToggle == null) {
            originalSprintToggle = Minecraft.getInstance().options.toggleSprint().get();
            Minecraft.getInstance().options.toggleSprint().set(false);
        }
    }

    private void restoreSprintToggle() {
        if (originalSprintToggle != null) {
            Minecraft.getInstance().options.toggleSprint().set(originalSprintToggle);
            originalSprintToggle = null;
        }
    }
    
    public static void forceRestoreSprintToggle() {
        // Failsafe method to restore sprint toggle in case of unexpected dismount
        // This can be called from client event handlers as needed
    }

    private void updateSprintingState(LivingEntity controllingPassenger) {
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ReturnToOwner(this));
        this.goalSelector.addGoal(3, new SafelyDescend(this));
        this.goalSelector.addGoal(4, new Hover(this));
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
    public boolean isInvulnerableTo(@NotNull DamageSource damageSource) {
        return super.isInvulnerableTo(damageSource)
                || damageSource.is(DamageTypes.IN_WALL)
                || damageSource.is(DamageTypes.DROWN)
                || damageSource.is(DamageTypes.FALL)
            ;
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if (ownerUUID != null && !this.level().isClientSide) {
            clearOwnerBondOnDeath();
        }
        
        restoreSprintToggle();
        cleanupChunkLoader();
        super.die(damageSource);
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
    protected void dropAllDeathLoot(@NotNull DamageSource damageSource) {
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }

        compound.put("BroomData", broomData.toNBT());
    }
    
    public boolean hasSummonTarget() {
        return summonTarget != null;
    }

    public void setSummonTarget(Vec3 target) {
        if (target == null) {
            return;
        }
        this.summonTarget = target;
        initializeChunkLoader();
    }
    
    @Nullable
    public Vec3 getSummonTarget() {
        return summonTarget;
    }
    
    public void clearSummonTarget() {
        summonTarget = null;
        cleanupChunkLoader();
    }
    
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

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void checkDespawn() {
        // Do nothing - we don't want the broom to despawn automatically
    }
}
