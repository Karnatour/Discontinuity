package me.pepperbell.continuity.client.processor;

import me.pepperbell.continuity.api.client.ProcessingDataProvider;
import me.pepperbell.continuity.client.properties.BaseCTMProperties;
import me.pepperbell.continuity.client.util.biome.BiomeRetriever;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

import static me.pepperbell.continuity.client.processor.DirectionMaps.rotateAround;

public class BaseProcessingPredicate implements ProcessingPredicate {
	protected Set<ResourceLocation> matchTilesSet;
	protected EnumSet<EnumFacing> faces;
	protected Predicate<Biome> biomePredicate;
	protected IntPredicate heightPredicate;
	protected Predicate<String> blockEntityNamePredicate;

	public BaseProcessingPredicate(Set<ResourceLocation> matchTilesSet, EnumSet<EnumFacing> faces, Predicate<Biome> biomePredicate, IntPredicate heightPredicate, Predicate<String> blockEntityNamePredicate) {
		this.matchTilesSet = matchTilesSet;
		this.faces = faces;
		this.biomePredicate = biomePredicate;
		this.heightPredicate = heightPredicate;
		this.blockEntityNamePredicate = blockEntityNamePredicate;
	}

	@Override
	public boolean shouldProcessQuad(QuadView quad, TextureAtlasSprite sprite, World blockView, IBlockState state, BlockPos pos, ProcessingDataProvider dataProvider) {
		if (matchTilesSet != null) {
			if (!matchTilesSet.contains(sprite.getIconName())) {
				return false;
			}
		}
		if (heightPredicate != null) {
			if (!heightPredicate.test(pos.getY())) {
				return false;
			}
		}
		if (faces != null) {
			EnumFacing face = quad.lightFace();
			if (state.getProperties().containsKey(BlockRotatedPillar.AXIS)) {
				EnumFacing.Axis axis = state.getValue(BlockRotatedPillar.AXIS);
				if (axis == EnumFacing.Axis.X) {
					face = rotateAround(face, EnumFacing.Axis.Z, true); // Clockwise around Z
				} else if (axis == EnumFacing.Axis.Z) {
					face = rotateAround(face, EnumFacing.Axis.X, false); // Counterclockwise around X
				}
			}
			if (!faces.contains(face)) {
				return false;
			}
		}
		if (biomePredicate != null) {
			Biome biome = dataProvider.getData(ProcessingDataKeys.BIOME_CACHE_KEY).get(blockView, pos);
			if (biome == null || !biomePredicate.test(biome)) {
				return false;
			}
		}
		if (blockEntityNamePredicate != null) {
			String blockEntityName = dataProvider.getData(ProcessingDataKeys.BLOCK_ENTITY_NAME_CACHE_KEY).get(blockView, pos);
			if (blockEntityName == null || !blockEntityNamePredicate.test(blockEntityName)) {
				return false;
			}
		}
		return true;
	}

	public static BaseProcessingPredicate fromProperties(BaseCTMProperties properties) {
		return new BaseProcessingPredicate(properties.getMatchTilesSet(), properties.getFaces(), properties.getBiomePredicate(), properties.getHeightPredicate(), properties.getBlockEntityNamePredicate());
	}

	public static class BiomeCache {
		protected Biome biome;
		protected boolean invalid = true;

		@Nullable
		public Biome get(World blockView, BlockPos pos) {
			if (invalid) {
				biome = BiomeRetriever.getBiome(blockView, pos);
				invalid = false;
			}
			return biome;
		}

		public void reset() {
			invalid = true;
		}
	}

	public static class BlockEntityNameCache {
		protected String blockEntityName;
		protected boolean invalid = true;

		@Nullable
		public String get(World world, BlockPos pos) {
			if (invalid) {
				TileEntity tileEntity = world.getTileEntity(pos);
				if (tileEntity instanceof IWorldNameable nameable) {
                    if (nameable.hasCustomName()) {
						blockEntityName = nameable.getName();
					} else {
						blockEntityName = null;
					}
				} else {
					blockEntityName = null;
				}
				invalid = false;
			}
			return blockEntityName;
		}

		public void reset() {
			invalid = true;
		}
	}


}
