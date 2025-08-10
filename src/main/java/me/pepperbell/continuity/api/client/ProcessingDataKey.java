package me.pepperbell.continuity.api.client;

import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

@ApiStatus.NonExtendable
public interface ProcessingDataKey<T> {
	ResourceLocation getId();

	int getRawId();

	Supplier<T> getValueSupplier();

	@Nullable
	Consumer<T> getValueResetAction();
}
