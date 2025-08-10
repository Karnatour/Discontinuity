package me.pepperbell.continuity.client.util.biome;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

public final class BiomeRetriever {
	private static final Provider PROVIDER = BiomeRetriever::getBiomeByWorldView;

	@Nullable
	public static Biome getBiome(World blockView, BlockPos pos) {
		return PROVIDER.getBiome(blockView, pos);
	}

	@Nullable
	private static Biome getBiomeByWorldView(World world, BlockPos pos) {
		if (world != null && pos != null) {
			return world.getBiome(pos);
		}
		return null;
	}

	private interface Provider {
		@Nullable
		Biome getBiome(World blockView, BlockPos pos);
	}
}
