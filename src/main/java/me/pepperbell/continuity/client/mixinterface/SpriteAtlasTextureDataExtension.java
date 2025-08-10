package me.pepperbell.continuity.client.mixinterface;

import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface SpriteAtlasTextureDataExtension {
	@Nullable
	Map<ResourceLocation, ResourceLocation> continuity$getEmissiveIdMap();

	void continuity$setEmissiveIdMap(Map<ResourceLocation, ResourceLocation> map);
}
