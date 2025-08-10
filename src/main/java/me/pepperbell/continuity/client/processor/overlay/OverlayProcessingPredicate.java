package me.pepperbell.continuity.client.processor.overlay;

import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import me.pepperbell.continuity.client.processor.BaseProcessingPredicate;
import me.pepperbell.continuity.client.properties.BaseCTMProperties;
import me.pepperbell.continuity.client.util.QuadUtil;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class OverlayProcessingPredicate extends BaseProcessingPredicate {
	public OverlayProcessingPredicate(Set<ResourceLocation> matchTilesSet, EnumSet<EnumFacing> faces, Predicate<Biome> biomePredicate, IntPredicate heightPredicate, Predicate<String> blockEntityNamePredicate) {
		super(matchTilesSet, faces, biomePredicate, heightPredicate, blockEntityNamePredicate);
	}

	@Override
	public boolean shouldProcessQuad(QuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, ProcessingDataProvider dataProvider) {
		if (!super.shouldProcessQuad(quad, sprite, blockView, state, pos, dataProvider)) {
			return false;
		}
		return QuadUtil.isQuadUnitSquare(quad);
	}

	public static OverlayProcessingPredicate fromProperties(BaseCTMProperties properties) {
		return new OverlayProcessingPredicate(properties.getMatchTilesSet(), properties.getFaces(), properties.getBiomePredicate(), properties.getHeightPredicate(), properties.getBlockEntityNamePredicate());
	}
}
