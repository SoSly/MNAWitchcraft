package org.sosly.witchcraft.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.sosly.witchcraft.items.ItemRegistry;

public class FlyingBroomData {
    public static final int DEFAULT_BRUSH_TIER = 1;
    public static final ResourceLocation DEFAULT_HANDLE_WOOD = new ResourceLocation("minecraft:oak");
    public static final int DEFAULT_RIBBON_COLOR = 16383998;
    public static final int DEFAULT_STORAGE_LEVEL = 0;
    
    private static final String NBT_BRUSH_TIER = "BrushTier";
    private static final String NBT_HANDLE_WOOD = "HandleWood";
    private static final String NBT_RIBBON_COLOR = "RibbonColor";
    private static final String NBT_STORAGE_LEVEL = "StorageLevel";
    private static final String NBT_CHARM_INVENTORY = "CharmInventory";
    private static final String NBT_STORAGE_INVENTORY = "StorageInventory";

    private int brushTier;
    private ResourceLocation handleWood;
    private int ribbonColor;
    private int storageLevel;
    private ItemStackHandler charmHandler;
    private ItemStackHandler storageHandler;

    public FlyingBroomData() {
        this(DEFAULT_BRUSH_TIER, DEFAULT_HANDLE_WOOD, DEFAULT_RIBBON_COLOR, DEFAULT_STORAGE_LEVEL);
    }

    public FlyingBroomData(int brushTier, ResourceLocation handleWood, int ribbonColor, int storageLevel) {
        this.brushTier = brushTier;
        this.handleWood = handleWood;
        this.ribbonColor = ribbonColor;
        this.storageLevel = storageLevel;
        this.charmHandler = new ItemStackHandler(3);
        this.storageHandler = new ItemStackHandler(getSlotsForLevel(storageLevel));
    }

    public static FlyingBroomData fromNBT(CompoundTag nbt) {
        int brushTier = nbt.contains(NBT_BRUSH_TIER) ? nbt.getInt(NBT_BRUSH_TIER) : DEFAULT_BRUSH_TIER;
        ResourceLocation handleWood = nbt.contains(NBT_HANDLE_WOOD) ? 
            new ResourceLocation(nbt.getString(NBT_HANDLE_WOOD)) : DEFAULT_HANDLE_WOOD;
        int ribbonColor = nbt.contains(NBT_RIBBON_COLOR) ? nbt.getInt(NBT_RIBBON_COLOR) : DEFAULT_RIBBON_COLOR;
        int storageLevel = nbt.contains(NBT_STORAGE_LEVEL) ? nbt.getInt(NBT_STORAGE_LEVEL) : DEFAULT_STORAGE_LEVEL;
        
        FlyingBroomData data = new FlyingBroomData(brushTier, handleWood, ribbonColor, storageLevel);
        
        if (nbt.contains(NBT_CHARM_INVENTORY)) {
            data.charmHandler.deserializeNBT(nbt.getCompound(NBT_CHARM_INVENTORY));
        }
        if (nbt.contains(NBT_STORAGE_INVENTORY)) {
            data.storageHandler.deserializeNBT(nbt.getCompound(NBT_STORAGE_INVENTORY));
        }
        
        return data;
    }

    public static FlyingBroomData fromItemStack(ItemStack stack) {
        if (!stack.hasTag()) {
            return new FlyingBroomData();
        }
        return fromNBT(stack.getTag());
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt(NBT_BRUSH_TIER, this.brushTier);
        nbt.putString(NBT_HANDLE_WOOD, this.handleWood.toString());
        nbt.putInt(NBT_RIBBON_COLOR, this.ribbonColor);
        nbt.putInt(NBT_STORAGE_LEVEL, this.storageLevel);
        nbt.put(NBT_CHARM_INVENTORY, this.charmHandler.serializeNBT());
        nbt.put(NBT_STORAGE_INVENTORY, this.storageHandler.serializeNBT());
        return nbt;
    }

    public ItemStack toItemStack() {
        ItemStack stack = new ItemStack(ItemRegistry.FLYING_BROOM.get());
        stack.setTag(this.toNBT());
        return stack;
    }

    public FlyingBroomData applyDye(DyeColor dyeColor) {
        int blendedColor = blendColorsMinecraft(this.ribbonColor, dyeColor);
        return withNewColor(blendedColor);
    }

    public FlyingBroomData setHandleWood(Block strippedLog) {
        ResourceLocation woodType = getWoodTypeFromStrippedLog(strippedLog);
        return withNewWood(woodType);
    }

    public FlyingBroomData upgradeStorage(int newLevel) {
        if (newLevel == this.storageLevel) {
            return this;
        }
        
        FlyingBroomData upgraded = new FlyingBroomData(this.brushTier, this.handleWood, this.ribbonColor, newLevel);
        
        int itemsToCopy = Math.min(this.storageHandler.getSlots(), upgraded.storageHandler.getSlots());
        for (int i = 0; i < itemsToCopy; i++) {
            upgraded.storageHandler.setStackInSlot(i, this.storageHandler.getStackInSlot(i));
        }
        
        for (int i = 0; i < 3; i++) {
            upgraded.charmHandler.setStackInSlot(i, this.charmHandler.getStackInSlot(i));
        }
        
        return upgraded;
    }

    public FlyingBroomData washRibbon() {
        return withNewColor(DEFAULT_RIBBON_COLOR);
    }

    public FlyingBroomData withNewColor(int color) {
        FlyingBroomData copy = new FlyingBroomData(this.brushTier, this.handleWood, color, this.storageLevel);
        copyInventories(copy);
        return copy;
    }

    public FlyingBroomData withNewWood(ResourceLocation wood) {
        FlyingBroomData copy = new FlyingBroomData(this.brushTier, wood, this.ribbonColor, this.storageLevel);
        copyInventories(copy);
        return copy;
    }

    private void copyInventories(FlyingBroomData target) {
        for (int i = 0; i < 3; i++) {
            target.charmHandler.setStackInSlot(i, this.charmHandler.getStackInSlot(i));
        }
        int slots = Math.min(this.storageHandler.getSlots(), target.storageHandler.getSlots());
        for (int i = 0; i < slots; i++) {
            target.storageHandler.setStackInSlot(i, this.storageHandler.getStackInSlot(i));
        }
    }

    public static int getSlotsForLevel(int level) {
        return switch (level) {
            case 1 -> 9;
            case 2 -> 18;
            case 3 -> 27;
            default -> 0;
        };
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

    private static ResourceLocation getWoodTypeFromStrippedLog(Block strippedLog) {
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(strippedLog);
        if (blockId == null) {
            return DEFAULT_HANDLE_WOOD;
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
        
        return DEFAULT_HANDLE_WOOD;
    }

    public int getBrushTier() {
        return brushTier;
    }

    public ResourceLocation getHandleWood() {
        return handleWood;
    }

    public int getRibbonColor() {
        return ribbonColor;
    }

    public int getStorageLevel() {
        return storageLevel;
    }

    public ItemStackHandler getCharmHandler() {
        return charmHandler;
    }

    public ItemStackHandler getStorageHandler() {
        return storageHandler;
    }
}