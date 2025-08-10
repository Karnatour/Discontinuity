package me.pepperbell.continuity.client.resource;

import me.pepperbell.continuity.client.ContinuityClient;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.Properties;

public final class EmissiveSuffixLoader {
	public static final ResourceLocation LOCATION = new ResourceLocation("optifine/emissive.properties");

	private static String emissiveSuffix;

	@Nullable
	public static String getEmissiveSuffix() {
		return emissiveSuffix;
	}

	@ApiStatus.Internal
	public static void load(IResourceManager manager) {
		emissiveSuffix = null;

		try (IResource resource = manager.getResource(LOCATION)) {
			Properties properties = new Properties();
			properties.load(resource.getInputStream());
			emissiveSuffix = properties.getProperty("suffix.emissive");
		} catch (FileNotFoundException e) {
			//
		} catch (Exception e) {
			ContinuityClient.LOGGER.error("Failed to load emissive suffix from file '" + LOCATION + "'", e);
		}
	}
}
