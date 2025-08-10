package me.pepperbell.continuity.client.resource;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import me.pepperbell.continuity.api.client.CTMLoader;
import me.pepperbell.continuity.api.client.CTMLoaderRegistry;
import me.pepperbell.continuity.api.client.CTMProperties;
import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.util.BooleanState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

public final class CTMPropertiesLoader {
	private static final List<CTMLoadingContainer<?>> ALL = new ObjectArrayList<>();
	private static final List<CTMLoadingContainer<?>> AFFECTS_BLOCK = new ObjectArrayList<>();
	private static final List<CTMLoadingContainer<?>> IGNORES_BLOCK = new ObjectArrayList<>();
	private static final List<CTMLoadingContainer<?>> VALID_FOR_MULTIPASS = new ObjectArrayList<>();

	private static final OptionalListCreator<CTMLoadingContainer<?>> LIST_CREATOR = new OptionalListCreator<>();

	private static List<IResourcePack> getAllResourcePacks() {
		ResourcePackRepository repository = Minecraft.getMinecraft().getResourcePackRepository();
		List<IResourcePack> packs = new ArrayList<>();

		for (ResourcePackRepository.Entry entry : repository.getRepositoryEntries()) {
			packs.add(entry.getResourcePack());
		}

		packs.add(Minecraft.getMinecraft().defaultResourcePack);

		return packs;
	}

	@ApiStatus.Internal
	public static void loadAll(IResourceManager resourceManager) {
		int packPriority = 0;
		List<IResourcePack> packs = getAllResourcePacks();
		BooleanState invalidIdentifierState = InvalidIdentifierStateHolder.get();
		invalidIdentifierState.enable();

		for (IResourcePack pack : packs) {
			loadAll(pack, packPriority);
			packPriority++;
		}
		invalidIdentifierState.disable();
		resolveMultipassDependents();
	}

	public static List<File> findPropertiesFiles(File folder) {
		List<File> propertiesFiles = new ArrayList<>();
		scanFolder(folder, propertiesFiles);
		return propertiesFiles;
	}

	private static void scanFolder(File folder, List<File> result) {
		if (folder == null || !folder.exists()) return;

		File[] files = folder.listFiles();
		if (files == null) return;

		for (File file : files) {
			if (file.isDirectory()) {
				scanFolder(file, result);
			} else if (file.isFile() && file.getName().endsWith(".properties")) {
				result.add(file);
			}
		}
	}

	public static String getRelativePath(File base, File file) {
		return base.toURI().relativize(file.toURI()).getPath().replace("\\", "/");
	}

