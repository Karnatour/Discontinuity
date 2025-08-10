package me.pepperbell.continuity.client.mixin;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistrySimple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {

    @Accessor("bakedRegistry")
    RegistrySimple<ModelResourceLocation, IBakedModel> getBakedRegistry();

    @Accessor("resourceManager")
    IResourceManager getResourceManager();

    @Accessor("sprites")
    Map<ResourceLocation, TextureAtlasSprite> getSprites();

}
