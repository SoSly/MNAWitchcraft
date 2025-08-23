package org.sosly.witchcraft.blocks.sympathy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.sosly.witchcraft.blocks.entities.BoundPoppetEntity;
import org.sosly.witchcraft.items.sympathy.BoundPoppetItem;

import java.util.UUID;


public class PoppetBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public PoppetBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.WATERLOGGED, false));
    }

    public PoppetBlock() {
        super(BlockBehaviour.Properties.of()
                .ignitedByLava()
                .mapColor(MapColor.WOOL)
                .sound(SoundType.WOOL)
                .strength(0.7F));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.WATERLOGGED);
        builder.add(FaceAttachedHorizontalDirectionalBlock.FACE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        switch (dir) {
            case NORTH, SOUTH:
                return Shapes.box(0.10f, 0f, 0.25f, 0.9f, 1.25f, 0.9f);
            case EAST, WEST:
                return Shapes.box(0.25f, 0f, 0.10f, 0.9f, 1.25f, 0.9f);
            default:
                return Shapes.block();
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
        return defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.WATERLOGGED, fluidState.getType().equals(Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos posFrom) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, newState, level, pos, posFrom);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        if (level.isClientSide() || player.isCreative()) {
            return;
        }
        
        BlockEntity blockEntity = level.getBlockEntity(pos);

        ResourceLocation key = ForgeRegistries.BLOCKS.getKey(this);
        if (key == null) {
            return;
        }
        
        Item boundPoppetItem = ForgeRegistries.ITEMS.getValue(key);
        ItemStack stack = new ItemStack(boundPoppetItem);

        if (!(blockEntity instanceof BoundPoppetEntity boundPoppet)) {
            Block.popResource(level, pos, stack);
            return;
        }
        
        UUID target = boundPoppet.target();
        String type = boundPoppet.type();
        if (target == null) {
            Block.popResource(level, pos, stack);
            return;
        }
        
        ((BoundPoppetItem)stack.getItem()).setTarget(level, target, type, stack);
        Block.popResource(level, pos, stack);
    }
}
