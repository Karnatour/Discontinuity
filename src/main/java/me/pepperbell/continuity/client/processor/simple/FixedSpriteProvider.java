package me.pepperbell.continuity.client.processor.simple;

import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import me.pepperbell.continuity.client.properties.BaseCTMProperties;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public class FixedSpriteProvider implements SpriteProvider {
	protected TextureAtlasSprite sprite;

	public FixedSpriteProvider(TextureAtlasSprite sprite) {
		this.sprite = sprite;
	}

	@Override
	@Nullable
	public TextureAtlasSprite getSprite(QuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, ProcessingDataProvider dataProvider) {
		return this.sprite;
	}

	public static class Factory implements SpriteProvider.Factory<BaseCTMProperties> {
		@Override
		public SpriteProvider createSpriteProvider(TextureAtlasSprite[] sprites, BaseCTMProperties properties) {
			return new FixedSpriteProvider(sprites[0]);
		}

		@Override
		public int getTextureAmount(BaseCTMProperties properties) {
			return 1;
		}
	}
}
