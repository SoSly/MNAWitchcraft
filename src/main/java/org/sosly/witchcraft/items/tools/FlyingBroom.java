package org.sosly.witchcraft.items.tools;

import com.mna.api.items.TieredItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.sosly.witchcraft.api.capabilities.ICovenCapability;
import org.sosly.witchcraft.capabilities.coven.CovenProvider;
import org.sosly.witchcraft.entities.EntityTypeRegistry;
import org.sosly.witchcraft.renderers.items.FlyingBroomItemRenderer;

import java.util.List;
import java.util.function.Consumer;

public class FlyingBroom extends TieredItem {
    public FlyingBroom() {
        super((new Item.Properties()).stacksTo(1));
    }
    
    private void ensureDefaultNBT(ItemStack stack) {
        if (stack.getTag() != null) {
            return;
        }
        
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("BrushTier", 1);
        nbt.putString("HandleWood", "minecraft:oak");
        nbt.putInt("RibbonColor", 16383998);
        nbt.putInt("StorageLevel", 0);
        stack.setTag(nbt);
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
            ensureDefaultNBT(stack);
            CompoundTag nbt = stack.getTag();
            if (nbt != null) {
                if (nbt.contains("RibbonColor")) {
                    int color = nbt.getInt("RibbonColor");
                    String hexColor = String.format("#%06X", color);
                    tooltip.add(Component.literal("Ribbon Color: " + hexColor).withStyle(net.minecraft.ChatFormatting.GRAY));
                }
                if (nbt.contains("HandleWood")) {
                    String woodType = nbt.getString("HandleWood");
                    tooltip.add(Component.literal("Handle Wood: " + woodType).withStyle(net.minecraft.ChatFormatting.GRAY));
                }
            }
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
        
        ensureDefaultNBT(stack);
        
        cleanupExistingBroom(player, level);
        org.sosly.witchcraft.entities.tools.FlyingBroom broomEntity = createAndPositionBroom(context);
        transferNBTToBroom(stack, broomEntity);
        bondBroomToPlayer(player, broomEntity);
        
        level.addFreshEntity(broomEntity);
        consumeItem(stack, player, context);
        
        return InteractionResult.SUCCESS;
    }
    
