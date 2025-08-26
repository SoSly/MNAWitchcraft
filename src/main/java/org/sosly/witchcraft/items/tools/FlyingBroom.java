package org.sosly.witchcraft.items.tools;

import com.mna.api.items.TieredItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.api.capabilities.IBroomCapability;
import org.sosly.witchcraft.capabilities.broom.BroomProvider;
import org.sosly.witchcraft.data.FlyingBroomData;
import org.sosly.witchcraft.entities.EntityTypeRegistry;
import org.sosly.witchcraft.renderers.items.FlyingBroomItemRenderer;

import java.util.List;
import java.util.function.Consumer;

public class FlyingBroom extends TieredItem {
    public FlyingBroom() {
        super((new Item.Properties()).stacksTo(1));
    }
    
    
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return FlyingBroomItemRenderer.getInstance();
            }
        });
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        if (flag.isAdvanced()) {
            FlyingBroomData data = FlyingBroomData.fromItemStack(stack);
            String hexColor = String.format("#%06X", data.getRibbonColor());
            tooltip.add(Component.literal("Ribbon Color: " + hexColor).withStyle(net.minecraft.ChatFormatting.GRAY));
            tooltip.add(Component.literal("Handle Wood: " + data.getHandleWood()).withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        
        if (tryWashInCauldron(context, player, stack, level)) {
            return InteractionResult.SUCCESS;
        }
        
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        FlyingBroomData broomData = FlyingBroomData.fromItemStack(stack);
        
        cleanupExistingBroom(player, level);
        org.sosly.witchcraft.entities.tools.FlyingBroom broomEntity = createAndPositionBroom(context);
        broomEntity.setBroomData(broomData);
        bondBroomToPlayer(player, broomEntity);
        
        level.addFreshEntity(broomEntity);
        consumeItem(stack, player, context);
        
        return InteractionResult.SUCCESS;
    }
    
    private boolean tryWashInCauldron(UseOnContext context, Player player, ItemStack stack, Level level) {
        if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof LayeredCauldronBlock)) {
            return false;
        }
        
        FlyingBroomData data = FlyingBroomData.fromItemStack(stack);
        if (data.getRibbonColor() == FlyingBroomData.DEFAULT_RIBBON_COLOR) {
            return false;
        }
        
        if (!level.isClientSide) {
            FlyingBroomData washedData = data.washRibbon();
            stack.setTag(washedData.toNBT());
            LayeredCauldronBlock.lowerFillLevel(level.getBlockState(context.getClickedPos()), level, context.getClickedPos());
        }
        
        return true;
    }
    
    private void cleanupExistingBroom(Player player, Level level) {
        LazyOptional<IBroomCapability> cap = player.getCapability(BroomProvider.BROOM);
        cap.ifPresent(broom -> {
            if (!broom.hasBondedBroom()) {
                return;
            }
            
            org.sosly.witchcraft.entities.tools.FlyingBroom existingBroom = findBondedBroom(level, player, broom);
            if (existingBroom != null) {
                existingBroom.discard();
            }
            broom.setBondedBroomId(null);
        });
    }
    
    private org.sosly.witchcraft.entities.tools.FlyingBroom findBondedBroom(Level level, Player player, IBroomCapability broom) {
        for (org.sosly.witchcraft.entities.tools.FlyingBroom entity : level.getEntitiesOfClass(org.sosly.witchcraft.entities.tools.FlyingBroom.class,
                player.getBoundingBox().inflate(1000))) {
            if (broom.getBondedBroomId().equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }
    
    private org.sosly.witchcraft.entities.tools.FlyingBroom createAndPositionBroom(UseOnContext context) {
        org.sosly.witchcraft.entities.tools.FlyingBroom broomEntity = new org.sosly.witchcraft.entities.tools.FlyingBroom(EntityTypeRegistry.FLYING_BROOM.get(), context.getLevel());
        
        Vec3 clickPos = context.getClickLocation();
        broomEntity.setPos(clickPos.x, clickPos.y + 1.0, clickPos.z);
        broomEntity.setOwner(context.getPlayer());
        
        Vec3 broomPos = broomEntity.position().add(0, broomEntity.getEyeHeight(), 0);
        Vec3 targetLookDirection = broomPos.add(context.getPlayer().getLookAngle());
        broomEntity.lookAt(EntityAnchorArgument.Anchor.EYES, targetLookDirection);
        
        return broomEntity;
    }
    
    
    private void bondBroomToPlayer(Player player, org.sosly.witchcraft.entities.tools.FlyingBroom broomEntity) {
        LazyOptional<IBroomCapability> cap = player.getCapability(BroomProvider.BROOM);
        cap.ifPresent(broom -> {
            broom.setBondedBroomId(broomEntity.getUUID());
            broom.setBroomsUnlocked(true);
            broom.setLastKnownPosition(broomEntity.blockPosition());
            broom.setLastKnownDimension(broomEntity.level().dimension().location());
        });
    }
    
    private void consumeItem(ItemStack stack, Player player, UseOnContext context) {
        stack.shrink(1);
        if (player.isCreative()) {
            player.setItemInHand(context.getHand(), ItemStack.EMPTY);
        }
    }
    
    public static ItemStack dyeBroom(ItemStack broomStack, DyeColor dyeColor) {
        FlyingBroomData data = FlyingBroomData.fromItemStack(broomStack);
        return data.applyDye(dyeColor).toItemStack();
    }
    
    public static ItemStack changeHandleWood(ItemStack broomStack, Block strippedLog) {
        FlyingBroomData data = FlyingBroomData.fromItemStack(broomStack);
        return data.setHandleWood(strippedLog).toItemStack();
    }
}