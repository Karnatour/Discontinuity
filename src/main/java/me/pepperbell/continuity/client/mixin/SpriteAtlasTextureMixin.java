package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.client.mixinterface.SpriteExtension;
import me.pepperbell.continuity.client.resource.EmissiveIdProvider;
import me.pepperbell.continuity.client.resource.EmissiveSuffixLoader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(TextureMap.class)
public abstract class SpriteAtlasTextureMixin {

	@Shadow
	@Final
	private Map<String, TextureAtlasSprite> mapUploadedSprites;

	@Unique
	private Map<String, String> continuity$emissiveIdMap;

	@Inject(method = "loadTextureAtlas", at = @At("HEAD"))
	private void continuity$beforeStitch(IResourceManager resourceManager, CallbackInfo ci) {
		String emissiveSuffix = EmissiveSuffixLoader.getEmissiveSuffix();
		if (emissiveSuffix == null) return;

		continuity$emissiveIdMap = new HashMap<>();

		for (String key : mapUploadedSprites.keySet()) {
			ResourceLocation id = new ResourceLocation(key);
			ResourceLocation emissiveId = EmissiveIdProvider.toEmissiveId(id, emissiveSuffix);
			if (emissiveId != null) {
				try {
					if (resourceManager.getResource(emissiveId) != null) {
						continuity$emissiveIdMap.put(id.toString(), emissiveId.toString());
						((TextureMap)(Object)this).registerSprite(emissiveId);
					}
				} catch (Exception ignored) {}
			}
		}
	}

	@Inject(method = "loadTextureAtlas", at = @At("RETURN"))
	private void continuity$afterStitch(IResourceManager resourceManager, CallbackInfo ci) {
		if (continuity$emissiveIdMap == null) return;

		for (Map.Entry<String, String> entry : continuity$emissiveIdMap.entrySet()) {
			TextureAtlasSprite base = mapUploadedSprites.get(entry.getKey());
			TextureAtlasSprite emissive = mapUploadedSprites.get(entry.getValue());

			if (base != null && emissive != null) {
				((SpriteExtension) base).continuity$setEmissiveSprite(emissive);
			}
		}

		continuity$emissiveIdMap = null;
	}
}
