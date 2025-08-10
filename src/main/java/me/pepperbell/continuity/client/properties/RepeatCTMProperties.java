package me.pepperbell.continuity.client.properties;

import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.processor.Symmetry;
import net.minecraft.util.ResourceLocation;

import java.util.Properties;

public class RepeatCTMProperties extends BaseCTMProperties {
	protected int width;
	protected int height;
	protected Symmetry symmetry = Symmetry.NONE;

	public RepeatCTMProperties(Properties properties, ResourceLocation id, String packName, int packPriority, String method) {
		super(properties, id, packName, packPriority, method);
	}

	@Override
	public void init() {
		super.init();
		parseWidth();
		parseHeight();
		parseSymmetry();
	}

	protected void parseWidth() {
		String widthStr = properties.getProperty("width");
		if (widthStr == null) {
			ContinuityClient.LOGGER.error("No 'width' value provided in file '" + id + "' in pack '" + packName + "'");
			valid = false;
			return;
		}

		try {
			int width = Integer.parseInt(widthStr.trim());
			if (width > 0) {
				this.width = width;
				return;
			}
		} catch (NumberFormatException e) {
			//
		}
		ContinuityClient.LOGGER.error("Invalid 'width' value '" + widthStr + "' in file '" + id + "' in pack '" + packName + "'");
		valid = false;
	}

	protected void parseHeight() {
		String heightStr = properties.getProperty("height");
		if (heightStr == null) {
			ContinuityClient.LOGGER.error("No 'height' value provided in file '" + id + "' in pack '" + packName + "'");
			valid = false;
			return;
		}

		try {
			int height = Integer.parseInt(heightStr.trim());
			if (height > 0) {
				this.height = height;
				return;
			}
		} catch (NumberFormatException e) {
			//
		}
		ContinuityClient.LOGGER.error("Invalid 'height' value '" + heightStr + "' in file '" + id + "' in pack '" + packName + "'");
		valid = false;
	}

	protected void parseSymmetry() {
		Symmetry symmetry = PropertiesParsingHelper.parseSymmetry(properties, "symmetry", id, packName);
		if (symmetry != null) {
			this.symmetry = symmetry;
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Symmetry getSymmetry() {
		return symmetry;
	}

	public static class Validator<T extends RepeatCTMProperties> implements TileAmountValidator<T> {
		@Override
		public boolean validateTileAmount(int amount, T properties) {
			int targetAmount = properties.getWidth() * properties.getHeight();
			if (amount == targetAmount) {
				return true;
			}
			ContinuityClient.LOGGER.error("Method '" + properties.getMethod() + "' requires exactly " + targetAmount + " tiles but " + amount + " were provided in file '" + properties.getId() + "' in pack '" + properties.getPackName() + "'");
			return false;
		}
	}
}
