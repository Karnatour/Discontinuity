package me.pepperbell.continuity.client.processor;

import me.pepperbell.continuity.api.client.QuadProcessor;
import me.pepperbell.continuity.client.processor.simple.SimpleQuadProcessor;
import me.pepperbell.continuity.client.properties.StandardConnectingCTMProperties;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Supplier;

public class TopQuadProcessor extends ConnectingQuadProcessor {
	public TopQuadProcessor(TextureAtlasSprite[] sprites, ProcessingPredicate processingPredicate, ConnectionPredicate connectionPredicate, boolean innerSeams) {
		super(sprites, processingPredicate, connectionPredicate, innerSeams);
	}

	@Override
	public ProcessingResult processQuadInner(MutableQuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, int processorIndex, ProcessingContext context) {
		EnumFacing lightFace = quad.lightFace();
		EnumFacing.Axis axis = EnumFacing.Axis.Y;
		try {
			if (state.getBlock() instanceof BlockLog && state.getProperties().containsKey(BlockLog.LOG_AXIS)) {
				BlockLog.EnumAxis logAxis = state.getValue(BlockLog.LOG_AXIS);
				switch (logAxis) {
					case X:
						axis = EnumFacing.Axis.X;
						break;
					case Z:
						axis = EnumFacing.Axis.Z;
						break;
					case Y:
					case NONE:
					default:
						axis = EnumFacing.Axis.Y;
						break;
				}
			} else if (state.getBlock() instanceof BlockRotatedPillar && state.getProperties().containsKey(BlockRotatedPillar.AXIS)) {
				axis = state.getValue(BlockRotatedPillar.AXIS);
			}
		} catch (Exception e) {
			axis = EnumFacing.Axis.Y;
		}

		if (lightFace.getAxis() != axis) {
			EnumFacing up = EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE,axis);
			BlockPos.MutableBlockPos mutablePos = context.getData(ProcessingDataKeys.MUTABLE_POS_KEY).setPos(pos).move(up);
			if (connectionPredicate.shouldConnect(blockView, state, pos, mutablePos, lightFace, sprite, innerSeams)) {
				return SimpleQuadProcessor.process(quad, sprite, sprites[0]);
			}
		}
		return ProcessingResult.CONTINUE;
	}

	public static class Factory extends AbstractQuadProcessorFactory<StandardConnectingCTMProperties> {
		@Override
		public QuadProcessor createProcessor(StandardConnectingCTMProperties properties, TextureAtlasSprite[] sprites) {
			return new TopQuadProcessor(sprites, BaseProcessingPredicate.fromProperties(properties), properties.getConnectionPredicate(), properties.getInnerSeams());
		}

		@Override
		public int getTextureAmount(StandardConnectingCTMProperties properties) {
			return 1;
		}
	}
}
