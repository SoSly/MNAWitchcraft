package org.sosly.witchcraft.blocks.alchemy;

import com.mysticalchemy.crucible.BlockCrucible;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.sosly.witchcraft.blocks.EntityRegistry;
import org.sosly.witchcraft.fluids.FluidRegistry;
import org.sosly.witchcraft.items.ItemRegistry;
import com.mysticalchemy.init.ItemInit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.network.chat.Component;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all Witch's Cauldron variants.
 * 
 * The Witch's Cauldron system uses three separate blocks to handle different states:
 * - Empty: Can collect moonlight from the night sky
 * - Water: Contains water for brewing
 * - Moonlight: Contains moonlight for brewing and emits light
 * 
 * This architecture is necessary because Minecraft's LayeredCauldronBlock doesn't support
 * LEVEL = 0, so we need a separate block for the empty state.
 */
public abstract class AbstractWitchsCauldronBlock extends BlockCrucible implements EntityBlock {
    public static final IntegerProperty LEVEL = LayeredCauldronBlock.LEVEL;
    
    private static final VoxelShape INSIDE = box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape SHAPE = Shapes.or(
            box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D),
            box(2.0D, 3.0D, 2.0D, 14.0D, 4.0D, 14.0D),
            box(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 16.0D),
            box(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            box(2.0D, 0.0D, 0.0D, 14.0D, 16.0D, 2.0D),
            box(2.0D, 0.0D, 14.0D, 14.0D, 16.0D, 16.0D)
    );
    
