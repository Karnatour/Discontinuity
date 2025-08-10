package me.pepperbell.continuity.client.model;

import me.pepperbell.continuity.api.client.EmissiveSpriteApi;
import me.pepperbell.continuity.client.config.ContinuityConfig;
import me.pepperbell.continuity.client.util.QuadUtil;
import me.pepperbell.continuity.client.util.RenderUtil;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class EmissiveBakedModel extends ForwardingBakedModel {
	protected static final RenderMaterial[] EMISSIVE_MATERIALS;
	protected static final RenderMaterial DEFAULT_EMISSIVE_MATERIAL;
	protected static final RenderMaterial CUTOUT_MIPPED_EMISSIVE_MATERIAL;

	static {
		BlendMode[] blendModes = BlendMode.values();
		EMISSIVE_MATERIALS = new RenderMaterial[blendModes.length];
		MaterialFinder finder = RenderUtil.getMaterialFinder();
		for (BlendMode blendMode : blendModes) {
			EMISSIVE_MATERIALS[blendMode.ordinal()] = finder.emissive(0, true).disableDiffuse(0, true).disableAo(0, true).blendMode(0, blendMode.blockRenderLayer).find();
		}

		DEFAULT_EMISSIVE_MATERIAL = EMISSIVE_MATERIALS[BlendMode.DEFAULT.ordinal()];
		CUTOUT_MIPPED_EMISSIVE_MATERIAL = EMISSIVE_MATERIALS[BlendMode.CUTOUT_MIPPED.ordinal()];
	}

	public EmissiveBakedModel(IBakedModel wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void emitBlockQuads(World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		if (!ContinuityConfig.INSTANCE.emissiveTextures.get()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		ModelObjectsContainer container = ModelObjectsContainer.get();
		if (!container.featureStates.getEmissiveTexturesState().isEnabled()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		EmissiveBlockQuadTransform quadTransform = container.emissiveBlockQuadTransform;
		if (quadTransform.isActive()) {
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			return;
		}

		MeshBuilder meshBuilder = container.meshBuilder;
		quadTransform.prepare(meshBuilder.getEmitter(), blockView, state, pos, ContinuityConfig.INSTANCE.useManualCulling.get());

		context.pushTransform(quadTransform);
		super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
		context.popTransform();

		if (quadTransform.didEmit()) {
			context.meshConsumer().accept(meshBuilder.build());
		}
		quadTransform.reset();
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		if (!ContinuityConfig.INSTANCE.emissiveTextures.get()) {
			super.emitItemQuads(stack, randomSupplier, context);
			return;
		}

		ModelObjectsContainer container = ModelObjectsContainer.get();
		if (!container.featureStates.getEmissiveTexturesState().isEnabled()) {
			super.emitItemQuads(stack, randomSupplier, context);
			return;
		}

		EmissiveItemQuadTransform quadTransform = container.emissiveItemQuadTransform;
		if (quadTransform.isActive()) {
			super.emitItemQuads(stack, randomSupplier, context);
			return;
		}

		MeshBuilder meshBuilder = container.meshBuilder;
		quadTransform.prepare(meshBuilder.getEmitter());

		context.pushTransform(quadTransform);
		super.emitItemQuads(stack, randomSupplier, context);
		context.popTransform();

		if (quadTransform.didEmit()) {
			context.meshConsumer().accept(meshBuilder.build());
		}
		quadTransform.reset();
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		return wrapped.getQuads(state, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return wrapped.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return wrapped.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return wrapped.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return wrapped.getParticleTexture();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return wrapped.getOverrides();
	}

	protected static class EmissiveBlockQuadTransform implements RenderContext.QuadTransform {
		protected final CullingCache cullingCache = new CullingCache();

		protected QuadEmitter emitter;
		protected World blockView;
		protected IBlockState state;
		protected BlockPos pos;
		protected boolean useManualCulling;

		protected boolean active;
		protected boolean didEmit;
		protected boolean calculateDefaultLayer;
		protected boolean isDefaultLayerSolid;

		@Override
		public boolean transform(MutableQuadView quad) {
			if (useManualCulling && cullingCache.shouldCull(quad, blockView, pos, state)) {
				return false;
			}

			TextureAtlasSprite sprite = RenderUtil.getSpriteFinder().find(quad, 0);
			TextureAtlasSprite emissiveSprite = EmissiveSpriteApi.get().getEmissiveSprite(sprite);
			if (emissiveSprite != null) {
				quad.copyTo(emitter);

				BlendMode blendMode = RenderUtil.getBlendMode(quad);
				RenderMaterial emissiveMaterial;
				if (blendMode == BlendMode.DEFAULT) {
					if (calculateDefaultLayer) {
						isDefaultLayerSolid = state.getBlock().getRenderLayer() == BlockRenderLayer.SOLID;
						calculateDefaultLayer = false;
					}

					if (isDefaultLayerSolid) {
						emissiveMaterial = CUTOUT_MIPPED_EMISSIVE_MATERIAL;
					} else {
						emissiveMaterial = DEFAULT_EMISSIVE_MATERIAL;
					}
				} else if (blendMode == BlendMode.SOLID) {
					emissiveMaterial = CUTOUT_MIPPED_EMISSIVE_MATERIAL;
				} else {
					emissiveMaterial = EMISSIVE_MATERIALS[blendMode.ordinal()];
				}

				emitter.material(emissiveMaterial);
				QuadUtil.interpolate(emitter, sprite, emissiveSprite);
				emitter.emit();
				didEmit = true;
			}
			return true;
		}

		public boolean isActive() {
			return active;
		}

		public boolean didEmit() {
			return didEmit;
		}

		public void prepare(QuadEmitter emitter, World blockView, IBlockState state, BlockPos pos, boolean useManualCulling) {
			this.emitter = emitter;
			this.blockView = blockView;
			this.state = state;
			this.pos = pos;
			this.useManualCulling = useManualCulling;

			active = true;
			didEmit = false;
			calculateDefaultLayer = true;
			isDefaultLayerSolid = false;

			cullingCache.prepare();
		}

		public void reset() {
			emitter = null;
			blockView = null;
			state = null;
			pos = null;
			useManualCulling = false;

			active = false;
		}
	}

	protected static class EmissiveItemQuadTransform implements RenderContext.QuadTransform {
		protected QuadEmitter emitter;

		protected boolean active;
		protected boolean didEmit;

		@Override
		public boolean transform(MutableQuadView quad) {
			TextureAtlasSprite sprite = RenderUtil.getSpriteFinder().find(quad, 0);
			TextureAtlasSprite emissiveSprite = EmissiveSpriteApi.get().getEmissiveSprite(sprite);
			if (emissiveSprite != null) {
				quad.copyTo(emitter);
				emitter.material(DEFAULT_EMISSIVE_MATERIAL);
				QuadUtil.interpolate(emitter, sprite, emissiveSprite);
				emitter.emit();
				didEmit = true;
			}
			return true;
		}

		public boolean isActive() {
			return active;
		}

		public boolean didEmit() {
			return didEmit;
		}

		public void prepare(QuadEmitter emitter) {
			this.emitter = emitter;

			active = true;
			didEmit = false;
		}

		public void reset() {
			active = false;
			emitter = null;
		}
	}
}
