package me.pepperbell.continuity.client.model;

import net.minecraft.client.renderer.block.model.BuiltInModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class EmissiveUnbakedModel extends WrappingUnbakedModel {
	public EmissiveUnbakedModel(IModel wrapped) {
		super(wrapped);
	}

	@Override
	@Nullable
	public IBakedModel wrapBaked(@Nullable IBakedModel bakedWrapped, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
		if (bakedWrapped == null || bakedWrapped.isBuiltInRenderer()) {
			return bakedWrapped;
		}
		return new EmissiveBakedModel(bakedWrapped);
	}
}
