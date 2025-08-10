package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.api.client.ContinuityFeatureStates;
import net.minecraft.client.renderer.entity.RenderFallingBlock;
import net.minecraft.entity.item.EntityFallingBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderFallingBlock.class)
public class FallingBlockEntityRendererMixin {

	@Inject(method = "doRender(Lnet/minecraft/entity/item/EntityFallingBlock;DDDFF)V", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/BlockModelRenderer;renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z"))
	private void continuity$beforeRenderModel(EntityFallingBlock entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
		ContinuityFeatureStates states = ContinuityFeatureStates.get();
		states.getConnectedTexturesState().disable();
		states.getEmissiveTexturesState().disable();
	}

	@Inject(method = "doRender(Lnet/minecraft/entity/item/EntityFallingBlock;DDDFF)V", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/BlockModelRenderer;renderModel(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/BufferBuilder;ZJ)Z",
			shift = At.Shift.AFTER))
	private void continuity$afterRenderModel(EntityFallingBlock entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
		ContinuityFeatureStates states = ContinuityFeatureStates.get();
		states.getConnectedTexturesState().enable();
		states.getEmissiveTexturesState().enable();
	}
}
