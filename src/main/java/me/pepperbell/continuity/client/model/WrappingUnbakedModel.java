package me.pepperbell.continuity.client.model;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;

public abstract class WrappingUnbakedModel implements IModel {
    protected final IModel wrapped;
    protected boolean isBaking;

    public WrappingUnbakedModel(IModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return wrapped.getDependencies();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        return wrapped.getTextures();
    }

    @Override
    @Nullable
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        if (isBaking) return null;
        isBaking = true;

        IBakedModel wrappedBaked = wrapped.bake(state, format, bakedTextureGetter);
        IBakedModel result = wrapBaked(wrappedBaked, state, format, bakedTextureGetter);

        isBaking = false;
        return result;
    }

    @Nullable
    public abstract IBakedModel wrapBaked(@Nullable IBakedModel bakedWrapped, IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> textureGetter);

}
