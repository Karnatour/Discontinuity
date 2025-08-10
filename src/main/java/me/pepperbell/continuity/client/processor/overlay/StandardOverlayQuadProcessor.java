package me.pepperbell.continuity.client.processor.overlay;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import me.pepperbell.continuity.api.client.QuadProcessor;
import me.pepperbell.continuity.client.processor.*;
import me.pepperbell.continuity.client.properties.overlay.OverlayPropertiesSection;
import me.pepperbell.continuity.client.properties.overlay.StandardOverlayCTMProperties;
import me.pepperbell.continuity.client.util.QuadUtil;
import me.pepperbell.continuity.client.util.RenderUtil;
import me.pepperbell.continuity.client.util.SpriteCalculator;
import me.pepperbell.continuity.client.util.TextureUtil;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StandardOverlayQuadProcessor extends AbstractQuadProcessor {
	protected Set<ResourceLocation> matchTilesSet;
	protected Predicate<IBlockState> matchBlocksPredicate;
	protected Set<ResourceLocation> connectTilesSet;
	protected Predicate<IBlockState> connectBlocksPredicate;
	protected ConnectionPredicate connectionPredicate;

	protected int tintIndex;
	protected IBlockState tintBlock;
	protected RenderMaterial material;

	public StandardOverlayQuadProcessor(TextureAtlasSprite[] sprites, ProcessingPredicate processingPredicate, Set<ResourceLocation> matchTilesSet, Predicate<IBlockState> matchBlocksPredicate, Set<ResourceLocation> connectTilesSet, Predicate<IBlockState> connectBlocksPredicate, ConnectionPredicate connectionPredicate, int tintIndex, IBlockState tintBlock, BlendMode layer) {
		super(sprites, processingPredicate);
		this.matchTilesSet = matchTilesSet;
		this.matchBlocksPredicate = matchBlocksPredicate;
		this.connectTilesSet = connectTilesSet;
		this.connectBlocksPredicate = connectBlocksPredicate;
		this.connectionPredicate = connectionPredicate;

		this.tintIndex = tintIndex;
		this.tintBlock = tintBlock;
		material = RenderUtil.getMaterialFinder().blendMode(0, layer.blockRenderLayer).find();
	}

	@Override
	public ProcessingResult processQuadInner(MutableQuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, int processorIndex, ProcessingContext context) {
		EnumFacing lightFace = quad.lightFace();
		OverlayRenderer renderer = getRenderer(blockView, pos, state, lightFace, sprite, DirectionMaps.getMap(lightFace)[0], context);
		if (renderer != null) {
			context.addEmitterConsumer(renderer);
		}
		return ProcessingResult.CONTINUE;
	}

	protected boolean appliesOverlay(IBlockState other, World blockView, IBlockState state, BlockPos pos, EnumFacing face, TextureAtlasSprite quadSprite) {
		if (connectBlocksPredicate != null) {
			if (!connectBlocksPredicate.test(other)) {
				return false;
			}
		}
		if (matchTilesSet != null) {
			String iconName = SpriteCalculator.getSprite(other, face).getIconName();
			ResourceLocation iconLocation = new ResourceLocation(iconName);
			if (!matchTilesSet.contains(iconLocation)) {
				return false;
			}
		}
		return !connectionPredicate.shouldConnect(blockView, state, pos, other, face, quadSprite);
	}

	protected boolean hasSameOverlay(IBlockState other, World blockView, IBlockState state, BlockPos pos, EnumFacing face, TextureAtlasSprite quadSprite) {
		if (matchBlocksPredicate != null) {
			if (!matchBlocksPredicate.test(other)) {
				return false;
			}
		}
		if (matchTilesSet != null) {
			String iconName = SpriteCalculator.getSprite(other, face).getIconName();
			ResourceLocation iconLocation = new ResourceLocation(iconName);
			if (!matchTilesSet.contains(iconLocation)) {
				return false;
			}
		}
		return true;
	}

	protected boolean appliesOverlayUnobscured(IBlockState state0, EnumFacing direction0, World blockView, BlockPos pos, IBlockState state, EnumFacing lightFace, TextureAtlasSprite quadSprite, BlockPos.MutableBlockPos mutablePos) {
		boolean a0 = appliesOverlay(state0, blockView, state, pos, lightFace, quadSprite);
		if (a0) {
			mutablePos.setPos(pos).move(direction0).move(lightFace);
			a0 = !blockView.getBlockState(mutablePos).isOpaqueCube();
		}
		return a0;
	}

	protected boolean hasSameOverlayUnobscured(IBlockState state0, EnumFacing direction0, World blockView, BlockPos pos, IBlockState state, EnumFacing lightFace, TextureAtlasSprite quadSprite, BlockPos.MutableBlockPos mutablePos) {
		boolean s0 = hasSameOverlay(state0, blockView, state, pos, lightFace, quadSprite);
		if (s0) {
			mutablePos.setPos(pos).move(direction0).move(lightFace);
			s0 = !blockView.getBlockState(mutablePos).isOpaqueCube();
		}
		return s0;
	}

	protected boolean appliesOverlayCorner(EnumFacing direction0, EnumFacing direction1, World blockView, BlockPos pos, IBlockState state, EnumFacing lightFace, TextureAtlasSprite quadSprite, BlockPos.MutableBlockPos mutablePos) {
		mutablePos.setPos(pos).move(direction0).move(direction1);
		boolean corner0 = appliesOverlay(blockView.getBlockState(mutablePos), blockView, state, pos, lightFace, quadSprite);
		if (corner0) {
			mutablePos.move(lightFace);
			corner0 = !blockView.getBlockState(mutablePos).isOpaqueCube();
		}
		return corner0;
	}

	protected OverlayRenderer fromCorner(EnumFacing direction0, EnumFacing direction1, int sprite0, int sprite1, OverlayRenderer renderer, World blockView, BlockPos pos, IBlockState state, EnumFacing lightFace, TextureAtlasSprite quadSprite, BlockPos.MutableBlockPos mutablePos) {
		TextureAtlasSprite[] rendererSprites = prepareRenderer(renderer, lightFace, blockView, pos);
		mutablePos.setPos(pos).move(direction0).move(direction1);
		if (appliesOverlay(blockView.getBlockState(mutablePos), blockView, state, pos, lightFace, quadSprite)) {
			mutablePos.move(lightFace);
			if (!blockView.getBlockState(mutablePos).isOpaqueCube()) {
				rendererSprites[1] = sprites[sprite1];
			}
		}
		rendererSprites[0] = sprites[sprite0];
		return renderer;
	}

	protected OverlayRenderer fromOneSide(IBlockState state0, IBlockState state1, IBlockState state2, EnumFacing direction0, EnumFacing direction1, EnumFacing direction2, int sprite0, int sprite1, int sprite2, OverlayRenderer renderer, World blockView, BlockPos pos, IBlockState state, EnumFacing lightFace, TextureAtlasSprite quadSprite, BlockPos.MutableBlockPos mutablePos) {
		boolean s0 = hasSameOverlayUnobscured(state0, direction0, blockView, pos, state, lightFace, quadSprite, mutablePos);
		boolean s1 = hasSameOverlayUnobscured(state1, direction1, blockView, pos, state, lightFace, quadSprite, mutablePos);
		boolean s2 = hasSameOverlayUnobscured(state2, direction2, blockView, pos, state, lightFace, quadSprite, mutablePos);

		TextureAtlasSprite[] rendererSprites = prepareRenderer(renderer, lightFace, blockView, pos);
		rendererSprites[0] = sprites[sprite0];
		if (s0 | s1) {
			if (appliesOverlayCorner(direction0, direction1, blockView, pos, state, lightFace, quadSprite, mutablePos)) {
				rendererSprites[1] = sprites[sprite1];
			}
		}
		if (s1 | s2) {
			if (appliesOverlayCorner(direction1, direction2, blockView, pos, state, lightFace, quadSprite, mutablePos)) {
				rendererSprites[2] = sprites[sprite2];
			}
		}
		return renderer;
	}

	protected static OverlayRenderer getRenderer(ProcessingDataProvider dataProvider) {
		return dataProvider.getData(ProcessingDataKeys.STANDARD_OVERLAY_RENDERER_POOL_KEY).getRenderer();
	}

	protected TextureAtlasSprite[] prepareRenderer(OverlayRenderer renderer, EnumFacing face, World blockView, BlockPos pos) {
		return renderer.prepare(face, RenderUtil.getTintColor(tintBlock, blockView, pos, tintIndex), material);
	}

	protected OverlayRenderer prepareRenderer(OverlayRenderer renderer, EnumFacing face, World blockView, BlockPos pos, int sprite1) {
		TextureAtlasSprite[] rendererSprites = prepareRenderer(renderer, face, blockView, pos);
		rendererSprites[0] = sprites[sprite1];
		return renderer;
	}

	protected OverlayRenderer prepareRenderer(OverlayRenderer renderer, EnumFacing face, World blockView, BlockPos pos, int sprite1, int sprite2) {
		TextureAtlasSprite[] rendererSprites = prepareRenderer(renderer, face, blockView, pos);
		rendererSprites[0] = sprites[sprite1];
		rendererSprites[1] = sprites[sprite2];
		return renderer;
	}

	/*
	0:	D R (CORNER)
	1:	D
	2:	L D (CORNER)
	3:	D R
	4:	L D
	5:	L D R
	6:	L D T
	7:	R
	8:	L D R U
	9:	L
	10:	R U
	11:	L U
	12:	D R U
	13:	L R U
	14:	R U (CORNER)
	15:	U
	16:	L U (CORNER)
	 */
	protected OverlayRenderer getRenderer(World blockView, BlockPos pos, IBlockState state, EnumFacing lightFace, TextureAtlasSprite quadSprite, EnumFacing[] directions, ProcessingDataProvider dataProvider) {
		BlockPos.MutableBlockPos mutablePos = dataProvider.getData(ProcessingDataKeys.MUTABLE_POS_KEY);

		//

		mutablePos.setPos(pos).move(directions[0]);
		IBlockState state0 = blockView.getBlockState(mutablePos);
		boolean left = appliesOverlayUnobscured(state0, directions[0], blockView, pos, state, lightFace, quadSprite, mutablePos);
		mutablePos.setPos(pos).move(directions[1]);
		IBlockState state1 = blockView.getBlockState(mutablePos);
		boolean down = appliesOverlayUnobscured(state1, directions[1], blockView, pos, state, lightFace, quadSprite, mutablePos);
		mutablePos.setPos(pos).move(directions[2]);
		IBlockState state2 = blockView.getBlockState(mutablePos);
		boolean right = appliesOverlayUnobscured(state2, directions[2], blockView, pos, state, lightFace, quadSprite, mutablePos);
		mutablePos.setPos(pos).move(directions[3]);
		IBlockState state3 = blockView.getBlockState(mutablePos);
		boolean up = appliesOverlayUnobscured(state3, directions[3], blockView, pos, state, lightFace, quadSprite, mutablePos);

		//

		if (left & down & right & up) {
			return prepareRenderer(getRenderer(dataProvider), lightFace, blockView, pos, 8);
		}
		if (left & down & right) {
			return prepareRenderer(getRenderer(dataProvider), lightFace, blockView, pos, 5);
		}
		if (left & down & up) {
			return prepareRenderer(getRenderer(dataProvider), lightFace, blockView, pos, 6);
		}
		if (left & right & up) {
			return prepareRenderer(getRenderer(dataProvider), lightFace, blockView, pos, 13);
		}
		if (down & right & up) {
			return prepareRenderer(getRenderer(dataProvider), lightFace, blockView, pos, 12);
		}

		if (left & right) {
			return prepareRenderer(getRenderer(dataProvider), lightFace, blockView, pos, 9, 7);
		}
		if (up & down) {
			return prepareRenderer(getRenderer(dataProvider), lightFace, blockView, pos, 15, 1);
		}

		if (left & down) {
			return fromCorner(directions[2], directions[3], 4, 14, getRenderer(dataProvider), blockView, pos, state, lightFace, quadSprite, mutablePos);
		}
		if (down & right) {
			return fromCorner(directions[0], directions[3], 3, 16, getRenderer(dataProvider), blockView, pos, state, lightFace, quadSprite, mutablePos);
		}
		if (right & up) {
			return fromCorner(directions[0], directions[1], 10, 2, getRenderer(dataProvider), blockView, pos, state, lightFace, quadSprite, mutablePos);
		}
		if (up & left) {
			return fromCorner(directions[1], directions[2], 11, 0, getRenderer(dataProvider), blockView, pos, state, lightFace, quadSprite, mutablePos);
		}

		//

		if (left) {
			return fromOneSide(state1, state2, state3, directions[1], directions[2], directions[3], 9, 0, 14, getRenderer(dataProvider), blockView, pos, state, lightFace, quadSprite, mutablePos);
		}
		if (down) {
			return fromOneSide(state2, state3, state0, directions[2], directions[3], directions[0], 1, 14, 16, getRenderer(dataProvider), blockView, pos, state, lightFace, quadSprite, mutablePos);
		}
		if (right) {
			return fromOneSide(state3, state0, state1, directions[3], directions[0], directions[1], 7, 16, 2, getRenderer(dataProvider), blockView, pos, state, lightFace, quadSprite, mutablePos);
		}
		if (up) {
			return fromOneSide(state0, state1, state2, directions[0], directions[1], directions[2], 15, 2, 0, getRenderer(dataProvider), blockView, pos, state, lightFace, quadSprite, mutablePos);
		}

		//

		boolean s0 = hasSameOverlayUnobscured(state0, directions[0], blockView, pos, state, lightFace, quadSprite, mutablePos);
		boolean s1 = hasSameOverlayUnobscured(state1, directions[1], blockView, pos, state, lightFace, quadSprite, mutablePos);
		boolean s2 = hasSameOverlayUnobscured(state2, directions[2], blockView, pos, state, lightFace, quadSprite, mutablePos);
		boolean s3 = hasSameOverlayUnobscured(state3, directions[3], blockView, pos, state, lightFace, quadSprite, mutablePos);

		boolean corner0 = false;
		boolean corner1 = false;
		boolean corner2 = false;
		boolean corner3 = false;
		if (s0 | s1) {
			corner0 = appliesOverlayCorner(directions[0], directions[1], blockView, pos, state, lightFace, quadSprite, mutablePos);
		}
		if (s1 | s2) {
			corner1 = appliesOverlayCorner(directions[1], directions[2], blockView, pos, state, lightFace, quadSprite, mutablePos);
		}
		if (s2 | s3) {
			corner2 = appliesOverlayCorner(directions[2], directions[3], blockView, pos, state, lightFace, quadSprite, mutablePos);
		}
		if (s3 | s0) {
			corner3 = appliesOverlayCorner(directions[3], directions[0], blockView, pos, state, lightFace, quadSprite, mutablePos);
		}

		if (corner0 | corner1 | corner2 | corner3) {
			OverlayRenderer renderer = getRenderer(dataProvider);
			TextureAtlasSprite[] rendererSprites = prepareRenderer(renderer, lightFace, blockView, pos);
			if (corner0) {
				rendererSprites[0] = sprites[2];
			}
			if (corner1) {
				rendererSprites[1] = sprites[0];
			}
			if (corner2) {
				rendererSprites[2] = sprites[14];
			}
			if (corner3) {
				rendererSprites[3] = sprites[16];
			}
			return renderer;
		}

		//

		return null;
	}

	public static class OverlayRenderer implements Consumer<QuadEmitter> {
		protected static final TextureAtlasSprite[] EMPTY_SPRITES = new TextureAtlasSprite[4];

		protected TextureAtlasSprite[] sprites = new TextureAtlasSprite[4];
		protected EnumFacing face;
		protected int color;
		protected RenderMaterial material;

		@Override
		public void accept(QuadEmitter emitter) {
			for (TextureAtlasSprite sprite : sprites) {
				if (sprite != null && !TextureUtil.isMissingSprite(sprite)) {
					QuadUtil.emitOverlayQuad(emitter, face, sprite, color, material);
				}
			}
		}

		public TextureAtlasSprite[] prepare(EnumFacing face, int color, RenderMaterial material) {
			System.arraycopy(EMPTY_SPRITES, 0, sprites, 0, EMPTY_SPRITES.length);
			this.face = face;
			this.color = color;
			this.material = material;
			return sprites;
		}
	}

	public static class OverlayRendererPool {
		protected final List<OverlayRenderer> list = new ObjectArrayList<>();
		protected int nextIndex = 0;

		public OverlayRenderer getRenderer() {
			if (nextIndex >= list.size()) {
				list.add(new OverlayRenderer());
			}
			OverlayRenderer renderer = list.get(nextIndex);
			nextIndex++;
			return renderer;
		}

		public void reset() {
			nextIndex = 0;
		}
	}

	public static class Factory extends AbstractQuadProcessorFactory<StandardOverlayCTMProperties> {
		@Override
		public QuadProcessor createProcessor(StandardOverlayCTMProperties properties, TextureAtlasSprite[] sprites) {
			OverlayPropertiesSection overlaySection = properties.getOverlayPropertiesSection();
			return new StandardOverlayQuadProcessor(sprites, OverlayProcessingPredicate.fromProperties(properties), properties.getMatchTilesSet(), properties.getMatchBlocksPredicate(), properties.getConnectTilesSet(), properties.getConnectBlocksPredicate(), properties.getConnectionPredicate(), overlaySection.getTintIndex(), overlaySection.getTintBlock(), overlaySection.getLayer());
		}

		@Override
		public int getTextureAmount(StandardOverlayCTMProperties properties) {
			return 17;
		}

		@Override
		public boolean supportsNullSprites(StandardOverlayCTMProperties properties) {
			return false;
		}
	}
}
