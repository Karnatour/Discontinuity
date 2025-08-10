package me.pepperbell.continuity.client.processor;

import me.pepperbell.continuity.api.client.QuadProcessor;
import me.pepperbell.continuity.client.processor.simple.SimpleQuadProcessor;
import me.pepperbell.continuity.client.properties.StandardConnectingCTMProperties;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Supplier;

public class HorizontalQuadProcessor extends ConnectingQuadProcessor {
	// Indices for this array are formed from these bit values:
	// 1   *   2
	protected static final int[] SPRITE_INDEX_MAP = new int[] {
			3, 2, 0, 1,
	};

	public HorizontalQuadProcessor(TextureAtlasSprite[] sprites, ProcessingPredicate processingPredicate, ConnectionPredicate connectionPredicate, boolean innerSeams) {
		super(sprites, processingPredicate, connectionPredicate, innerSeams);
	}

	@Override
	public ProcessingResult processQuadInner(MutableQuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, int processorIndex, ProcessingContext context) {
		EnumFacing[] directions = DirectionMaps.getDirections(quad);
		BlockPos.MutableBlockPos mutablePos = context.getData(ProcessingDataKeys.MUTABLE_POS_KEY);
		int connections = getConnections(directions, mutablePos, blockView, state, pos, quad.lightFace(), sprite);
		TextureAtlasSprite newSprite = sprites[SPRITE_INDEX_MAP[connections]];
		return SimpleQuadProcessor.process(quad, sprite, newSprite);
	}

	protected int getConnections(EnumFacing[] directions, BlockPos.MutableBlockPos mutablePos, World blockView, IBlockState state, BlockPos pos, EnumFacing face, TextureAtlasSprite quadSprite) {
		int connections = 0;
		for (int i = 0; i < 2; i++) {
			mutablePos.setPos(pos).move(directions[i * 2]);
			if (connectionPredicate.shouldConnect(blockView, state, pos, mutablePos, face, quadSprite, innerSeams)) {
				connections |= 1 << i;
			}
		}
		return connections;
	}

	public static class Factory extends AbstractQuadProcessorFactory<StandardConnectingCTMProperties> {
		@Override
		public QuadProcessor createProcessor(StandardConnectingCTMProperties properties, TextureAtlasSprite[] sprites) {
			return new HorizontalQuadProcessor(sprites, BaseProcessingPredicate.fromProperties(properties), properties.getConnectionPredicate(), properties.getInnerSeams());
		}

		@Override
		public int getTextureAmount(StandardConnectingCTMProperties properties) {
			return 4;
		}
	}
}
