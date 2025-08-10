package me.pepperbell.continuity.client.resource;

import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.properties.PropertiesParsingHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Predicate;

public final class CustomBlockLayers {
	public static final ResourceLocation LOCATION = new ResourceLocation("optifine/block.properties");

	@SuppressWarnings("unchecked")
	private static final Predicate<IBlockState>[] EMPTY_LAYER_PREDICATES = new Predicate[BlockLayer.VALUES.length];

	@SuppressWarnings("unchecked")
	private static final Predicate<IBlockState>[] LAYER_PREDICATES = new Predicate[BlockLayer.VALUES.length];

	private static boolean disableSolidCheck;

	@Nullable
	public static BlockRenderLayer getLayer(IBlockState state) {
		if (!disableSolidCheck) {
			if (state.isOpaqueCube()) {
				return null;
			}
		}

		for (int i = 0; i < BlockLayer.VALUES.length; i++) {
			Predicate<IBlockState> predicate = LAYER_PREDICATES[i];
			if (predicate != null) {
				if (predicate.test(state)) {
					return BlockLayer.VALUES[i].getLayer();
				}
			}
		}
		return null;
	}

	private static void reload(IResourceManager manager) {
		System.arraycopy(EMPTY_LAYER_PREDICATES, 0, LAYER_PREDICATES, 0, EMPTY_LAYER_PREDICATES.length);

		try (IResource resource = manager.getResource(LOCATION)) {
			Properties properties = new Properties();
			properties.load(resource.getInputStream());
			reload(properties, resource.getResourceLocation(), resource.getResourcePackName());
		} catch (FileNotFoundException e) {
			//
		} catch (Exception e) {
			ContinuityClient.LOGGER.error("Failed to load custom block layers from file '" + LOCATION + "'", e);
		}
	}

	private static void reload(Properties properties, ResourceLocation fileLocation, String packName) {
		for (BlockLayer blockLayer : BlockLayer.VALUES) {
			String propertyKey = "layer." + blockLayer.getKey();
			Predicate<IBlockState> predicate = PropertiesParsingHelper.parseBlockStates(properties, propertyKey, fileLocation, packName);
			if (predicate != PropertiesParsingHelper.EMPTY_BLOCK_STATE_PREDICATE) {
				LAYER_PREDICATES[blockLayer.ordinal()] = predicate;
			}
		}

		String disableSolidCheckStr = properties.getProperty("disableSolidCheck");
		if (disableSolidCheckStr != null) {
			disableSolidCheck = Boolean.parseBoolean(disableSolidCheckStr.trim());
		}
	}

	public static class ReloadListener{
		private static final ReloadListener INSTANCE = new ReloadListener();

		public static void init() {
			MinecraftForge.EVENT_BUS.register(INSTANCE);
		}

		@SubscribeEvent
		public void onTextureStitch(TextureStitchEvent.Post event) {
			CustomBlockLayers.reload(Minecraft.getMinecraft().getResourceManager());
		}
	}

	private enum BlockLayer {
		SOLID(BlockRenderLayer.SOLID),
		CUTOUT(BlockRenderLayer.CUTOUT),
		CUTOUT_MIPPED(BlockRenderLayer.CUTOUT_MIPPED),
		TRANSLUCENT(BlockRenderLayer.TRANSLUCENT);

		public static final BlockLayer[] VALUES = values();

		private final BlockRenderLayer layer;
		private final String key;

		BlockLayer(BlockRenderLayer layer) {
			this.layer = layer;
			key = name().toLowerCase(Locale.ROOT);
		}

		public BlockRenderLayer getLayer() {
			return layer;
		}

		public String getKey() {
			return key;
		}
	}
}
