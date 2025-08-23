package org.sosly.witchcraft.items;

import com.mna.api.items.TieredItem;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;
import org.sosly.witchcraft.entities.EntityTypeRegistry;
import org.sosly.witchcraft.entities.FlyingBroomEntity;

public class FlyingBroomItem extends TieredItem {
    public FlyingBroomItem() {
        super((new Item.Properties()).stacksTo(1));
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        cleanupExistingBroom(player, level);
        FlyingBroomEntity broomEntity = createAndPositionBroom(context);
        transferNBTToBroom(context.getItemInHand(), broomEntity);
        bondBroomToPlayer(player, broomEntity);
        
        level.addFreshEntity(broomEntity);
        consumeItem(context.getItemInHand(), player, context);
        
        return InteractionResult.SUCCESS;
    }
    
    private void cleanupExistingBroom(Player player, Level level) {
        LazyOptional<ICovenCapability> cap = player.getCapability(CovenProvider.COVEN);
        cap.ifPresent(coven -> {
            if (!coven.hasBondedBroom()) {
                return;
            }
            
            FlyingBroomEntity existingBroom = findBondedBroom(level, player, coven);
            if (existingBroom != null) {
                existingBroom.discard();
            }
            coven.setBondedBroomId(null);
        });
    }
    
    private FlyingBroomEntity findBondedBroom(Level level, Player player, ICovenCapability coven) {
        for (FlyingBroomEntity entity : level.getEntitiesOfClass(FlyingBroomEntity.class, 
                player.getBoundingBox().inflate(1000))) {
            if (coven.getBondedBroomId().equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }
    
    private FlyingBroomEntity createAndPositionBroom(UseOnContext context) {
        FlyingBroomEntity broomEntity = new FlyingBroomEntity(EntityTypeRegistry.FLYING_BROOM.get(), context.getLevel());
        
        Vec3 clickPos = context.getClickLocation();
        broomEntity.setPos(clickPos.x, clickPos.y + 1.0, clickPos.z);
        broomEntity.setOwner(context.getPlayer());
        
        Vec3 broomPos = broomEntity.position().add(0, broomEntity.getEyeHeight(), 0);
        Vec3 targetLookDirection = broomPos.add(context.getPlayer().getLookAngle());
        broomEntity.lookAt(EntityAnchorArgument.Anchor.EYES, targetLookDirection);
        
        return broomEntity;
    }
    
    private void transferNBTToBroom(ItemStack stack, FlyingBroomEntity broomEntity) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            return;
        }
        
        if (nbt.contains("BrushTier")) {
            broomEntity.setBrushTier(nbt.getInt("BrushTier"));
        }
        if (nbt.contains("HandleWood")) {
            broomEntity.setHandleWood(new ResourceLocation(nbt.getString("HandleWood")));
        }
        if (nbt.contains("RibbonColor")) {
            broomEntity.setRibbonColor(nbt.getInt("RibbonColor"));
        }
        if (nbt.contains("StorageLevel")) {
            broomEntity.setStorageLevel(nbt.getInt("StorageLevel"));
        }
        if (nbt.contains("CharmInventory")) {
            broomEntity.getCharmHandler().deserializeNBT(nbt.getCompound("CharmInventory"));
        }
        if (nbt.contains("StorageInventory")) {
            broomEntity.getStorageHandler().deserializeNBT(nbt.getCompound("StorageInventory"));
        }
    }
    
    private void bondBroomToPlayer(Player player, FlyingBroomEntity broomEntity) {
        LazyOptional<ICovenCapability> cap = player.getCapability(CovenProvider.COVEN);
        cap.ifPresent(coven -> coven.setBondedBroomId(broomEntity.getUUID()));
    }
    
    private void consumeItem(ItemStack stack, Player player, UseOnContext context) {
        stack.shrink(1);
        if (player.isCreative()) {
            player.setItemInHand(context.getHand(), ItemStack.EMPTY);
        }
    }
}