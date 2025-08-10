package me.pepperbell.continuity.client.processor.simple;

import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import me.pepperbell.continuity.client.processor.ProcessingDataKeys;
import me.pepperbell.continuity.client.processor.Symmetry;
import me.pepperbell.continuity.client.properties.RandomCTMProperties;
import me.pepperbell.continuity.client.util.MathUtil;
import me.pepperbell.continuity.client.util.RandomIndexProvider;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.function.Supplier;

public class RandomSpriteProvider implements SpriteProvider {
	protected TextureAtlasSprite[] sprites;
	protected RandomIndexProvider indexProvider;
	protected int randomLoops;
	protected Symmetry symmetry;
	protected boolean linked;

	public RandomSpriteProvider(TextureAtlasSprite[] sprites, RandomIndexProvider indexProvider, int randomLoops, Symmetry symmetry, boolean linked) {
		this.sprites = sprites;
		this.indexProvider = indexProvider;
		this.randomLoops = randomLoops;
		this.symmetry = symmetry;
		this.linked = linked;
	}

	@Override
	@Nullable
	public TextureAtlasSprite getSprite(QuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, Supplier<Random> randomSupplier, ProcessingDataProvider dataProvider) {
		EnumFacing face = symmetry.getActualFace(quad.lightFace());

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if (linked) {
			Block block = state.getBlock();
			BlockPos.MutableBlockPos mutablePos = dataProvider.getData(ProcessingDataKeys.MUTABLE_POS_KEY).setPos(pos);

			int i = 0;
			do {
				mutablePos.setY(mutablePos.getY() - 1);
				i++;
			} while (i < 3 && block == blockView.getBlockState(mutablePos).getBlock());
			y = mutablePos.getY() + 1;
		}

		int seed = MathUtil.mix(x, y, z, face.ordinal(), randomLoops);
		return sprites[indexProvider.getRandomIndex(seed)];
	}

	public static class Factory implements SpriteProvider.Factory<RandomCTMProperties> {
		@Override
		public SpriteProvider createSpriteProvider(TextureAtlasSprite[] sprites, RandomCTMProperties properties) {
			if (sprites.length == 1) {
				return new FixedSpriteProvider(sprites[0]);
			}
			return new RandomSpriteProvider(sprites, properties.getIndexProviderFactory().createIndexProvider(sprites.length), properties.getRandomLoops(), properties.getSymmetry(), properties.getLinked());
		}

		@Override
		public int getTextureAmount(RandomCTMProperties properties) {
			return properties.getSpriteIds().size();
		}
	}
}
