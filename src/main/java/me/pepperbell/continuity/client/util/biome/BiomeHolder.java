package me.pepperbell.continuity.client.util.biome;

import me.pepperbell.continuity.client.ContinuityClient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;

public class BiomeHolder {
	protected final ResourceLocation id;
	protected Biome biome;

	public BiomeHolder(ResourceLocation id) {
		this.id = id;
		refresh();
	}

	public ResourceLocation getId() {
		return id;
	}

	@Nullable
	public Biome getBiome() {
		return biome;
	}

	public void refresh() {
		if (Biome.REGISTRY.containsKey(id)) {
			biome = Biome.REGISTRY.getObject(id);
		} else {
			ContinuityClient.LOGGER.warn("Unknown biome '" + id + "'");
			biome = null;
		}
	}
}
