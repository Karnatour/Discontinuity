package me.pepperbell.continuity.client.properties;

import net.minecraft.util.ResourceLocation;

import java.util.Properties;

public class StandardConnectingCTMProperties extends ConnectingCTMProperties {
	protected boolean innerSeams = false;

	public StandardConnectingCTMProperties(Properties properties, ResourceLocation id, String packName, int packPriority, String method) {
		super(properties, id, packName, packPriority, method);
	}

	@Override
	public void init() {
		super.init();
		parseInnerSeams();
	}

	protected void parseInnerSeams() {
		String innerSeamsStr = properties.getProperty("innerSeams");
		if (innerSeamsStr == null) {
			return;
		}

		innerSeams = Boolean.parseBoolean(innerSeamsStr.trim());
	}

	public boolean getInnerSeams() {
		return innerSeams;
	}
}
