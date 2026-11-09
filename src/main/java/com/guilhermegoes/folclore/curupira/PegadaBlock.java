package com.guilhermegoes.folclore.curupira;

import com.guilhermegoes.folclore.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A footprint of the Curupira pressed into the ground. Its toes point the way he came from, never
 * the way he went: whoever follows the trail walks away from him. It fades on its own after a while
 * and is never dropped as an item.
 */
public class PegadaBlock extends Block {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 0.25);
	/** How long a footprint lasts, in ticks. */
	public static final int DURACAO = 20 * 40;

	public PegadaBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos baixo = pos.below();
		return level.getBlockState(baixo).isFaceSturdy(level, baixo, Direction.UP);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction direction, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		return !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		level.removeBlock(pos, false);
	}

	/**
	 * Presses a footprint at {@code pos} with its toes pointing {@code dedos}, if the spot is free
	 * air over firm ground. Returns whether it was placed.
	 */
	public static boolean marcar(ServerLevel level, BlockPos pos, Direction dedos) {
		// Air, or grass and small plants that the foot presses down (never water).
		BlockState aqui = level.getBlockState(pos);
		if (!aqui.isAir() && !(aqui.canBeReplaced() && aqui.getFluidState().isEmpty() && !aqui.is(ModBlocks.PEGADA))) {
			return false;
		}
		BlockState pegada = ModBlocks.PEGADA.defaultBlockState().setValue(FACING, dedos);
		if (!pegada.canSurvive(level, pos)) {
			return false;
		}
		level.setBlock(pos, pegada, Block.UPDATE_ALL);
		level.scheduleTick(pos, ModBlocks.PEGADA, DURACAO + level.getRandom().nextInt(200));
		return true;
	}
}
