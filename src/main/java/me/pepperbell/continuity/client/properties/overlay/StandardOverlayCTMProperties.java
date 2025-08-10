package me.pepperbell.continuity.client.properties.overlay;

import me.pepperbell.continuity.client.properties.ConnectingCTMProperties;
import me.pepperbell.continuity.client.properties.PropertiesParsingHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

public class StandardOverlayCTMProperties extends ConnectingCTMProperties implements OverlayPropertiesSection.Provider {
	protected OverlayPropertiesSection overlaySection;
	protected Set<ResourceLocation> connectTilesSet;
	protected Predicate<IBlockState> connectBlocksPredicate;

	public StandardOverlayCTMProperties(Properties properties, ResourceLocation id, String packName, int packPriority, String method) {
		super(properties, id, packName, packPriority, method);
		overlaySection = new OverlayPropertiesSection(properties, id, packName);
	}

	@Override
	public void init() {
		super.init();
		overlaySection.init();
		parseConnectTiles();
		parseConnectBlocks();
	}

	@Override
	public OverlayPropertiesSection getOverlayPropertiesSection() {
		return overlaySection;
	}

	protected void parseConnectTiles() {
		connectTilesSet = PropertiesParsingHelper.parseMatchTiles(properties, "connectTiles", id, packName);
	}

	protected void parseConnectBlocks() {
		connectBlocksPredicate = PropertiesParsingHelper.parseBlockStates(properties, "connectBlocks", id, packName);
	}

	public Set<ResourceLocation> getConnectTilesSet() {
		return connectTilesSet;
	}

	public Predicate<IBlockState> getConnectBlocksPredicate() {
		return connectBlocksPredicate;
	}
}
