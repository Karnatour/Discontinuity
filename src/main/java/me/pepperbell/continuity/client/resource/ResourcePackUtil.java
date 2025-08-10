package me.pepperbell.continuity.client.resource;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.List;

public final class ResourcePackUtil {

	private static IResourcePack[] resourcePacks;

	public static DefaultResourcePack getDefaultResourcePack() {
		return (DefaultResourcePack) Minecraft.getMinecraft().defaultResourcePack;
	}

	public static void setup(IResourceManager resourceManager) {
		ResourcePackRepository repository = Minecraft.getMinecraft().getResourcePackRepository();
		List<ResourcePackRepository.Entry> entries = repository.getRepositoryEntries();
		resourcePacks = entries.stream()
				.map(ResourcePackRepository.Entry::getResourcePack)
				.toArray(IResourcePack[]::new);

		// Reverse order to prioritize last-added
		ArrayUtils.reverse(resourcePacks);
	}


	@Nullable
	public static IResourcePack getProvidingResourcePack(ResourceLocation resourceLocation) {
		for (IResourcePack pack : resourcePacks) {
			try {
				if (pack.resourceExists(resourceLocation)) {
					return pack;
				}
			} catch (Exception ignored) {
				// In case of weird exceptions when querying broken resource packs
			}
		}
		return null;
	}

	public static void clear() {
		resourcePacks = null;
	}
}
