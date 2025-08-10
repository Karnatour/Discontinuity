package me.pepperbell.continuity.client.model;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CullingCache {
	protected final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

	protected int completionFlags;
	protected int resultFlags;

	public boolean shouldCull(World blockView, BlockPos pos, IBlockState state, EnumFacing cullFace) {
		int mask = 1 << cullFace.ordinal();
		if ((completionFlags & mask) == 0) {
			completionFlags |= mask;

			Block block = state.getBlock();
			BlockPos offsetPos = pos.offset(cullFace);
			boolean shouldRender = block.shouldSideBeRendered(state,blockView, offsetPos, cullFace);

			if (shouldRender) {
				resultFlags |= mask;
				return false;
			} else {
				return true;
			}
		} else {
			return (resultFlags & mask) == 0;
		}
	}

	public boolean shouldCull(QuadView quad, World blockView, BlockPos pos, IBlockState state) {
		EnumFacing cullFace = quad.cullFace();
		if (cullFace == null) {
			return false;
		}
		return shouldCull(blockView, pos, state, cullFace);
	}

	public void prepare() {
		completionFlags = 0;
		resultFlags = 0;
	}
}
