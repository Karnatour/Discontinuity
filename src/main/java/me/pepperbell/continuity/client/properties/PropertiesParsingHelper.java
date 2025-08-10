package me.pepperbell.continuity.client.properties;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.processor.Symmetry;
import me.pepperbell.continuity.client.resource.ResourceRedirectHandler;
import net.minecraft.block.Block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.IntFunction;
import java.util.function.Predicate;

public final class PropertiesParsingHelper {
	public static final Predicate<IBlockState> EMPTY_BLOCK_STATE_PREDICATE = state -> false;

	@Nullable
	public static ImmutableSet<ResourceLocation> parseMatchTiles(Properties properties, String propertyKey, ResourceLocation fileLocation, String packName) {
		String matchTilesStr = properties.getProperty(propertyKey);
		if (matchTilesStr == null) {
			return null;
		}

		String[] matchTileStrs = matchTilesStr.trim().split(" ");
		if (matchTileStrs.length != 0) {
			String basePath = FilenameUtils.getPath(fileLocation.getPath());
			ResourceRedirectHandler redirectHandler = ResourceRedirectHandler.get();
			ImmutableSet.Builder<ResourceLocation> setBuilder = ImmutableSet.builder();

			for (int i = 0; i < matchTileStrs.length; i++) {
				String matchTileStr = matchTileStrs[i];
				if (!matchTileStr.isEmpty()) {
					String[] parts = matchTileStr.split(":", 2);
					if (parts.length != 0) {
						String namespace;
						String path;
						if (parts.length > 1) {
							namespace = parts[0];
							path = parts[1];
						} else {
							namespace = null;
							path = parts[0];
						}

						if (path.endsWith(".png")) {
							path = path.substring(0, path.length() - 4);
						}
						if (path.startsWith("./")) {
							path = basePath + path.substring(2);
						} else if (path.startsWith("~/")) {
							path = "optifine/" + path.substring(2);
						} else if (path.startsWith("/")) {
							path = "optifine/" + path.substring(1);
						} else if (!path.contains("/")) {
							path = "textures/block/" + path;
						}
						if (path.startsWith("textures/")) {
							path = path.substring(9);
						} else if (path.startsWith("optifine/")) {
							if (redirectHandler == null) {
								continue;
							}
							path = redirectHandler.getSourceSpritePath(path + ".png");
							if (namespace == null) {
								namespace = fileLocation.getNamespace();
							}
						}

						try {
							setBuilder.add(new ResourceLocation(namespace, path));
							continue;
						} catch (Exception e) {
							//
						}
					}
					ContinuityClient.LOGGER.warn("Invalid '" + propertyKey + "' element '" + matchTileStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packName + "'");
				}
			}

			return setBuilder.build();
		}
		return ImmutableSet.of();
	}

