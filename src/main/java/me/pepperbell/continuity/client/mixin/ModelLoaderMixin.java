package me.pepperbell.continuity.client.mixin;

import me.pepperbell.continuity.client.resource.CTMPropertiesLoader;
import me.pepperbell.continuity.client.resource.EmissiveSuffixLoader;
import me.pepperbell.continuity.client.resource.ModelWrappingHandler;
import me.pepperbell.continuity.client.resource.ResourcePackUtil;
import me.pepperbell.continuity.client.util.biome.BiomeHolderManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
	@Shadow
	@Final
	private Map<ResourceLocation, IModel> stateModels;

	@Inject(method = "<init>", at = @At(value = "TAIL"))
	private void continuity$onInit(IResourceManager resourceManagerIn, TextureMap p_i46085_2, BlockModelShapes p_i46085_3, CallbackInfo ci) {
		// TODO: move these to the very beginning of resource reload
		ResourcePackUtil.setup(resourceManagerIn);
		BiomeHolderManager.clearCache();

		EmissiveSuffixLoader.load(resourceManagerIn);
		CTMPropertiesLoader.clearAll();
		CTMPropertiesLoader.loadAll(resourceManagerIn);
	}


	@Inject(method = "onPostBakeEvent", at = @At("TAIL"))
	private void continuity$onFinishLoadingModels(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry, CallbackInfo ci) {
		ModelWrappingHandler.wrapCTMModels(stateModels);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void continuity$onConstructorReturn(IResourceManager p_i46085_1, TextureMap p_i46085_2, BlockModelShapes p_i46085_3, CallbackInfo ci) {
		Map<ResourceLocation, TextureAtlasSprite> sprites = ((ModelBakeryAccessor) this).getSprites();
		ModelWrappingHandler.wrapEmissiveModels(sprites, stateModels);

		CTMPropertiesLoader.clearAll();

		// TODO: move these to the very end of resource reload
		ResourcePackUtil.clear();
		BiomeHolderManager.refreshHolders();
	}
}