    private boolean tryWashInCauldron(UseOnContext context, Player player, ItemStack stack, Level level) {
        if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof LayeredCauldronBlock)) {
            return false;
        }
        
        ensureDefaultNBT(stack);
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains("RibbonColor")) {
            return false;
        }
        
        int currentColor = nbt.getInt("RibbonColor");
        if (currentColor == 16383998) {
            return false;
        }
        
        if (!level.isClientSide) {
            nbt.putInt("RibbonColor", 16383998);
            LayeredCauldronBlock.lowerFillLevel(level.getBlockState(context.getClickedPos()), level, context.getClickedPos());
        }
        
        return true;
    }
    
    private void cleanupExistingBroom(Player player, Level level) {
        LazyOptional<ICovenCapability> cap = player.getCapability(CovenProvider.COVEN);
        cap.ifPresent(coven -> {
            if (!coven.hasBondedBroom()) {
                return;
            }
            
            org.sosly.witchcraft.entities.tools.FlyingBroom existingBroom = findBondedBroom(level, player, coven);
            if (existingBroom != null) {
                existingBroom.discard();
            }
            coven.setBondedBroomId(null);
        });
    }
    
    private org.sosly.witchcraft.entities.tools.FlyingBroom findBondedBroom(Level level, Player player, ICovenCapability coven) {
        for (org.sosly.witchcraft.entities.tools.FlyingBroom entity : level.getEntitiesOfClass(org.sosly.witchcraft.entities.tools.FlyingBroom.class,
                player.getBoundingBox().inflate(1000))) {
            if (coven.getBondedBroomId().equals(entity.getUUID())) {
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
    
    private void transferNBTToBroom(ItemStack stack, org.sosly.witchcraft.entities.tools.FlyingBroom broomEntity) {
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
    
    private void bondBroomToPlayer(Player player, org.sosly.witchcraft.entities.tools.FlyingBroom broomEntity) {
        LazyOptional<ICovenCapability> cap = player.getCapability(CovenProvider.COVEN);
        cap.ifPresent(coven -> {
            coven.setBondedBroomId(broomEntity.getUUID());
            coven.setBroomsUnlocked(true);
        });
    }
    
    private void consumeItem(ItemStack stack, Player player, UseOnContext context) {
        stack.shrink(1);
        if (player.isCreative()) {
            player.setItemInHand(context.getHand(), ItemStack.EMPTY);
        }
    }
    
    public static ItemStack dyeBroom(ItemStack broomStack, DyeColor dyeColor) {
        ItemStack result = broomStack.copy();
        ensureDefaultNBTStatic(result);
        
        CompoundTag nbt = result.getOrCreateTag();
        int currentColor = nbt.getInt("RibbonColor");
        
        int blendedColor = blendColorsMinecraft(currentColor, dyeColor);
        nbt.putInt("RibbonColor", blendedColor);
        
        return result;
    }
    
    public static ItemStack changeHandleWood(ItemStack broomStack, Block strippedLog) {
        ItemStack result = broomStack.copy();
        ensureDefaultNBTStatic(result);
        
        CompoundTag nbt = result.getOrCreateTag();
        ResourceLocation woodType = getWoodTypeFromStrippedLog(strippedLog);
        nbt.putString("HandleWood", woodType.toString());
        
        return result;
    }
    
    private static ResourceLocation getWoodTypeFromStrippedLog(Block strippedLog) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(strippedLog);
        if (blockId == null) {
            return new ResourceLocation("minecraft:oak");
        }
        
        String blockPath = blockId.getPath();
        if (blockPath.startsWith("stripped_") && (blockPath.endsWith("_log") || blockPath.endsWith("_stem"))) {
            String woodType = blockPath.substring(9);
            if (woodType.endsWith("_log")) {
                woodType = woodType.substring(0, woodType.length() - 4);
            } else if (woodType.endsWith("_stem")) {
                woodType = woodType.substring(0, woodType.length() - 5);
            }
            return new ResourceLocation(blockId.getNamespace(), woodType);
        }
        
        if (blockPath.equals("stripped_bamboo_block")) {
            return new ResourceLocation(blockId.getNamespace(), "bamboo");
        }
        
        return new ResourceLocation("minecraft:oak");
    }
    
    private static void ensureDefaultNBTStatic(ItemStack stack) {
        if (stack.getTag() != null) {
            return;
        }
        
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("BrushTier", 1);
        nbt.putString("HandleWood", "minecraft:oak");
        nbt.putInt("RibbonColor", 16383998);
        nbt.putInt("StorageLevel", 0);
        stack.setTag(nbt);
    }
    
    private static int blendColorsMinecraft(int currentColor, DyeColor dyeColor) {
        int[] colorComponents = new int[3];
        int maxBrightness = 0;
        int totalComponents = 0;
        
        if (currentColor != 0) {
            float currentRed = (float)(currentColor >> 16 & 255) / 255.0F;
            float currentGreen = (float)(currentColor >> 8 & 255) / 255.0F;
            float currentBlue = (float)(currentColor & 255) / 255.0F;
            
            maxBrightness += (int)(Math.max(currentRed, Math.max(currentGreen, currentBlue)) * 255.0F);
            colorComponents[0] += (int)(currentRed * 255.0F);
            colorComponents[1] += (int)(currentGreen * 255.0F);
            colorComponents[2] += (int)(currentBlue * 255.0F);
            totalComponents++;
        }
        
        float[] dyeColors = dyeColor.getTextureDiffuseColors();
        int dyeRed = (int)(dyeColors[0] * 255.0F);
        int dyeGreen = (int)(dyeColors[1] * 255.0F);
        int dyeBlue = (int)(dyeColors[2] * 255.0F);
        
        maxBrightness += Math.max(dyeRed, Math.max(dyeGreen, dyeBlue));
        colorComponents[0] += dyeRed;
        colorComponents[1] += dyeGreen;
        colorComponents[2] += dyeBlue;
        totalComponents++;
        
        int avgRed = colorComponents[0] / totalComponents;
        int avgGreen = colorComponents[1] / totalComponents;
        int avgBlue = colorComponents[2] / totalComponents;
        
        float brightness = (float)maxBrightness / (float)totalComponents;
        float maxComponent = (float)Math.max(avgRed, Math.max(avgGreen, avgBlue));
        
        avgRed = (int)((float)avgRed * brightness / maxComponent);
        avgGreen = (int)((float)avgGreen * brightness / maxComponent);
        avgBlue = (int)((float)avgBlue * brightness / maxComponent);
        
        return (avgRed << 16) | (avgGreen << 8) | avgBlue;
    }
}