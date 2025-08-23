package org.sosly.witchcraft.entities;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;
import org.sosly.witchcraft.items.ItemRegistry;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.UUID;

public class FlyingBroomEntity extends PathfinderMob {
    private static final EntityDataAccessor<Float> DATA_HOVER_OFFSET = SynchedEntityData.defineId(FlyingBroomEntity.class, EntityDataSerializers.FLOAT);
    
    private UUID ownerUUID;
    
    // Broom customization data
    private int brushTier = 1; // 1=Overworld, 2=Nether, 3=Universal
    private ResourceLocation handleWood = new ResourceLocation("minecraft:oak");
    private int ribbonColor = 16383998; // Default white
    private int storageLevel = 0; // 0=None, 1=Small(9), 2=Medium(18), 3=Large(27)

    private final double jitterAmount = 0.0001; // Very small movement
    
    // Inventories
    private final ItemStackHandler charmHandler = new ItemStackHandler(3); // 3 charm slots
    private ItemStackHandler storageHandler = new ItemStackHandler(0); // Size varies by storage level

    public FlyingBroomEntity(EntityType<? extends FlyingBroomEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 0, true);
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
        
        // Add tiny jitter in facing direction to maintain orientation
        float yaw = (float) Math.toRadians(this.getYRot());
        double targetX = this.getX() + Math.sin(-yaw) * jitterAmount;
        double targetZ = this.getZ() + Math.cos(yaw) * jitterAmount;
        
        this.getMoveControl().setWantedPosition(targetX, targetHoverY, targetZ, 1);
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

    // Broom customization getters/setters
    public int getBrushTier() { return brushTier; }
    public void setBrushTier(int tier) { 
        this.brushTier = tier; 
    }

    public ResourceLocation getHandleWood() { return handleWood; }
    public void setHandleWood(ResourceLocation wood) { 
        this.handleWood = wood; 
    }

    public int getRibbonColor() { return ribbonColor; }
    public void setRibbonColor(int color) { 
        this.ribbonColor = color; 
    }

    public int getStorageLevel() { return storageLevel; }
    public void setStorageLevel(int level) { 
        this.storageLevel = level;
        int slots = getSlotsForLevel(level);
        if (slots == storageHandler.getSlots()) {
            return;
        }
        
        resizeStorageHandler(slots);
    }
    
    private int getSlotsForLevel(int level) {
        return switch (level) {
            case 1 -> 9;
            case 2 -> 18;
            case 3 -> 27;
            default -> 0;
        };
    }
    
    private void resizeStorageHandler(int slots) {
        ItemStackHandler oldHandler = storageHandler;
        storageHandler = new ItemStackHandler(slots);
        copyItemsToNewHandler(oldHandler, slots);
    }
    
    private void copyItemsToNewHandler(ItemStackHandler oldHandler, int newSlots) {
        int itemsToCopy = Math.min(oldHandler.getSlots(), newSlots);
        for (int i = 0; i < itemsToCopy; i++) {
            storageHandler.setStackInSlot(i, oldHandler.getStackInSlot(i));
        }
    }

    public ItemStackHandler getCharmHandler() { return charmHandler; }
    public ItemStackHandler getStorageHandler() { return storageHandler; }

    @Override
    public @NotNull InteractionResult interactAt(@NotNull Player player, @NotNull Vec3 vec, @NotNull InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            return super.interactAt(player, vec, hand);
        }
        if (!isOwner(player)) {
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
        ItemStack broomItem = new ItemStack(ItemRegistry.FLYING_BROOM.get());
        CompoundTag nbt = new CompoundTag();
        
        nbt.putInt("BrushTier", this.brushTier);
        nbt.putString("HandleWood", this.handleWood.toString());
        nbt.putInt("RibbonColor", this.ribbonColor);
        nbt.putInt("StorageLevel", this.storageLevel);
        nbt.put("CharmInventory", charmHandler.serializeNBT());
        nbt.put("StorageInventory", storageHandler.serializeNBT());
        
        broomItem.setTag(nbt);
        return broomItem;
    }
    
    private void clearPlayerBond(Player player) {
        LazyOptional<ICovenCapability> cap = player.getCapability(CovenProvider.COVEN);
        cap.ifPresent(coven -> coven.setBondedBroomId(null));
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
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
        }
        
        // Read customization data
        this.brushTier = compound.getInt("BrushTier");
        if (compound.contains("HandleWood")) {
            this.handleWood = new ResourceLocation(compound.getString("HandleWood"));
        }
        this.ribbonColor = compound.getInt("RibbonColor");
        
        // Read storage level and resize handler if needed
        int newStorageLevel = compound.getInt("StorageLevel");
        if (newStorageLevel != this.storageLevel) {
            setStorageLevel(newStorageLevel);
        }
        
        // Read inventories
        if (compound.contains("CharmInventory")) {
            charmHandler.deserializeNBT(compound.getCompound("CharmInventory"));
        }
        if (compound.contains("StorageInventory")) {
            storageHandler.deserializeNBT(compound.getCompound("StorageInventory"));
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
    public void die(@NotNull DamageSource damageSource) {
        if (ownerUUID != null && !this.level().isClientSide) {
            clearOwnerBondOnDeath();
        }
        super.die(damageSource);
    }
    
    private void clearOwnerBondOnDeath() {
        Player owner = this.level().getPlayerByUUID(ownerUUID);
        if (owner == null) {
            return;
        }
        
        LazyOptional<ICovenCapability> cap = owner.getCapability(CovenProvider.COVEN);
        cap.ifPresent(coven -> {
            if (this.uuid.equals(coven.getBondedBroomId())) {
                coven.setBondedBroomId(null);
            }
        });
    }

    @Override
    protected void dropAllDeathLoot(@NotNull DamageSource damageSource) {
        // Don't drop any items on death - upgrades are lost permanently
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
        
        // Write customization data
        compound.putInt("BrushTier", this.brushTier);
        compound.putString("HandleWood", this.handleWood.toString());
        compound.putInt("RibbonColor", this.ribbonColor);
        compound.putInt("StorageLevel", this.storageLevel);
        
        // Write inventories
        compound.put("CharmInventory", charmHandler.serializeNBT());
        compound.put("StorageInventory", storageHandler.serializeNBT());
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
