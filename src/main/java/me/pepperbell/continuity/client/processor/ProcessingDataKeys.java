package me.pepperbell.continuity.client.processor;

import me.pepperbell.continuity.api.client.ProcessingDataKey;
import me.pepperbell.continuity.api.client.ProcessingDataKeyRegistry;
import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.processor.overlay.SimpleOverlayQuadProcessor;
import me.pepperbell.continuity.client.processor.overlay.StandardOverlayQuadProcessor;
import net.minecraft.util.math.BlockPos;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ProcessingDataKeys {
	public static final ProcessingDataKey<BlockPos.MutableBlockPos> MUTABLE_POS_KEY = create("mutable_pos", BlockPos.MutableBlockPos::new);
	public static final ProcessingDataKey<BaseProcessingPredicate.BiomeCache> BIOME_CACHE_KEY = create("biome_cache", BaseProcessingPredicate.BiomeCache::new, BaseProcessingPredicate.BiomeCache::reset);
	public static final ProcessingDataKey<BaseProcessingPredicate.BlockEntityNameCache> BLOCK_ENTITY_NAME_CACHE_KEY = create("block_entity_name_cache", BaseProcessingPredicate.BlockEntityNameCache::new, BaseProcessingPredicate.BlockEntityNameCache::reset);
	public static final ProcessingDataKey<CompactCTMQuadProcessor.VertexContainer> VERTEX_CONTAINER_KEY = create("vertex_container", CompactCTMQuadProcessor.VertexContainer::new);
	public static final ProcessingDataKey<StandardOverlayQuadProcessor.OverlayRendererPool> STANDARD_OVERLAY_RENDERER_POOL_KEY = create("standard_overlay_renderer_pool", StandardOverlayQuadProcessor.OverlayRendererPool::new, StandardOverlayQuadProcessor.OverlayRendererPool::reset);
	public static final ProcessingDataKey<SimpleOverlayQuadProcessor.OverlayRendererPool> SIMPLE_OVERLAY_RENDERER_POOL_KEY = create("simple_overlay_renderer_pool", SimpleOverlayQuadProcessor.OverlayRendererPool::new, SimpleOverlayQuadProcessor.OverlayRendererPool::reset);

	private static <T> ProcessingDataKey<T> create(String id, Supplier<T> valueSupplier) {
		return ProcessingDataKeyRegistry.get().registerKey(ContinuityClient.asId(id), valueSupplier);
	}

	private static <T> ProcessingDataKey<T> create(String id, Supplier<T> valueSupplier, Consumer<T> valueResetAction) {
		return ProcessingDataKeyRegistry.get().registerKey(ContinuityClient.asId(id), valueSupplier, valueResetAction);
	}
}
