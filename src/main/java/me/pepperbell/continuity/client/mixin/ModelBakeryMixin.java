package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.client.resource.ModelWrappingHandler;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.BlockStateMapper;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Inject(method = "loadBlock", at = @At("RETURN"))
    private void continuity$afterLoadModel(BlockStateMapper blockstatemapper, Block block, ResourceLocation resourcelocation, CallbackInfo ci) {
        if (resourcelocation instanceof ModelResourceLocation) {
            Map<IBlockState, ModelResourceLocation> map = blockstatemapper.getVariants(block);
            for(Map.Entry<IBlockState, ModelResourceLocation> entry : map.entrySet()) {
                ModelWrappingHandler.onAddBlockStateModel(resourcelocation, entry.getKey());
            }
        }
    }
}
