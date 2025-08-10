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

public interface SpriteProvider {
	@Nullable
	TextureAtlasSprite getSprite(QuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, ProcessingDataProvider dataProvider);

	interface Factory<T extends BaseCTMProperties> {
		SpriteProvider createSpriteProvider(TextureAtlasSprite[] sprites, T properties);

		int getTextureAmount(T properties);
	}
}
