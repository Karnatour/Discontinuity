package me.pepperbell.continuity.client.mixinterface;

import me.pepperbell.continuity.client.resource.ResourceRedirectHandler;
import org.jetbrains.annotations.Nullable;

public interface LifecycledResourceManagerImplExtension {
	@Nullable
	ResourceRedirectHandler continuity$getRedirectHandler();
}
