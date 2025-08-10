package me.pepperbell.continuity.client.processor;

import me.pepperbell.continuity.api.client.QuadProcessor;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Supplier;

public abstract class AbstractQuadProcessor implements QuadProcessor {
	protected TextureAtlasSprite[] sprites;
	protected ProcessingPredicate processingPredicate;

	public AbstractQuadProcessor(TextureAtlasSprite[] sprites, ProcessingPredicate processingPredicate) {
		this.sprites = sprites;
		this.processingPredicate = processingPredicate;
	}

	@Override
	public ProcessingResult processQuad(MutableQuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, int processorIndex, ProcessingContext context) {
		if (!processingPredicate.shouldProcessQuad(quad, sprite, blockView, state, pos, context)) {
			return ProcessingResult.CONTINUE;
		}
		return processQuadInner(quad, sprite, blockView, state, pos, randomSupplier, pass, processorIndex, context);
	}

	public abstract ProcessingResult processQuadInner(MutableQuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, int processorIndex, ProcessingContext context);
}
