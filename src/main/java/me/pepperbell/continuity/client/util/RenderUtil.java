package me.pepperbell.continuity.client.util;

import me.pepperbell.continuity.client.resource.CustomBlockLayers;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.RenderMaterialImpl;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class RenderUtil{
    private static final BlockColors BLOCK_COLORS = Minecraft.getMinecraft().getBlockColors();

    private static final ThreadLocal<MaterialFinder> MATERIAL_FINDER = ThreadLocal.withInitial(() -> RendererAccess.INSTANCE.getRenderer().materialFinder());

    private static final BlendModeGetter BLEND_MODE_GETTER = createBlendModeGetter();

    private static SpriteFinder blockAtlasSpriteFinder;

    private static BlendModeGetter createBlendModeGetter() {
        //} else if (FabricLoader.getInstance().isModLoaded("indium")) {
        //return quad -> ((link.infra.indium.renderer.RenderMaterialImpl) quad.material()).blendMode(0);
        if (RendererAccess.INSTANCE.getRenderer() instanceof IndigoRenderer) {
            return quad -> BlendMode.fromRenderLayer(((RenderMaterialImpl) quad.material()).blendMode(0));
        }
        return quad -> BlendMode.DEFAULT;
    }

    public static int getTintColor(IBlockState state, World blockView, BlockPos pos, int tintIndex) {
        if (state == null || tintIndex == -1) {
            return -1;
        }
        return 0xFF000000 | BLOCK_COLORS.getColor(state, blockView, pos);
    }

    public static MaterialFinder getMaterialFinder() {
        return MATERIAL_FINDER.get().clear();
    }

    public static BlendMode getBlendMode(QuadView quad) {
        return BLEND_MODE_GETTER.getBlendMode(quad);
    }

    public static SpriteFinder getSpriteFinder() {
        return blockAtlasSpriteFinder;
    }

    private interface BlendModeGetter {

        BlendMode getBlendMode(QuadView quad);
    }

    public static class ReloadListener{
        private static final ReloadListener INSTANCE = new ReloadListener();

        public static void init() {
            MinecraftForge.EVENT_BUS.register(INSTANCE);
        }

        @SubscribeEvent
        public void onTextureStitch(TextureStitchEvent.Post event) {
            blockAtlasSpriteFinder = SpriteFinder.get(event.getMap());
        }
    }
}
