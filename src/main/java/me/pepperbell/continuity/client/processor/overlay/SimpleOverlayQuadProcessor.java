package me.pepperbell.continuity.client.processor.overlay;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.pepperbell.continuity.api.client.QuadProcessor;
import me.pepperbell.continuity.client.processor.ProcessingDataKeys;
import me.pepperbell.continuity.client.processor.ProcessingPredicate;
import me.pepperbell.continuity.client.processor.simple.SimpleQuadProcessor;
import me.pepperbell.continuity.client.processor.simple.SpriteProvider;
import me.pepperbell.continuity.client.properties.BaseCTMProperties;
import me.pepperbell.continuity.client.properties.overlay.OverlayPropertiesSection;
import me.pepperbell.continuity.client.util.QuadUtil;
import me.pepperbell.continuity.client.util.RenderUtil;
import me.pepperbell.continuity.client.util.TextureUtil;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleOverlayQuadProcessor extends SimpleQuadProcessor {
	protected int tintIndex;
	protected IBlockState tintBlock;
	protected RenderMaterial material;

	public SimpleOverlayQuadProcessor(SpriteProvider spriteProvider, ProcessingPredicate processingPredicate, int tintIndex, IBlockState tintBlock, BlendMode layer) {
		super(spriteProvider, processingPredicate);
		this.tintIndex = tintIndex;
		this.tintBlock = tintBlock;
		material = RenderUtil.getMaterialFinder().blendMode(0, layer.blockRenderLayer).find();
	}

	@Override
	public ProcessingResult processQuad(MutableQuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, int pass, int processorIndex, ProcessingContext context) {
		if (processingPredicate.shouldProcessQuad(quad, sprite, blockView, state, pos, context)) {
			TextureAtlasSprite newSprite = spriteProvider.getSprite(quad, sprite, blockView, state, pos, randomSupplier, context);
			if (!TextureUtil.isMissingSprite(newSprite)) {
				OverlayRenderer renderer = context.getData(ProcessingDataKeys.SIMPLE_OVERLAY_RENDERER_POOL_KEY).getRenderer();
				renderer.prepare(quad.lightFace(), newSprite, RenderUtil.getTintColor(tintBlock, blockView, pos, tintIndex), material);
				context.addEmitterConsumer(renderer);
			}
		}
		return ProcessingResult.CONTINUE;
	}

	public static class OverlayRenderer implements Consumer<QuadEmitter> {
		protected EnumFacing face;
		protected TextureAtlasSprite sprite;
		protected int color;
		protected RenderMaterial material;

		@Override
		public void accept(QuadEmitter emitter) {
			QuadUtil.emitOverlayQuad(emitter, face, sprite, color, material);
		}

		public void prepare(EnumFacing face, TextureAtlasSprite sprite, int color, RenderMaterial material) {
			this.face = face;
			this.sprite = sprite;
			this.color = color;
			this.material = material;
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

	public static class Factory<T extends BaseCTMProperties & OverlayPropertiesSection.Provider> extends SimpleQuadProcessor.Factory<T> {
		public Factory(SpriteProvider.Factory<? super T> spriteProviderFactory) {
			super(spriteProviderFactory);
		}

		@Override
		public QuadProcessor createProcessor(T properties, TextureAtlasSprite[] sprites) {
			OverlayPropertiesSection overlaySection = properties.getOverlayPropertiesSection();
			return new SimpleOverlayQuadProcessor(spriteProviderFactory.createSpriteProvider(sprites, properties), OverlayProcessingPredicate.fromProperties(properties), overlaySection.getTintIndex(), overlaySection.getTintBlock(), overlaySection.getLayer());
		}

		@Override
		public boolean supportsNullSprites(T properties) {
			return false;
		}
	}
}
