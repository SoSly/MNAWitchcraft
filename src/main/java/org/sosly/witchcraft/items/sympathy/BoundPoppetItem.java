package org.sosly.witchcraft.items.sympathy;

import com.mna.items.ritual.PlayerCharm;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class BoundPoppetItem extends PlayerCharm {
    private final Block block;

    public BoundPoppetItem(Block block) {
        super();
        this.block = block;
    }

    public InteractionResult useOn(UseOnContext pContext) {
        InteractionResult interactionresult = this.place(new BlockPlaceContext(pContext));
        if (interactionresult.consumesAction() || !this.isEdible()) {
            return interactionresult;
        }
        
        InteractionResult interactionresult1 = this.use(pContext.getLevel(), pContext.getPlayer(), pContext.getHand()).getResult();
        return interactionresult1 == InteractionResult.CONSUME ? InteractionResult.CONSUME_PARTIAL : interactionresult1;
    }

    public InteractionResult place(BlockPlaceContext pContext) {
        if (!this.getBlock().isEnabled(pContext.getLevel().enabledFeatures())) {
            return InteractionResult.FAIL;
        }
        
        if (!pContext.canPlace()) {
            return InteractionResult.FAIL;
        }
        
        BlockPlaceContext blockplacecontext = this.updatePlacementContext(pContext);
        if (blockplacecontext == null) {
            return InteractionResult.FAIL;
        }
        
        BlockState blockstate = this.getPlacementState(blockplacecontext);
        if (blockstate == null) {
            return InteractionResult.FAIL;
        }
        
        if (!this.placeBlock(blockplacecontext, blockstate)) {
            return InteractionResult.FAIL;
        }
        
        BlockPos blockpos = blockplacecontext.getClickedPos();
        Level level = blockplacecontext.getLevel();
        Player player = blockplacecontext.getPlayer();
        ItemStack itemstack = blockplacecontext.getItemInHand();
        BlockState blockstate1 = level.getBlockState(blockpos);
        if (blockstate1.is(blockstate.getBlock())) {
            blockstate1 = this.updateBlockStateFromTag(blockpos, level, itemstack, blockstate1);
            this.updateCustomBlockEntityTag(blockpos, level, player, itemstack, blockstate1);
            blockstate1.getBlock().setPlacedBy(level, blockpos, blockstate1, player, itemstack);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, blockpos, itemstack);
            }
        }

        SoundType soundtype = blockstate1.getSoundType(level, blockpos, pContext.getPlayer());
        level.playSound(player, blockpos, this.getPlaceSound(blockstate1, level, blockpos, pContext.getPlayer()), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(player, blockstate1));
        if (player == null || !player.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected boolean canPlace(BlockPlaceContext pContext, BlockState pState) {
        Player player = pContext.getPlayer();
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return (!this.mustSurvive() || pState.canSurvive(pContext.getLevel(), pContext.getClickedPos())) && pContext.getLevel().isUnobstructed(pState, pContext.getClickedPos(), collisioncontext);
    }

    protected boolean mustSurvive() {
        return true;
    }

    public Block getBlock() {
        return this.getBlockRaw() == null ? null : net.minecraftforge.registries.ForgeRegistries.BLOCKS.getDelegateOrThrow(this.getBlockRaw()).get();
    }

    @Nullable
    public static CompoundTag getBlockEntityData(ItemStack pStack) {
        return pStack.getTagElement("BlockEntityTag");
    }

    private Block getBlockRaw() {
        return this.block;
    }

    protected SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
        return state.getSoundType(world, pos, entity).getPlaceSound();
    }

    protected BlockState getPlacementState(BlockPlaceContext pContext) {
        BlockState blockstate = this.getBlock().getStateForPlacement(pContext);
        return blockstate != null && this.canPlace(pContext, blockstate) ? blockstate : null;
    }

    protected boolean placeBlock(BlockPlaceContext pContext, BlockState pState) {
        return pContext.getLevel().setBlock(pContext.getClickedPos(), pState, 11);
    }

    private BlockState updateBlockStateFromTag(BlockPos pPos, Level pLevel, ItemStack pStack, BlockState pState) {
        BlockState blockstate = pState;
        CompoundTag compoundtag = pStack.getTag();
        if (compoundtag != null) {
            CompoundTag compoundtag1 = compoundtag.getCompound("BlockStateTag");
            StateDefinition<Block, BlockState> statedefinition = pState.getBlock().getStateDefinition();

            for(String s : compoundtag1.getAllKeys()) {
                Property<?> property = statedefinition.getProperty(s);
                if (property != null) {
                    String s1 = compoundtag1.get(s).getAsString();
                    blockstate = updateState(blockstate, property, s1);
                }
            }
        }

        if (blockstate != pState) {
            pLevel.setBlock(pPos, blockstate, 2);
        }

        return blockstate;
    }

    protected boolean updateCustomBlockEntityTag(BlockPos pPos, Level pLevel, @Nullable Player pPlayer, ItemStack pStack, BlockState pState) {
        return updateCustomBlockEntityTag(pLevel, pPlayer, pPos, pStack);
    }

    public static boolean updateCustomBlockEntityTag(Level pLevel, @Nullable Player pPlayer, BlockPos pPos, ItemStack pStack) {
        MinecraftServer minecraftserver = pLevel.getServer();
        if (minecraftserver == null) {
            return false;
        }
        
        CompoundTag compoundtag = getBlockEntityData(pStack);
        if (compoundtag == null) {
            return false;
        }
        
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity == null) {
            return false;
        }
        
        if (!pLevel.isClientSide && blockentity.onlyOpCanSetNbt() && (pPlayer == null || !pPlayer.canUseGameMasterBlocks())) {
            return false;
        }

        CompoundTag compoundtag1 = blockentity.saveWithoutMetadata();
        CompoundTag compoundtag2 = compoundtag1.copy();
        compoundtag1.merge(compoundtag);
        if (!compoundtag1.equals(compoundtag2)) {
            blockentity.load(compoundtag1);
            blockentity.setChanged();
            return true;
        }
        
        return false;
    }

    public BlockPlaceContext updatePlacementContext(BlockPlaceContext pContext) {
        return pContext;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState pState, Property<T> pProperty, String pValueIdentifier) {
        return pProperty.getValue(pValueIdentifier).map((p_40592_) -> {
            return pState.setValue(pProperty, p_40592_);
        }).orElse(pState);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public void setTarget(Level level, UUID target, String type, ItemStack stack) {
        stack.getOrCreateTag().putUUID("target", target);
        stack.getOrCreateTag().putString("type", type);

        Player player = level.getPlayerByUUID(target);
        if (player == null) {
            return;
        }

        this.SetPlayerTarget(player, stack);
    }

    public Entity getTarget(ItemStack stack, Level level) {
        Player player = this.GetPlayerTarget(stack, level);
        if (player != null) {
            return player;
        }

        if (level.isClientSide()) {
            return null;
        }

        UUID target = stack.getOrCreateTag().getUUID("target");
        return ((ServerLevel)level).getEntity(target);
    }

    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null) {
            tooltip.add(Component.translatable("item.mnaw.bound_poppet/not_bound").withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.ITALIC));
            return;
        }
        
        Entity target = this.getTarget(stack, level);
        if (target instanceof Player player) {
            tooltip.add(Component.translatable("item.mnaw.bound_poppet/player", player.getDisplayName()).withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.ITALIC));
            return;
        }
        
        tooltip.add(Component.translatable("item.mnaw.bound_poppet/mob", nbt.getString("type")).withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.ITALIC));
    }
}
