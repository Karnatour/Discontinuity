package me.pepperbell.continuity.impl.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.pepperbell.continuity.api.client.CTMLoader;
import me.pepperbell.continuity.api.client.CTMLoaderRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class CTMLoaderRegistryImpl implements CTMLoaderRegistry {
	public static final CTMLoaderRegistryImpl INSTANCE = new CTMLoaderRegistryImpl();

	private final Map<String, CTMLoader<?>> loaderMap = new Object2ObjectOpenHashMap<>();

	@Override
	public void registerLoader(String method, CTMLoader<?> loader) {
		loaderMap.put(method, loader);
	}

	@Override
	@Nullable
	public CTMLoader<?> getLoader(String method) {
		return loaderMap.get(method);
	}
}
