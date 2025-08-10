package me.pepperbell.continuity.client.mixinterface;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public interface ChunkRendererRegionExtension {
	Biome continuity$getBiome(BlockPos pos);
}
