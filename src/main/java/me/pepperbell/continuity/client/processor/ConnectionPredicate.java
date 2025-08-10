package me.pepperbell.continuity.client.processor;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ConnectionPredicate {
	boolean shouldConnect(World blockView, IBlockState state, BlockPos pos, IBlockState toState, EnumFacing face, TextureAtlasSprite quadSprite);

	default boolean shouldConnect(World blockView, IBlockState state, BlockPos pos, BlockPos toPos, EnumFacing face, TextureAtlasSprite quadSprite) {
		return shouldConnect(blockView, state, pos, blockView.getBlockState(toPos), face, quadSprite);
	}

	default boolean shouldConnect(World blockView, IBlockState state, BlockPos pos, BlockPos.MutableBlockPos toPos, EnumFacing face, TextureAtlasSprite quadSprite, boolean innerSeams) {
		if (shouldConnect(blockView, state, pos, toPos, face, quadSprite)) {
			if (innerSeams) {
				toPos.move(face);
				return !shouldConnect(blockView, state, pos, toPos, face, quadSprite);
			} else {
				return true;
			}
		}
		return false;
	}
}
