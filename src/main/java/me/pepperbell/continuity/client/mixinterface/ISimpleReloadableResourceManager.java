package me.pepperbell.continuity.client.mixinterface;

import me.pepperbell.continuity.client.resource.ResourceRedirectHandler;

public interface ISimpleReloadableResourceManager {
    ResourceRedirectHandler discontinuity$getRedirectHandler();
}