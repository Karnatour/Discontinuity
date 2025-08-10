package me.pepperbell.continuity.client.properties.overlay;

import me.pepperbell.continuity.client.properties.BaseCTMProperties;
import net.minecraft.util.ResourceLocation;

import java.util.Properties;

public class BaseOverlayCTMProperties extends BaseCTMProperties implements OverlayPropertiesSection.Provider {
	protected OverlayPropertiesSection overlaySection;

	public BaseOverlayCTMProperties(Properties properties, ResourceLocation id, String packName, int packPriority, String method) {
		super(properties, id, packName, packPriority, method);
		overlaySection = new OverlayPropertiesSection(properties, id, packName);
	}

	@Override
	public void init() {
		super.init();
		overlaySection.init();
	}

	@Override
	public OverlayPropertiesSection getOverlayPropertiesSection() {
		return overlaySection;
	}
}
