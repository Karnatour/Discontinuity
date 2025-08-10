package me.pepperbell.continuity.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public final class TextureUtil {
	public static final ResourceLocation MISSING_SPRITE_ID = new ResourceLocation("missingno");

	public static ResourceLocation toSpriteId(ResourceLocation id) {
		return id;
	}

	public static boolean isMissingSprite(TextureAtlasSprite sprite) {
		TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
		return sprite == missing;
	}
}
