package org.sosly.witchcraft.events;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.sosly.witchcraft.Witchcraft;
import org.sosly.witchcraft.items.ItemRegistry;
import org.sosly.witchcraft.blocks.BlockRegistry;
import org.sosly.witchcraft.blocks.alchemy.CondensedMoonlightCauldronBlock;

@Mod.EventBusSubscriber(modid = Witchcraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CauldronInteractions {
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            registerCondensedMoonlightInteractions();
        });
    }
    
    private static void registerCondensedMoonlightInteractions() {
        CauldronInteraction.EMPTY.put(ItemRegistry.CONDENSED_MOONLIGHT_BUCKET.get(), (state, level, pos, player, hand, stack) -> {
            if (level.isClientSide) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            
            BlockState moonlightCauldron = BlockRegistry.CONDENSED_MOONLIGHT_CAULDRON.get().defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, 3);
            level.setBlock(pos, moonlightCauldron, 3);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, moonlightCauldron));
            
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        
        CondensedMoonlightCauldronBlock.CONDENSED_MOONLIGHT_CAULDRON_INTERACTION.put(Items.GLASS_BOTTLE, (state, level, pos, player, hand, stack) -> {
            if (level.isClientSide) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, 
                new ItemStack(ItemRegistry.CONDENSED_MOONLIGHT_BOTTLE.get())));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            
            LayeredCauldronBlock.lowerFillLevel(state, level, pos);
            
            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        
        CondensedMoonlightCauldronBlock.CONDENSED_MOONLIGHT_CAULDRON_INTERACTION.put(Items.BUCKET, (state, level, pos, player, hand, stack) -> {
            if (level.isClientSide) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, 
                new ItemStack(ItemRegistry.CONDENSED_MOONLIGHT_BUCKET.get())));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        
        CauldronInteraction.EMPTY.put(ItemRegistry.CONDENSED_MOONLIGHT_BOTTLE.get(), (state, level, pos, player, hand, stack) -> {
            if (level.isClientSide) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            
            BlockState moonlightCauldron = BlockRegistry.CONDENSED_MOONLIGHT_CAULDRON.get().defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, 1);
            level.setBlock(pos, moonlightCauldron, 3);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, moonlightCauldron));
            
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        
        CondensedMoonlightCauldronBlock.CONDENSED_MOONLIGHT_CAULDRON_INTERACTION.put(ItemRegistry.CONDENSED_MOONLIGHT_BOTTLE.get(), (state, level, pos, player, hand, stack) -> {
            if (level.isClientSide) {
                return InteractionResult.PASS;
            }
            
            int currentLevel = state.getValue(LayeredCauldronBlock.LEVEL);
            if (currentLevel >= 3) {
                return InteractionResult.PASS;
            }
            
            BlockState newState = state.setValue(LayeredCauldronBlock.LEVEL, currentLevel + 1);
            level.setBlock(pos, newState, 3);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
            
            Item item = stack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
    }
}