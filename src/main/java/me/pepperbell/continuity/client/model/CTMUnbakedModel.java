package me.pepperbell.continuity.client.model;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.pepperbell.continuity.api.client.QuadProcessor;
import me.pepperbell.continuity.client.resource.CTMLoadingContainer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CTMUnbakedModel extends WrappingUnbakedModel {
	private final List<CTMLoadingContainer<?>> containerList;
	@Nullable
	private final List<CTMLoadingContainer<?>> multipassContainerList;

	public CTMUnbakedModel(IModel wrapped, List<CTMLoadingContainer<?>> containerList, @Nullable List<CTMLoadingContainer<?>> multipassContainerList) {
		super(wrapped);
		this.containerList = containerList;
		this.multipassContainerList = multipassContainerList;
	}

	@Override
	public Set<ResourceLocation> getTextures() {
		Set<ResourceLocation> dependencies = new ObjectOpenHashSet<>(wrapped.getTextures());
		for (CTMLoadingContainer<?> container : containerList) {
			dependencies.addAll(container.getProperties().getTextureDependencies());
		}
		if (multipassContainerList != null) {
			for (CTMLoadingContainer<?> container : multipassContainerList) {
				dependencies.addAll(container.getProperties().getTextureDependencies());
			}
		}
		return dependencies;
	}

	@Nullable
	@Override
	public IBakedModel wrapBaked(@Nullable IBakedModel bakedWrapped, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
		if (bakedWrapped == null || bakedWrapped.isBuiltInRenderer()) {
			return bakedWrapped;
		}
		return new CTMBakedModel(bakedWrapped, toProcessorList(containerList, textureGetter), multipassContainerList == null ? null : toProcessorList(multipassContainerList, textureGetter));
	}

	protected static ImmutableList<QuadProcessor> toProcessorList(List<CTMLoadingContainer<?>> containerList, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
		ImmutableList.Builder<QuadProcessor> listBuilder = ImmutableList.builder();
		for (CTMLoadingContainer<?> container : containerList) {
			listBuilder.add(container.toProcessor(textureGetter));
		}
		return listBuilder.build();
	}
}
