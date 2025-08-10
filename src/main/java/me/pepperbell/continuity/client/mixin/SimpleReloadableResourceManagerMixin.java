package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.client.mixinterface.ISimpleReloadableResourceManager;
import me.pepperbell.continuity.client.resource.ResourceRedirectHandler;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleReloadableResourceManager.class)
public abstract class SimpleReloadableResourceManagerMixin implements ISimpleReloadableResourceManager {
    @Unique
    private ResourceRedirectHandler continuity$redirectHandler;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.continuity$redirectHandler = new ResourceRedirectHandler();
    }

    @Override
    public ResourceRedirectHandler discontinuity$getRedirectHandler() {
        return this.continuity$redirectHandler;
    }
}
