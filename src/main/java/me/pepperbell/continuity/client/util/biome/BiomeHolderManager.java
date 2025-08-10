package me.pepperbell.continuity.client.util.biome;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ResourceLocation;
import java.util.Map;

public final class BiomeHolderManager {
	private static final Map<ResourceLocation, BiomeHolder> HOLDER_CACHE = new Object2ObjectOpenHashMap<>();

	public static BiomeHolder getOrCreateHolder(ResourceLocation id) {
		BiomeHolder holder = HOLDER_CACHE.get(id);
		if (holder == null) {
			holder = new BiomeHolder(id);
			HOLDER_CACHE.put(id, holder);
		}
		return holder;
	}

	public static void init() {
		refreshHolders();
	}

	public static void refreshHolders() {
		for (BiomeHolder holder : HOLDER_CACHE.values()) {
			holder.refresh();
		}
	}

	public static void clearCache() {
		HOLDER_CACHE.clear();
	}
}
