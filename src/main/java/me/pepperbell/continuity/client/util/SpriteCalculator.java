package me.pepperbell.continuity.client.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.ApiStatus;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

public final class SpriteCalculator {
	private static BlockModelShapes MODELS;

	public static BlockModelShapes getBlockModelShapes() {
		if (MODELS == null) {
			MODELS = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
		}
		return MODELS;
	}

	private static final EnumMap<EnumFacing, SpriteCache> SPRITE_CACHES = new EnumMap<>(EnumFacing.class);
	static {
		for (EnumFacing direction : EnumFacing.values()) {
			SPRITE_CACHES.put(direction, new SpriteCache(direction));
		}
	}

	public static TextureAtlasSprite getSprite(IBlockState state, EnumFacing face) {
		return SPRITE_CACHES.get(face).getSprite(state);
	}

	public static TextureAtlasSprite calculateSprite(IBlockState state, EnumFacing face, Supplier<Random> randomSupplier) {
		IBakedModel model = getBlockModelShapes().getModelForState(state);
		try {
			List<BakedQuad> quads = model.getQuads(state, face, randomSupplier.get().nextLong());
			if (!quads.isEmpty()) {
				return quads.get(0).getSprite();
			}
			quads = model.getQuads(state, null, randomSupplier.get().nextLong());
			if (!quads.isEmpty()) {
				int amount = quads.size();
				for (int i = 0; i < amount; i++) {
					BakedQuad quad = quads.get(i);
					if (quad.getFace() == face) {
						return quad.getSprite();
					}
				}
			}
		} catch (Exception e) {
			//
		}
		return model.getParticleTexture();
	}

	@ApiStatus.Internal
	public static void clearCache() {
		for (SpriteCache cache : SPRITE_CACHES.values()) {
			cache.clear();
		}
	}

	private static class SpriteCache {
		private final EnumFacing face;
		private final Map<IBlockState, TextureAtlasSprite> sprites = new Object2ObjectOpenHashMap<>();
		private final Supplier<Random> randomSupplier = new Supplier<>() {
			private final Random random = new Random();

			@Override
			public Random get() {
				// Use item rendering seed for consistency
				random.setSeed(42L);
				return random;
			}
		};
		private final StampedLock lock = new StampedLock();

		public SpriteCache(EnumFacing face) {
			this.face = face;
		}

		public TextureAtlasSprite getSprite(IBlockState state) {
			TextureAtlasSprite sprite;
			long readStamp = lock.readLock();
			try {
				sprite = sprites.get(state);
			} finally {
				lock.unlockRead(readStamp);
			}
			if (sprite == null) {
				long writeStamp = lock.writeLock();
				try {
					sprite = calculateSprite(state, face, randomSupplier);
					sprites.put(state, sprite);
				} finally {
					lock.unlockWrite(writeStamp);
				}
			}
			return sprite;
		}

		public void clear() {
			long writeStamp = lock.writeLock();
			try {
				sprites.clear();
			} finally {
				lock.unlockWrite(writeStamp);
			}
		}
	}
}
