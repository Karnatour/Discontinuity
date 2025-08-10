package me.pepperbell.continuity.client.properties;

import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.processor.ConnectionPredicate;
import me.pepperbell.continuity.client.util.SpriteCalculator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Locale;
import java.util.Properties;

public class ConnectingCTMProperties extends BaseCTMProperties {
	protected ConnectionPredicate connectionPredicate;

	public ConnectingCTMProperties(Properties properties, ResourceLocation id, String packName, int packPriority, String method) {
		super(properties, id, packName, packPriority, method);
	}

	@Override
	public void init() {
		super.init();
		parseConnect();
		detectConnect();
		validateConnect();
	}

	protected void parseConnect() {
		String connectStr = properties.getProperty("connect");
		if (connectStr == null) {
			return;
		}

		try {
			connectionPredicate = ConnectionType.valueOf(connectStr.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			//
		}
	}

	protected void detectConnect() {
		if (connectionPredicate == null) {
			if (affectsBlockStates()) {
				connectionPredicate = ConnectionType.BLOCK;
			} else if (affectsTextures()) {
				connectionPredicate = ConnectionType.TILE;
			}
		}
	}

	protected void validateConnect() {
		if (connectionPredicate == null) {
			ContinuityClient.LOGGER.error("No valid connection type provided in file '" + id + "' in pack '" + packName + "'");
			valid = false;
		}
	}

	public ConnectionPredicate getConnectionPredicate() {
		return connectionPredicate;
	}

	public enum ConnectionType implements ConnectionPredicate {
		BLOCK {
			@Override
			public boolean shouldConnect(World blockView, IBlockState state, BlockPos pos, IBlockState toState, EnumFacing face, TextureAtlasSprite quadSprite) {
				return state.getBlock() == toState.getBlock();
			}
		},
		TILE {
			@Override
			public boolean shouldConnect(World blockView, IBlockState state, BlockPos pos, IBlockState toState, EnumFacing face, TextureAtlasSprite quadSprite) {
				if (state == toState) {
					return true;
				}
				return quadSprite == SpriteCalculator.getSprite(toState, face);
			}
		},
		MATERIAL {
			@Override
			public boolean shouldConnect(World blockView, IBlockState state, BlockPos pos, IBlockState toState, EnumFacing face, TextureAtlasSprite quadSprite) {
				return state.getMaterial() == toState.getMaterial();
			}
		},
		STATE {
			@Override
			public boolean shouldConnect(World blockView, IBlockState state, BlockPos pos, IBlockState toState, EnumFacing face, TextureAtlasSprite quadSprite) {
				return state == toState;
			}
		};
	}
}
