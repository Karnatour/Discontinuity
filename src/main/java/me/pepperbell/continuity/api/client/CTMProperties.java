package me.pepperbell.continuity.api.client;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public interface CTMProperties extends Comparable<CTMProperties> {
	boolean affectsTextures();

	boolean affectsTexture(ResourceLocation id);

	boolean affectsBlockStates();

	boolean affectsBlockState(IBlockState state);

	Collection<ResourceLocation> getTextureDependencies();

	default boolean isValidForMultipass() {
		return true;
	}
}
