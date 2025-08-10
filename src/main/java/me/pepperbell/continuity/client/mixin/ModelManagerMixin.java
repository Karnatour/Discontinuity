package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.client.util.SpriteCalculator;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.resources.IResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
	@Inject(method = "onResourceManagerReload", at = @At("HEAD"))
	private void continuity$onModelReload(IResourceManager resourceManager, CallbackInfo ci) {
		SpriteCalculator.clearCache();
	}
}
