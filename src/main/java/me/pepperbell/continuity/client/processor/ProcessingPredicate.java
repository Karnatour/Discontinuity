package me.pepperbell.continuity.client.processor;

import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ProcessingPredicate {
	boolean shouldProcessQuad(QuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, ProcessingDataProvider dataProvider);
}
