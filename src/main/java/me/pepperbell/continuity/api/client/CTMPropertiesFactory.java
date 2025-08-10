package me.pepperbell.continuity.api.client;

import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

public interface CTMPropertiesFactory<T extends CTMProperties> {
	@Nullable
	T createProperties(Properties properties, ResourceLocation id, String packName, int packPriority, String method);
}