    public AbstractWitchsCauldronBlock() {
        super();
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, getDefaultLevel()));
    }
    
    /**
     * Get the default LEVEL value for this cauldron variant.
     * Empty cauldrons don't use LEVEL, but we still need to provide a valid value.
     */
    protected int getDefaultLevel() {
        return 1;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }
    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return INSIDE;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WitchsCauldronBlockEntity(pos, state);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide) {
            return;
        }
        
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof WitchsCauldronBlockEntity cauldron)) {
            return;
        }
        
        if (cauldron.getHeat() < WitchsCauldronBlockEntity.BOIL_POINT) {
            return;
        }
        
        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            SoundEvents.LAVA_POP, SoundSource.BLOCKS,
            1.0f, (float) (0.8f + Math.random() * 0.4f), false);
    }
    
    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide) {
            return;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof WitchsCauldronBlockEntity cauldron)) {
            return;
        }
        
        int fluidLevel = cauldron.getFluidLevel();
        if (fluidLevel == 0) {
            return;
        }
        
        float fluidHeight = pos.getY() + (6.0F + 3 * fluidLevel) / 16.0F;
        if (entity.getY() > fluidHeight) {
            return;
        }
        
        if (entity instanceof net.minecraft.world.entity.item.ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (!cauldron.tryAddIngredient(stack)) {
                entity.push(-0.2 + Math.random() * 0.4, 1, -0.2 + Math.random() * 0.4);
                return;
            }
            
            level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 
                1.0f, (float) (0.8f + Math.random() * 0.4f));
            itemEntity.discard();
            return;
        }
        
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity)) {
            return;
        }
        if (cauldron.getHeat() <= WitchsCauldronBlockEntity.MAX_TEMP / 2) {
            return;
        }
        
        entity.hurt(level.damageSources().inFire(), 1);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        ItemStack itemStack = player.getItemInHand(hand);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if (!(blockEntity instanceof WitchsCauldronBlockEntity cauldron)) {
            return super.use(state, level, pos, player, hand, hit);
        }
        
        if (itemStack.getItem() instanceof com.mysticalchemy.crucible.ItemSamplingKit) {
            return handleSamplingKit(level, pos, cauldron, player, hand, itemStack);
        }
        
        if (cauldron.getFluidLevel() > 0 && cauldron.isPotion() && itemStack.is(Items.GLASS_BOTTLE)) {
            extractPotion(level, cauldron, player, hand, pos);
            return InteractionResult.SUCCESS;
        }
        
        if (cauldron.getFluidLevel() > 0 && cauldron.getFluidType() == WitchsCauldronBlockEntity.FluidType.MOONLIGHT && 
                itemStack.is(Items.GLASS_BOTTLE)) {
            ItemStack moonlightBottle = new ItemStack(ItemRegistry.CONDENSED_MOONLIGHT_BOTTLE.get());
            
            player.getItemInHand(hand).shrink(1);
            if (!player.addItem(moonlightBottle)) {
                player.drop(moonlightBottle, false);
            }
            
            cauldron.drain();
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        
        if (itemStack.is(ItemInit.SPOON.get()) && !cauldron.isEmpty()) {
            if (!player.isCreative()) {
                itemStack.hurt(1, level.random, (ServerPlayer) player);
            }
            cauldron.stir();
            level.playSound(null, pos, SoundEvents.PLAYER_SPLASH, SoundSource.BLOCKS, 
                1.0f, (float) (0.8f + Math.random() * 0.4f));
            level.sendBlockUpdated(pos, state, state, 3);
            return InteractionResult.SUCCESS;
        }
        
        if (itemStack.is(Items.WATER_BUCKET) && cauldron.canAcceptFluid(Fluids.WATER)) {
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            cauldron.setFluid(WitchsCauldronBlockEntity.FluidType.WATER, 3);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        
        if (itemStack.is(ItemRegistry.CONDENSED_MOONLIGHT_BUCKET.get()) && 
                cauldron.canAcceptFluid(FluidRegistry.CONDENSED_MOONLIGHT_SOURCE.get())) {
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));
            }
            cauldron.setFluid(WitchsCauldronBlockEntity.FluidType.MOONLIGHT, 3);
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }
        
        if (!itemStack.is(Items.BUCKET) || cauldron.isEmpty() || cauldron.isPotion() || cauldron.getFluidLevel() < 3) {
            return InteractionResult.PASS;
        }
        
        ItemStack filledBucket = cauldron.getFluidType() == WitchsCauldronBlockEntity.FluidType.WATER
                ? new ItemStack(Items.WATER_BUCKET)
                : new ItemStack(ItemRegistry.CONDENSED_MOONLIGHT_BUCKET.get());
        
        if (!player.getAbilities().instabuild) {
            itemStack.shrink(1);
            if (itemStack.isEmpty()) {
                player.setItemInHand(hand, filledBucket);
            } else if (!player.getInventory().add(filledBucket)) {
                player.drop(filledBucket, false);
            }
        }
        
        cauldron.setFluid(WitchsCauldronBlockEntity.FluidType.EMPTY, 0);
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }
    
    private void extractPotion(Level level, WitchsCauldronBlockEntity cauldron, Player player, InteractionHand hand, BlockPos pos) {
        HashMap<MobEffect, Float> prominentEffects = cauldron.getProminentEffects();
        if (prominentEffects.isEmpty()) {
            return;
        }
        
        ItemStack potionStack = createBasePotionStack(cauldron);
        
        List<MobEffectInstance> effectInstances = new ArrayList<>();
        for (MobEffect effect : prominentEffects.keySet()) {
            int amplifier = (int) Math.floor(prominentEffects.get(effect) - 1);
            int duration = effect.isInstantenous() ? 1 : cauldron.getDuration();
            effectInstances.add(new MobEffectInstance(effect, duration, amplifier));
        }
        
        PotionUtils.setPotion(potionStack, Potions.WATER);
        PotionUtils.setCustomEffects(potionStack, effectInstances);
        
        if (effectInstances.size() == 1) {
            potionStack.setHoverName(Component.translatable(effectInstances.get(0).getDescriptionId()));
        } else {
            potionStack.setHoverName(Component.translatable("item.mysticalchemy.concoction"));
        }
        
        player.getItemInHand(hand).shrink(1);
        if (!player.addItem(potionStack)) {
            player.drop(potionStack, false);
        }
        
        cauldron.drain();
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    
    private ItemStack createBasePotionStack(WitchsCauldronBlockEntity cauldron) {
        if (cauldron.isLingering()) {
            return new ItemStack(Items.LINGERING_POTION);
        }
        if (cauldron.isSplash()) {
            return new ItemStack(Items.SPLASH_POTION);
        }
        return new ItemStack(Items.POTION);
    }
    
    private InteractionResult handleSamplingKit(Level level, BlockPos pos, WitchsCauldronBlockEntity cauldron, 
                                                Player player, InteractionHand hand, ItemStack kitStack) {
        final float threshold;
        if (kitStack.getItem() == ItemInit.SIMPLE_SAMPLING_KIT.get()) {
            threshold = 1.0f;
        } else if (kitStack.getItem() == ItemInit.ADVANCED_SAMPLING_KIT.get()) {
            threshold = 0.1f;
        } else {
            threshold = 1.0f;
        }
        
        if (!player.isCreative()) {
            kitStack.shrink(1);
        }
        
        HashMap<MobEffect, Float> allEffects = cauldron.getAllEffects();
        
        if (allEffects.isEmpty()) {
            player.sendSystemMessage(Component.translatable("chat.mysticalchemy.no_effects"));
            return InteractionResult.SUCCESS;
        }
        
        if (allEffects.values().stream().noneMatch(f -> f >= threshold)) {
            player.sendSystemMessage(Component.translatable("chat.mysticalchemy.no_notable_effects"));
            return InteractionResult.SUCCESS;
        }
        
        allEffects.forEach((e, f) -> {
            if (f >= threshold) {
                Component ttc = Component.translatable("chat.mysticalchemy.format_effect", 
                        String.format("%.2f", f), e.getDisplayName().getString())
                        .withStyle(f >= 1 ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.DARK_RED);
                player.sendSystemMessage(ttc);
            }
        });
        
        return InteractionResult.SUCCESS;
    }
    
    /**
     * Get the BlockEntityTicker for this cauldron variant.
     * Each variant implements its own ticker behavior.
     */
    @Nullable
    @Override
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type);
    
    /**
     * Get the light emission level for this cauldron variant.
     * Override in variants that emit light (e.g., moonlight cauldron).
     */
    @Override
    public abstract int getLightEmission(BlockState state, BlockGetter level, BlockPos pos);
}