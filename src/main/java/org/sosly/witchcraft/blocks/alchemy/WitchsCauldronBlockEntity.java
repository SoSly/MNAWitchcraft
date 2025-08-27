package org.sosly.witchcraft.blocks.alchemy;

import com.mojang.logging.LogUtils;
import com.mysticalchemy.config.BrewingConfig;
import com.mysticalchemy.init.RecipeInit;
import com.mysticalchemy.recipe.PotionIngredientRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.sosly.witchcraft.blocks.BlockRegistry;
import org.sosly.witchcraft.blocks.EntityRegistry;
import org.sosly.witchcraft.fluids.FluidRegistry;

import java.util.HashMap;
import java.util.Optional;

public class WitchsCauldronBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public enum FluidType {
        EMPTY(null),
        WATER(Fluids.WATER),
        MOONLIGHT(FluidRegistry.CONDENSED_MOONLIGHT_SOURCE.get());
        
        private final Fluid fluid;
        
        FluidType(Fluid fluid) {
            this.fluid = fluid;
        }
        
        public Fluid getFluid() {
            return fluid;
        }
        
        public static FluidType fromFluid(Fluid fluid) {
            if (fluid == Fluids.WATER) {
                return WATER;
            }
            if (fluid == FluidRegistry.CONDENSED_MOONLIGHT_SOURCE.get()) {
                return MOONLIGHT;
            }
            return EMPTY;
        }
    }
    
    private FluidType fluidType = FluidType.EMPTY;
    private int fluidLevel = 0;
    
    private static final int UPDATE_RATE = 10;
    public static final float MIN_TEMP = 0f;
    public static final float MAX_TEMP = 200f;
    public static final float BOIL_POINT = 100f;
    
    private static final HashMap<Block, Float> HEATERS = new HashMap<>();
    static {
        HEATERS.put(Blocks.CAMPFIRE, 1.0f);
        HEATERS.put(Blocks.FIRE, 2.0f);
        HEATERS.put(Blocks.LAVA, 5.0f);
        HEATERS.put(Blocks.ICE, -2.0f);
    }
    
    private float heat = MIN_TEMP;
    private float stir = 0;
    private Biome myBiome;
    
    public static final float ITEM_HEAT_LOSS = 25f;
    public static final int MAX_MAGNITUDE = 3;
    public static final int MAX_EFFECTS = 4;
    public static final int MAX_DURATION = 9600;
    
    private boolean isSplash = false;
    private boolean isLingering = false;
    private int duration = 600;
    private HashMap<MobEffect, Float> effectStrengths;
    private RecipeManager recipeManager;
    
    private long targetColor = 12345L;
    private long startColor = 12345L;
    private double infusePct = 1.0f;
    
    public WitchsCauldronBlockEntity(BlockPos pos, BlockState state) {
        super(EntityRegistry.WITCHS_CAULDRON.get(), pos, state);
        effectStrengths = new HashMap<MobEffect, Float>();
    }
    
    public FluidType getFluidType() {
        return fluidType;
    }
    
    public int getFluidLevel() {
        return fluidLevel;
    }
    
    public boolean isEmpty() {
        return fluidLevel == 0 || fluidType == FluidType.EMPTY;
    }
    
    public boolean canAcceptFluid(Fluid fluid) {
        if (isEmpty()) {
            return true;
        }
        FluidType incomingType = FluidType.fromFluid(fluid);
        return incomingType != FluidType.EMPTY && incomingType == fluidType && fluidLevel < 3;
    }
    
    public boolean fill(Fluid fluid) {
        if (!canAcceptFluid(fluid)) {
            return false;
        }
        
        boolean wasEmpty = isEmpty();
        
        if (isEmpty()) {
            fluidType = FluidType.fromFluid(fluid);
            fluidLevel = 1;
        } else {
            fluidLevel++;
        }
        
        if (wasEmpty && !isEmpty()) {
            switchToFilledCauldron();
        } else if (!wasEmpty) {
            updateBlockStateLevel();
        }
        
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }
    
    public boolean drain() {
        if (isEmpty()) {
            return false;
        }
        
        fluidLevel--;
        if (fluidLevel <= 0) {
            fluidType = FluidType.EMPTY;
            fluidLevel = 0;
            switchToEmptyCauldron();
        } else {
            updateBlockStateLevel();
        }
        
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }
    
    public void setFluid(FluidType type, int newLevel) {
        boolean wasEmpty = isEmpty();
        this.fluidType = type;
        this.fluidLevel = Math.max(0, Math.min(3, newLevel));
        
        if (type == FluidType.WATER) {
            targetColor = 12345L;
            startColor = 12345L;
        } else if (type == FluidType.MOONLIGHT) {
            targetColor = 0xC0D0E6L;
            startColor = 0xC0D0E6L;
        }
        infusePct = 1.0f;
        
        if (wasEmpty && !isEmpty()) {
            switchToFilledCauldron();
        } else if (!wasEmpty && !isEmpty()) {
            updateBlockStateLevel();
        }
        
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("FluidType", fluidType.name());
        tag.putInt("FluidLevel", fluidLevel);
        tag.putFloat("heat", heat);
        tag.putFloat("stir", stir);
        
        tag.putBoolean("splash", isSplash);
        tag.putBoolean("lingering", isLingering);
        tag.putInt("duration", duration);
        tag.putInt("numEffects", effectStrengths.size());
        
        int count = 0;
        for (MobEffect e : effectStrengths.keySet()) {
            tag.putString("effect" + count, ForgeRegistries.MOB_EFFECTS.getKey(e).toString());
            tag.putFloat("effectstr" + count, effectStrengths.get(e));
            count++;
        }
    }
    
    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        String fluidTypeString = tag.getString("FluidType");
        if (fluidTypeString.isEmpty()) {
            LOGGER.warn("WitchsCauldronBlockEntity at {} has empty FluidType in NBT, defaulting to EMPTY", worldPosition);
            fluidType = FluidType.EMPTY;
        } else {
            try {
                fluidType = FluidType.valueOf(fluidTypeString);
            } catch (IllegalArgumentException e) {
                LOGGER.error("WitchsCauldronBlockEntity at {} has invalid FluidType '{}' in NBT, defaulting to EMPTY", worldPosition, fluidTypeString);
                fluidType = FluidType.EMPTY;
            }
        }
        fluidLevel = tag.getInt("FluidLevel");
        if (tag.contains("heat")) {
            heat = tag.getFloat("heat");
        }
        if (tag.contains("stir")) {
            stir = tag.getFloat("stir");
        }
        
        if (tag.contains("splash")) {
            isSplash = tag.getBoolean("splash");
        }
        if (tag.contains("lingering")) {
            isLingering = tag.getBoolean("lingering");
        }
        if (tag.contains("duration")) {
            duration = tag.getInt("duration");
        }
        
        if (tag.contains("numEffects")) {
            int count = tag.getInt("numEffects");
            for (int i = 0; i < count; ++i) {
                if (!tag.contains("effect" + i) || !tag.contains("effectstr" + i)) {
                    continue;
                }
                
                String effectString = tag.getString("effect" + i);
                MobEffect e = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectString));
                if (e == null) {
                    LOGGER.warn("WitchsCauldronBlockEntity at {} failed to load effect '{}' from NBT", worldPosition, effectString);
                    continue;
                }
                
                effectStrengths.put(e, tag.getFloat("effectstr" + i));
            }
        }
        
        recalculatePotionColor();
    }
    
    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }
    
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.EMPTY;
    }
    
    public static void tick(Level level, BlockPos pos, BlockState state, WitchsCauldronBlockEntity blockEntity) {
        blockEntity.serverTick();
    }
    
    private void serverTick() {
        if (level.isClientSide && !isEmpty()) {
            spawnParticles();
        }
        
        infusePct = Mth.clamp(infusePct + 0.01f, 0, 1);
        
        if (level.getGameTime() % UPDATE_RATE != 0) {
            return;
        }
        if (myBiome == null) {
            myBiome = level.getBiome(worldPosition).get();
        }
        if (isEmpty()) {
            resetPotion();
            return;
        }
        if (!level.isClientSide) {
            tickHeatAndStir();
        }
    }
    
    private void tickHeatAndStir() {
        Block below = level.getBlockState(worldPosition.below()).getBlock();
        float preHeat = heat;
        
        if (HEATERS.containsKey(below)) {
            heat = Mth.clamp(heat + HEATERS.get(below), MIN_TEMP, MAX_TEMP);
        } else {
            heat = Mth.clamp(heat - (1 - myBiome.getBaseTemperature()) * 10, MIN_TEMP, MAX_TEMP);
        }
        
        if (stir > 0.25f) {
            stir = Mth.clamp(stir - 0.02f, 0, 1);
        } else {
            stir = Mth.clamp(stir - 0.005f, 0, 1);
        }
        
        if (heat >= BOIL_POINT && stir == 0 && Math.random() < 0.1f && !isEmpty() && fluidType == FluidType.WATER) {
            drain();
        }
        
        if (heat != preHeat) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
    
    
    private void spawnParticles() {
        if (fluidType == FluidType.MOONLIGHT && fluidLevel > 0) {
            if (!level.isDay() && level.canSeeSky(worldPosition.above()) && fluidLevel < 3) {
                if (Math.random() < 0.1) {
                    level.addParticle(ParticleTypes.END_ROD,
                        worldPosition.getX() + 0.5 - 0.3 + Math.random() * 0.6,
                        worldPosition.getY() + 0.375 + (0.1875 * fluidLevel),
                        worldPosition.getZ() + 0.5 - 0.3 + Math.random() * 0.6,
                        0, 0.01, 0);
                }
                
                for (int i = 0; i < 2; i++) {
                    if (Math.random() < 0.3) {
                        level.addParticle(ParticleTypes.END_ROD,
                            worldPosition.getX() + 0.2 + Math.random() * 0.6,
                            worldPosition.getY() + 1.5 + Math.random() * 0.5,
                            worldPosition.getZ() + 0.2 + Math.random() * 0.6,
                            0, -0.02, 0);
                    }
                }
            }
        }
        
        if (heat > BOIL_POINT) {
            int numBubbles = (int) Math.ceil(5f * ((heat - BOIL_POINT) / (MAX_TEMP - BOIL_POINT)));
            
            for (int i = 0; i < numBubbles; ++i) {
                level.addParticle(ParticleTypes.BUBBLE_POP, 
                    worldPosition.getX() + 0.5 - 0.3 + Math.random() * 0.6,
                    worldPosition.getY() + 0.375 + (0.1875 * fluidLevel),
                    worldPosition.getZ() + 0.5 - 0.3 + Math.random() * 0.6, 
                    0, 0, 0);
            }
            
            if (stir < 0.25f && fluidType == FluidType.WATER) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, 
                    worldPosition.getX() + 0.5 - 0.3 + Math.random() * 0.6,
                    worldPosition.getY() + 0.375 + (0.1875 * fluidLevel),
                    worldPosition.getZ() + 0.5 - 0.3 + Math.random() * 0.6, 
                    0, 0.01f + (0.25f - stir) * 0.25f, 0);
            }
        }
    }
    
    public float getHeat() {
        return heat;
    }
    
    public void stir() {
        stir = 1.0f;
    }
    
    private void resetPotion() {
        heat = MIN_TEMP;
        stir = 1.0f;
        isSplash = false;
        isLingering = false;
        duration = 600;
        effectStrengths.clear();
        infusePct = 1.0f;
        targetColor = 12345L;
    }
    
    public boolean tryAddIngredient(ItemStack stack) {
        if (infusePct != 1.0f || heat < BOIL_POINT || isEmpty()) {
            return false;
        }
        
        if (recipeManager == null) {
            recipeManager = level.getRecipeManager();
        }
        if (recipeManager == null) {
            LOGGER.error("WitchsCauldronBlockEntity at {} failed to get RecipeManager when trying to add ingredient", worldPosition);
            return false;
        }
        
        Optional<PotionIngredientRecipe> recipe = recipeManager
                .getRecipesFor(RecipeInit.POTION_RECIPE_TYPE.get(), createDummyCraftingInventory(stack), level).stream()
                .findFirst();
        
        if (recipe.isEmpty()) {
            return false;
        }
        
        PotionIngredientRecipe resolvedRecipe = recipe.get();
        
        if (!canMerge(resolvedRecipe, stack.getCount())) {
            return false;
        }
        
        if (resolvedRecipe.getMakesLingering()) {
            isLingering = true;
        }
        if (resolvedRecipe.getMakesSplash()) {
            isSplash = true;
        }
        if (resolvedRecipe.getDurationAdded() > 0) {
            duration += resolvedRecipe.getDurationAdded() * stack.getCount();
        }
        
        heat = Mth.clamp(heat - ITEM_HEAT_LOSS * stack.getCount(), MIN_TEMP, MAX_TEMP);
        
        stir = Math.max(stir, 0.5f);
        
        mergeEffects(resolvedRecipe.getEffects(), stack.getCount());
        recalculatePotionColor();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        return true;
    }
    
    private boolean canMerge(PotionIngredientRecipe recipe, int quantity) {
        HashMap<MobEffect, Float> prominent = getProminentEffects();
        
        for (MobEffect e : recipe.getEffects().keySet()) {
            if (e == null) {
                continue;
            }
            
            if (prominent.size() >= MAX_EFFECTS) {
                float currentMagnitude = effectStrengths.getOrDefault(e, 0f);
                float newMagnitude = currentMagnitude + recipe.getEffects().get(e) * quantity;
                if (!prominent.containsKey(e) && newMagnitude >= 1.0f) {
                    return false;
                }
            }
            
            float effectStrength = recipe.getEffects().get(e) * quantity;
            if (effectStrengths.containsKey(e)) {
                if (effectStrengths.get(e) + effectStrength > MAX_MAGNITUDE) {
                    return false;
                }
            } else if (effectStrength > MAX_MAGNITUDE) {
                return false;
            }
        }
        
        if (recipe.getDurationAdded() * quantity + duration > MAX_DURATION) {
            return false;
        }
        
        return true;
    }
    
    private void mergeEffects(HashMap<MobEffect, Float> effectList, int quantity) {
        for (MobEffect e : effectList.keySet()) {
            if (e == null) {
                continue;
            }
            
            ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(e);
            if (key != null && BrewingConfig.isEffectDisabled(key)) {
                continue;
            }
            
            float newStrength = effectList.get(e) * quantity;
            float currentStrength = effectStrengths.getOrDefault(e, 0f);
            effectStrengths.put(e, Math.min(currentStrength + newStrength, MAX_MAGNITUDE));
        }
    }
    
    private void recalculatePotionColor() {
        HashMap<MobEffect, Float> prominents = getProminentEffects();
        if (prominents.isEmpty()) {
            if (fluidType == FluidType.MOONLIGHT) {
                targetColor = 0xC0D0E6L;
            } else {
                targetColor = 12345L;
            }
            infusePct = 1.0f;
            return;
        }
        
        long color = 0;
        for (MobEffect e : prominents.keySet()) {
            color += e.getColor();
        }
        
        color /= prominents.size();
        if (targetColor == color) {
            return;
        }
        
        startColor = getPotionColor();
        targetColor = color;
        infusePct = 0.0f;
    }
    
    private CraftingContainer createDummyCraftingInventory(ItemStack stack) {
        CraftingContainer craftingInventory = new TransientCraftingContainer(new AbstractContainerMenu((MenuType<?>) null, -1) {
            @Override
            public boolean stillValid(Player playerIn) {
                return false;
            }
            
            @Override
            public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
                return ItemStack.EMPTY;
            }
        }, 1, 1);
        
        craftingInventory.setItem(0, stack);
        return craftingInventory;
    }
    
    public HashMap<MobEffect, Float> getProminentEffects() {
        HashMap<MobEffect, Float> effects = new HashMap<>();
        for (MobEffect e : effectStrengths.keySet()) {
            Float strength = effectStrengths.get(e);
            if (strength < 1.0f) {
                continue;
            }
            
            ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(e);
            if (key != null && BrewingConfig.isEffectDisabled(key)) {
                continue;
            }
            
            effects.put(e, strength);
        }
        return effects;
    }
    
    public boolean isPotion() {
        for (Float strength : effectStrengths.values()) {
            if (strength >= 1.0f) {
                return true;
            }
        }
        return false;
    }
    
    public long getPotionColor() {
        if (!isPotion() && effectStrengths.isEmpty()) {
            if (fluidType == FluidType.MOONLIGHT) {
                return 0xC0D0E6L;
            }
            return 12345L;
        }
        
        if (infusePct == 1.0f) {
            return targetColor;
        }
        
        int[] rgbStart = new int[] {
            (int) (startColor >> 16 & 0xff),
            (int) (startColor >> 8 & 0xff),
            (int) (startColor & 0xff)
        };
        
        int[] rgbTarget = new int[] {
            (int) (targetColor >> 16 & 0xff),
            (int) (targetColor >> 8 & 0xff),
            (int) (targetColor & 0xff)
        };
        
        int[] lerpColor = new int[3];
        for (int i = 0; i < 3; ++i) {
            lerpColor[i] = rgbStart[i] + (int)((rgbTarget[i] - rgbStart[i]) * infusePct);
        }
        
        long outputColor = 0;
        outputColor += lerpColor[0] << 16;
        outputColor += lerpColor[1] << 8;
        outputColor += lerpColor[2];
        
        return outputColor;
    }
    
    public boolean isSplash() {
        return isSplash;
    }
    
    public boolean isLingering() {
        return isLingering;
    }
    
    public int getDuration() {
        return duration;
    }
    
    @SuppressWarnings("unchecked")
    public HashMap<MobEffect, Float> getAllEffects() {
        return (HashMap<MobEffect, Float>) effectStrengths.clone();
    }
    
    private void switchToEmptyCauldron() {
        if (level == null) {
            LOGGER.error("WitchsCauldronBlockEntity at {} tried to switch to empty cauldron but level is null", worldPosition);
            return;
        }
        if (level.isClientSide) {
            return;
        }
        CauldronStateManager.switchToEmptyBlock(level, worldPosition, this);
    }
    
    private void switchToFilledCauldron() {
        if (level == null) {
            LOGGER.error("WitchsCauldronBlockEntity at {} tried to switch to filled cauldron but level is null", worldPosition);
            return;
        }
        if (level.isClientSide) {
            return;
        }
        CauldronStateManager.switchToFilledBlock(level, worldPosition, this, fluidType, fluidLevel);
    }
    
    public void setFluidLevel(int level) {
        this.fluidLevel = level;
    }
    
    public void updateBlockStateLevel() {
        if (level == null || level.isClientSide) {
            return;
        }
        
        BlockState currentState = level.getBlockState(worldPosition);
        
        boolean isMoonlightBlock = currentState.getBlock() instanceof MoonlightWitchsCauldronBlock;
        boolean shouldBeMoonlight = fluidType == FluidType.MOONLIGHT;
        
        if (isMoonlightBlock != shouldBeMoonlight) {
            switchToFilledCauldron();
            return;
        }
        
        CauldronStateManager.updateBlockLevel(level, worldPosition, currentState, fluidLevel);
    }
}