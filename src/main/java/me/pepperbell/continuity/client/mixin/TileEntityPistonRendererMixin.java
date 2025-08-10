package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.api.client.ContinuityFeatureStates;
import net.minecraft.client.renderer.tileentity.TileEntityPistonRenderer;
import net.minecraft.tileentity.TileEntityPiston;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityPistonRenderer.class)
public class TileEntityPistonRendererMixin {

	@Inject(method = "render(Lnet/minecraft/tileentity/TileEntityPiston;DDDFIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/BufferBuilder;begin(ILnet/minecraft/client/renderer/vertex/VertexFormat;)V"))
	private void continuity$beforeRender(TileEntityPiston te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
		ContinuityFeatureStates states = ContinuityFeatureStates.get();
		states.getConnectedTexturesState().disable();
		states.getEmissiveTexturesState().disable();
	}

	@Inject(method = "render(Lnet/minecraft/tileentity/TileEntityPiston;DDDFIF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Tessellator;draw()V", shift = At.Shift.AFTER))
	private void continuity$afterRender(TileEntityPiston te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
		ContinuityFeatureStates states = ContinuityFeatureStates.get();
		states.getConnectedTexturesState().enable();
		states.getEmissiveTexturesState().enable();
	}
}