	private static void loadAll(IResourcePack pack, int packPriority) {
		if (!(pack instanceof AbstractResourcePack)) {
			return;
		}

		AbstractResourcePack folderPack = (AbstractResourcePack) pack;
		File resourcePackFile = folderPack.resourcePackFile;

		List<File> propsFiles = findPropertiesFiles(resourcePackFile);

		for (File propsFile : propsFiles) {
			try (FileInputStream fis = new FileInputStream(propsFile)) {
				Properties props = new Properties();
				props.load(fis);

				String relativePath = getRelativePath(resourcePackFile, propsFile);
				ResourceLocation id = toResourceLocation(relativePath);

				load(props, id, pack.getPackName(), packPriority);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static ResourceLocation toResourceLocation(String relativePath) {
		if (!relativePath.startsWith("assets/")) {
			return null;
		}

		String pathAfterAssets = relativePath.substring("assets/".length());
		int slashIndex = pathAfterAssets.indexOf('/');

		if (slashIndex < 0) {
			return null;
		}

		String namespace = pathAfterAssets.substring(0, slashIndex);
		String path = pathAfterAssets.substring(slashIndex + 1);

		return new ResourceLocation(namespace, path);
	}

	private static void load(Properties properties, ResourceLocation id, String packName, int packPriority) {
		System.out.println("Loading CTM properties for '" + id + "' in pack '" + packName + "' with priority " + packPriority);
		String method = properties.getProperty("method", "ctm").trim();
		CTMLoader<?> loader = CTMLoaderRegistry.get().getLoader(method);
		if (loader != null) {
			load(loader, properties, id, packName, packPriority, method);
		} else {
			ContinuityClient.LOGGER.error("Unknown 'method' value '" + method + "' in file '" + id + "' in pack '" + packName + "'");
		}
	}

	private static <T extends CTMProperties> void load(CTMLoader<T> loader, Properties properties, ResourceLocation id, String packName, int packPriority, String method) {
		T ctmProperties = loader.getPropertiesFactory().createProperties(properties, id, packName, packPriority, method);
		if (ctmProperties != null) {
			CTMLoadingContainer<T> container = new CTMLoadingContainer<>(loader, ctmProperties);
			ALL.add(container);
			if (ctmProperties.affectsBlockStates()) {
				AFFECTS_BLOCK.add(container);
			} else {
				IGNORES_BLOCK.add(container);
			}
			if (ctmProperties.affectsTextures() && ctmProperties.isValidForMultipass()) {
				VALID_FOR_MULTIPASS.add(container);
			}
		}
	}

	private static void resolveMultipassDependents() {
		if (isEmpty()) {
			return;
		}

		Object2ObjectOpenHashMap<ResourceLocation, CTMLoadingContainer<?>> texture2ContainerMap = new Object2ObjectOpenHashMap<>();
		Object2ObjectOpenHashMap<ResourceLocation, List<CTMLoadingContainer<?>>> texture2ContainerListMap = new Object2ObjectOpenHashMap<>();

		int amount = ALL.size();
		for (int i = 0; i < amount; i++) {
			CTMLoadingContainer<?> container = ALL.get(i);
			Collection<ResourceLocation> textureDependencies = container.getProperties().getTextureDependencies();
			for (ResourceLocation spriteId : textureDependencies) {
                CTMLoadingContainer<?> containerValue = texture2ContainerMap.get(spriteId);
				if (containerValue == null) {
					List<CTMLoadingContainer<?>> containerListValue = texture2ContainerListMap.get(spriteId);
					if (containerListValue == null) {
						texture2ContainerMap.put(spriteId, container);
					} else {
						containerListValue.add(container);
					}
				} else {
					List<CTMLoadingContainer<?>> containerList = new ObjectArrayList<>();
					containerList.add(containerValue);
					containerList.add(container);
					texture2ContainerListMap.put(spriteId, containerList);
					texture2ContainerMap.remove(spriteId);
				}
			}
		}

		int amount1 = VALID_FOR_MULTIPASS.size();
		ObjectIterator<Object2ObjectMap.Entry<ResourceLocation, CTMLoadingContainer<?>>> iterator = texture2ContainerMap.object2ObjectEntrySet().fastIterator();
		while (iterator.hasNext()) {
			Object2ObjectMap.Entry<ResourceLocation, CTMLoadingContainer<?>> entry = iterator.next();
			ResourceLocation textureId = entry.getKey();
			CTMLoadingContainer<?> container1 = entry.getValue();

			for (int i = 0; i < amount1; i++) {
				CTMLoadingContainer<?> container = VALID_FOR_MULTIPASS.get(i);
				if (container.getProperties().affectsTexture(textureId)) {
					container1.addMultipassDependent(container);
				}
			}
		}
		ObjectIterator<Object2ObjectMap.Entry<ResourceLocation, List<CTMLoadingContainer<?>>>> iterator1 = texture2ContainerListMap.object2ObjectEntrySet().fastIterator();
		while (iterator1.hasNext()) {
			Object2ObjectMap.Entry<ResourceLocation, List<CTMLoadingContainer<?>>> entry = iterator1.next();
			ResourceLocation textureId = entry.getKey();
			List<CTMLoadingContainer<?>> containerList = entry.getValue();
			int amount2 = containerList.size();

			for (int i = 0; i < amount1; i++) {
				CTMLoadingContainer<?> container = VALID_FOR_MULTIPASS.get(i);
				if (container.getProperties().affectsTexture(textureId)) {
					for (int j = 0; j < amount2; j++) {
						CTMLoadingContainer<?> container1 = containerList.get(j);
						container1.addMultipassDependent(container);
					}
				}
			}
		}

		for (int i = 0; i < amount; i++) {
			CTMLoadingContainer<?> container = ALL.get(i);
			container.resolveRecursiveMultipassDependents();
		}
	}

	public static void consumeAllAffecting(IBlockState state, Consumer<CTMLoadingContainer<?>> consumer) {
		int amount = AFFECTS_BLOCK.size();
		for (int i = 0; i < amount; i++) {
			CTMLoadingContainer<?> container = AFFECTS_BLOCK.get(i);
			if (container.getProperties().affectsBlockState(state)) {
				consumer.accept(container);
			}
		}
	}

	@Nullable
	public static List<CTMLoadingContainer<?>> getAllAffecting(IBlockState state) {
		consumeAllAffecting(state, LIST_CREATOR);
		return LIST_CREATOR.get();
	}

	public static void consumeAllAffecting(Collection<ResourceLocation> spriteIds, Consumer<CTMLoadingContainer<?>> consumer) {
		int amount = IGNORES_BLOCK.size();
		for (int i = 0; i < amount; i++) {
			CTMLoadingContainer<?> container = IGNORES_BLOCK.get(i);
			for (ResourceLocation spriteId : spriteIds) {
				if (container.getProperties().affectsTexture(spriteId)) {
					consumer.accept(container);
					break;
				}
			}
		}
	}

	@Nullable
	public static List<CTMLoadingContainer<?>> getAllAffecting(Collection<ResourceLocation> spriteIds) {
		consumeAllAffecting(spriteIds, LIST_CREATOR);
		return LIST_CREATOR.get();
	}

	public static boolean isEmpty() {
		return ALL.isEmpty();
	}

	@ApiStatus.Internal
	public static void clearAll() {
		ALL.clear();
		AFFECTS_BLOCK.clear();
		IGNORES_BLOCK.clear();
		VALID_FOR_MULTIPASS.clear();
	}

	private static class OptionalListCreator<T> implements Consumer<T> {
		private ObjectArrayList<T> list = null;

		@Override
		public void accept(T t) {
			if (list == null) {
				list = new ObjectArrayList<>();
			}
			list.add(t);
		}

		@Nullable
		public ObjectArrayList<T> get() {
			ObjectArrayList<T> list = this.list;
			this.list = null;
			return list;
		}
	}
}
