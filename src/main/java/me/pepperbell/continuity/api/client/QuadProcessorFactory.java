package me.pepperbell.continuity.api.client;


import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public interface QuadProcessorFactory<T extends CTMProperties> {
	QuadProcessor createProcessor(T properties, Function<ResourceLocation, TextureAtlasSprite> textureGetter);
}
