package me.pepperbell.continuity.client.resource;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.pepperbell.continuity.client.mixinterface.SpriteAtlasTextureDataExtension;
import me.pepperbell.continuity.client.model.CTMUnbakedModel;
import me.pepperbell.continuity.client.model.EmissiveUnbakedModel;
import me.pepperbell.continuity.client.util.VoidSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ModelWrappingHandler {
	private static final Map<ResourceLocation, IBlockState> MODEL_ID_2_STATE_MAP = new Object2ObjectOpenHashMap<>();
	private static final Map<ResourceLocation, List<CTMLoadingContainer<?>>> MODEL_ID_2_CONTAINERS_MAP = new Object2ObjectOpenHashMap<>();

	public static void onAddBlockStateModel(ResourceLocation id, IBlockState state) {
		MODEL_ID_2_STATE_MAP.put(id, state);
		List<CTMLoadingContainer<?>> containerList = CTMPropertiesLoader.getAllAffecting(state);
		if (containerList != null) {
			MODEL_ID_2_CONTAINERS_MAP.put(id, containerList);
		}
	}

	public static void wrapCTMModels(Map<ResourceLocation, IModel> unbakedModels) {
		if (CTMPropertiesLoader.isEmpty()) {
			clearMaps();
			return;
		}

		Map<ResourceLocation, IModel> wrappedModels = new Object2ObjectOpenHashMap<>();
		Function<ResourceLocation, IModel> unbakedModelGetter = createUnbakedModelGetter(unbakedModels);
		VoidSet<Pair<String, String>> voidSet = VoidSet.get();
		CollectionBasedConsumer<CTMLoadingContainer<?>> reusableConsumer = new CollectionBasedConsumer<>();

		unbakedModels.forEach((id, model) -> {
			// Only wrap final block state models
			if (id instanceof ModelResourceLocation modelId && isBlockStateModelId(modelId)) {
				Collection<ResourceLocation> dependencies;
				try {
					dependencies = model.getDependencies();
				} catch (ModelNotLoadedException e) {
					return;
				}

				List<CTMLoadingContainer<?>> containerList = MODEL_ID_2_CONTAINERS_MAP.get(modelId);
				if (containerList == null) {
					containerList = CTMPropertiesLoader.getAllAffecting(dependencies);
					if (containerList == null) {
						return;
					}
				} else {
					reusableConsumer.setCollection(containerList);
					CTMPropertiesLoader.consumeAllAffecting(dependencies, reusableConsumer);
				}
				containerList.sort(Collections.reverseOrder());

				Set<CTMLoadingContainer<?>> multipassContainerSet = null;
				int amount = containerList.size();
				for (int i = 0; i < amount; i++) {
					CTMLoadingContainer<?> container = containerList.get(i);
					Set<CTMLoadingContainer<?>> dependents = container.getRecursiveMultipassDependents();
					if (dependents != null) {
						if (multipassContainerSet == null) {
							multipassContainerSet = new ObjectOpenHashSet<>();
						}
						multipassContainerSet.addAll(dependents);
					}
				}
				List<CTMLoadingContainer<?>> multipassContainerList = null;
				if (multipassContainerSet != null) {
					IBlockState state = MODEL_ID_2_STATE_MAP.get(modelId);
					for (CTMLoadingContainer<?> container : multipassContainerSet) {
						if (!container.getProperties().affectsBlockStates() || container.getProperties().affectsBlockState(state)) {
							if (multipassContainerList == null) {
								multipassContainerList = new ObjectArrayList<>();
							}
							multipassContainerList.add(container);
						}
					}
					if (multipassContainerList != null) {
						multipassContainerList.sort(Collections.reverseOrder());
					}
				}

				wrappedModels.put(modelId, new CTMUnbakedModel(model, containerList, multipassContainerList));
			}
		});

		clearMaps();
		injectWrappedModels(wrappedModels, unbakedModels);
	}

	public static void wrapEmissiveModels(Map<ResourceLocation,TextureAtlasSprite> spriteAtlasData, Map<ResourceLocation, IModel> unbakedModels) {
		Set<ResourceLocation> spriteIdsToWrap = new ObjectOpenHashSet<>();

		spriteAtlasData.forEach((atlasId, sprite) -> {
            Map<ResourceLocation, ResourceLocation> emissiveIdMap = ((SpriteAtlasTextureDataExtension) sprite).continuity$getEmissiveIdMap();
			if (emissiveIdMap != null) {
				for (ResourceLocation id : emissiveIdMap.keySet()) {
					spriteIdsToWrap.add(new ResourceLocation(atlasId.getNamespace(), id.getNamespace() + "/" + id.getPath()));
				}
			}
		});

		if (spriteIdsToWrap.isEmpty()) {
			return;
		}

		Map<ResourceLocation, IModel> wrappedModels = new Object2ObjectOpenHashMap<>();

        unbakedModels.forEach((id, model) -> {
			Collection<ResourceLocation> dependencies;
			try {
				dependencies = model.getDependencies();
			} catch (ModelNotLoadedException e) {
				return;
			}

			for (ResourceLocation spriteId : dependencies) {
				if (spriteIdsToWrap.contains(spriteId)) {
					wrappedModels.put(id, new EmissiveUnbakedModel(model));
					return;
				}
			}
		});

		injectWrappedModels(wrappedModels, unbakedModels);
	}

	private static Function<ResourceLocation, IModel> createUnbakedModelGetter(Map<ResourceLocation, IModel> unbakedModels) {
		return id -> {
			IModel model = unbakedModels.get(id);
			if (model == null) {
				throw new ModelNotLoadedException();
			}
			return model;
		};
	}

	private static void injectWrappedModels(Map<ResourceLocation, IModel> wrappedModels, Map<ResourceLocation, IModel> unbakedModels) {
		wrappedModels.forEach(unbakedModels::replace);
	}

	private static boolean isBlockStateModelId(ModelResourceLocation id) {
		return !id.getVariant().equals("inventory");
	}

	private static void clearMaps() {
		MODEL_ID_2_STATE_MAP.clear();
		MODEL_ID_2_CONTAINERS_MAP.clear();
	}

	private static class ModelNotLoadedException extends RuntimeException {
	}

	private static class CollectionBasedConsumer<T> implements Consumer<T> {
		private Collection<T> collection;

		@Override
		public void accept(T t) {
			collection.add(t);
		}

		public void setCollection(Collection<T> collection) {
			this.collection = collection;
		}
	}
}