	@Nullable
	public static Predicate<IBlockState> parseBlockStates(Properties properties, String propertyKey, ResourceLocation fileLocation, String packName) {
		String blockStatesStr = properties.getProperty(propertyKey);
		if (blockStatesStr == null) {
			return null;
		}

		String[] blockStateStrs = blockStatesStr.trim().split(" ");
		if (blockStateStrs.length != 0) {
			ImmutableList.Builder<Predicate<IBlockState>> predicateListBuilder = ImmutableList.builder();

			Block:
			for (int i = 0; i < blockStateStrs.length; i++) {
				String blockStateStr = blockStateStrs[i].trim();
				if (!blockStateStr.isEmpty()) {
					String[] parts = blockStateStr.split(":");
					if (parts.length != 0) {
						ResourceLocation blockId;
						int startIndex;
						try {
							if (parts.length == 1 || parts[1].contains("=")) {
								blockId = new ResourceLocation(parts[0]);
								startIndex = 1;
							} else {
								blockId = new ResourceLocation(parts[0], parts[1]);
								startIndex = 2;
							}
						} catch (Exception e) {
							ContinuityClient.LOGGER.warn("Invalid '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packName + "'", e);
							continue;
						}
						Block block = Block.REGISTRY.getObject(blockId);
						if (block != Blocks.AIR) {
							if (parts.length > startIndex) {
								ImmutableMap.Builder<IProperty<?>, Comparable<?>[]> propertyMapBuilder = ImmutableMap.builder();

								for (int j = startIndex; j < parts.length; j++) {
									String part = parts[j];
									if (!part.isEmpty()) {
										String[] propertyParts = part.split("=", 2);
										if (propertyParts.length == 2) {
											String propertyName = propertyParts[0];
											IProperty<?> property = block.getBlockState().getProperty(propertyName);
											if (property != null) {
												String propertyValuesStr = propertyParts[1];
												String[] propertyValueStrs = propertyValuesStr.split(",");
												if (propertyValueStrs.length != 0) {
													ImmutableList.Builder<Comparable<?>> valueListBuilder = ImmutableList.builder();

													for (String propertyValueStr : propertyValueStrs) {
														Optional<? extends Comparable<?>> optional = property.parseValue(propertyValueStr);
														if (optional.isPresent()) {
															valueListBuilder.add(optional.get());
														} else {
															ContinuityClient.LOGGER.warn("Invalid block property value '" + propertyValueStr + "' for property '" + propertyName + "' for block '" + blockId + "' in '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packName + "'");
															continue Block;
														}
													}

													ImmutableList<Comparable<?>> valueList = valueListBuilder.build();
													Comparable<?>[] valueArray = valueList.toArray(Comparable<?>[]::new);
													propertyMapBuilder.put(property, valueArray);
												}
											} else {
												ContinuityClient.LOGGER.warn("Unknown block property '" + propertyName + "' for block '" + blockId + "' in '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packName + "'");
												continue Block;
											}
										} else {
											ContinuityClient.LOGGER.warn("Invalid block property definition for block '" + blockId + "' in '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packName + "'");
											continue Block;
										}
									}
								}

								ImmutableMap<IProperty<?>, Comparable<?>[]> propertyMap = propertyMapBuilder.build();
								if (!propertyMap.isEmpty()) {
									Map.Entry<IProperty<?>, Comparable<?>[]>[] propertyMapEntryArray = propertyMap.entrySet().toArray((IntFunction<Map.Entry<IProperty<?>, Comparable<?>[]>[]>) Map.Entry[]::new);
									predicateListBuilder.add(state -> {
										if (state.getBlock() == block) {
											Outer:
											for (Map.Entry<IProperty<?>, Comparable<?>[]> entry : propertyMapEntryArray) {
												Comparable<?> targetValue = state.getValue(entry.getKey());
												Comparable<?>[] valueArray = entry.getValue();
												for (Comparable<?> value : valueArray) {
													if (targetValue == value) {
														continue Outer;
													}
												}
												return false;
											}
											return true;
										}
										return false;
									});
								}
							} else {
								predicateListBuilder.add(state -> state.getBlock() == block);
							}
						} else {
							ContinuityClient.LOGGER.warn("Unknown block '" + blockId + "' in '" + propertyKey + "' element '" + blockStateStr + "' at index " + i + " in file '" + fileLocation + "' in pack '" + packName + "'");
						}
					}
				}
			}

			ImmutableList<Predicate<IBlockState>> predicateList = predicateListBuilder.build();
			if (!predicateList.isEmpty()) {
				Predicate<IBlockState>[] predicateArray = predicateList.toArray((IntFunction<Predicate<IBlockState>[]>) Predicate[]::new);
				return state -> {
					for (Predicate<IBlockState> predicate : predicateArray) {
						if (predicate.test(state)) {
							return true;
						}
					}
					return false;
				};
			}
		}
		return EMPTY_BLOCK_STATE_PREDICATE;
	}

	@Nullable
	public static Symmetry parseSymmetry(Properties properties, String propertyKey, ResourceLocation fileLocation, String packName) {
		String symmetryStr = properties.getProperty(propertyKey);
		if (symmetryStr == null) {
			return null;
		}

		try {
			return Symmetry.valueOf(symmetryStr.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			ContinuityClient.LOGGER.warn("Unknown '" + propertyKey + "' value '" + symmetryStr + "' in file '" + fileLocation + "' in pack '" + packName + "'");
		}
		return null;
	}

	public static boolean parseOptifineOnly(Properties properties, ResourceLocation fileLocation) {
		if (!fileLocation.getNamespace().equals("minecraft")) {
			return false;
		}

		String optifineOnlyStr = properties.getProperty("optifineOnly");
		if (optifineOnlyStr == null) {
			return false;
		}

		return Boolean.parseBoolean(optifineOnlyStr.trim());
	}
}
