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

public class VerticalHorizontalQuadProcessor extends VerticalQuadProcessor {
	// Indices for this array are formed from these bit values:
	// 32     16
	// 1   *   8
	// 2       4
	protected static final int[] SECONDARY_SPRITE_INDEX_MAP = new int[] {
			3, 6, 3, 3, 3, 6, 3, 3, 4, 5, 4, 4, 3, 6, 3, 3,
			3, 6, 3, 3, 3, 6, 3, 3, 3, 6, 3, 3, 3, 6, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 3, 3, 3, 3,
			3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
	};

	public VerticalHorizontalQuadProcessor(TextureAtlasSprite[] sprites, ProcessingPredicate processingPredicate, ConnectionPredicate connectionPredicate, boolean innerSeams) {
		super(sprites, processingPredicate, connectionPredicate, innerSeams);
	}

	@Override
	public ProcessingResult processQuadInner(MutableQuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, int processorIndex, ProcessingContext context) {
		EnumFacing[] directions = DirectionMaps.getDirections(quad);
		BlockPos.MutableBlockPos mutablePos = context.getData(ProcessingDataKeys.MUTABLE_POS_KEY);
		int connections = getConnections(directions, mutablePos, blockView, state, pos, quad.lightFace(), sprite);
		TextureAtlasSprite newSprite;
		if (connections != 0) {
			newSprite = sprites[SPRITE_INDEX_MAP[connections]];
		} else {
			int secondaryConnections = getSecondaryConnections(directions, mutablePos, blockView, state, pos, quad.lightFace(), sprite);
			newSprite = sprites[SECONDARY_SPRITE_INDEX_MAP[secondaryConnections]];
		}
		return SimpleQuadProcessor.process(quad, sprite, newSprite);
	}

	protected int getSecondaryConnections(EnumFacing[] directions, BlockPos.MutableBlockPos mutablePos, World blockView, IBlockState state, BlockPos pos, EnumFacing face, TextureAtlasSprite quadSprite) {
		int connections = 0;
		for (int i = 0; i < 2; i++) {
			EnumFacing direction = directions[i * 2];
			mutablePos.setPos(pos).move(direction);
			if (connectionPredicate.shouldConnect(blockView, state, pos, mutablePos, face, quadSprite, innerSeams)) {
				connections |= 1 << (i * 3);
				for (int j = 0; j < 2; j++) {
					mutablePos.setPos(pos).move(direction).move(directions[((i + j) % 2) * 2 + 1]);
					if (connectionPredicate.shouldConnect(blockView, state, pos, mutablePos, face, quadSprite, innerSeams)) {
						connections |= 1 << ((i * 3 + j * 2 + 5) % 6);
					}
				}
			}
		}
		return connections;
	}

	public static class Factory extends AbstractQuadProcessorFactory<StandardConnectingCTMProperties> {
		@Override
		public QuadProcessor createProcessor(StandardConnectingCTMProperties properties, TextureAtlasSprite[] sprites) {
			return new VerticalHorizontalQuadProcessor(sprites, BaseProcessingPredicate.fromProperties(properties), properties.getConnectionPredicate(), properties.getInnerSeams());
		}

		@Override
		public int getTextureAmount(StandardConnectingCTMProperties properties) {
			return 7;
		}
	}
}
