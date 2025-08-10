package me.pepperbell.continuity.client.processor;

import me.pepperbell.continuity.client.util.QuadUtil;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.ArrayUtils;

public final class DirectionMaps {

	private static EnumFacing rotateXZ(EnumFacing facing) {
		switch (facing) {
			case DOWN: return EnumFacing.NORTH;
			case NORTH: return EnumFacing.UP;
			case UP: return EnumFacing.SOUTH;
			case SOUTH: return EnumFacing.DOWN;
			default: return facing;
		}
	}

	private static EnumFacing rotateZX(EnumFacing facing) {
		switch (facing) {
			case DOWN: return EnumFacing.SOUTH;
			case SOUTH: return EnumFacing.UP;
			case UP: return EnumFacing.NORTH;
			case NORTH: return EnumFacing.DOWN;
			default: return facing;
		}
	}

	private static EnumFacing rotateYZ(EnumFacing facing) {
		switch (facing) {
			case DOWN: return EnumFacing.EAST;
			case EAST: return EnumFacing.UP;
			case UP: return EnumFacing.WEST;
			case WEST: return EnumFacing.DOWN;
			default: return facing;
		}
	}

	private static EnumFacing rotateZY(EnumFacing facing) {
		switch (facing) {
			case DOWN: return EnumFacing.WEST;
			case WEST: return EnumFacing.UP;
			case UP: return EnumFacing.EAST;
			case EAST: return EnumFacing.DOWN;
			default: return facing;
		}
	}

	public static EnumFacing rotateAround(EnumFacing facing, EnumFacing.Axis axis, boolean clockwise) {
		if (axis == EnumFacing.Axis.Y) {
			// Horizontal rotation
			return clockwise ? facing.rotateY() : facing.rotateYCCW();
		}

		if (axis == EnumFacing.Axis.X) {
			// Rotation around X (Z <-> Y)
			if (facing.getAxis() == EnumFacing.Axis.Y || facing.getAxis() == EnumFacing.Axis.Z) {
				return clockwise
						? rotateXZ(facing)
						: rotateZX(facing);
			}
		}

		if (axis == EnumFacing.Axis.Z) {
			// Rotation around Z (X <-> Y)
			if (facing.getAxis() == EnumFacing.Axis.Y || facing.getAxis() == EnumFacing.Axis.X) {
				return clockwise
						? rotateYZ(facing)
						: rotateZY(facing);
			}
		}

		return facing;
	}


	public static final EnumFacing[][][] DIRECTION_MAPS = new EnumFacing[6][8][];
	static {
		for (EnumFacing face : EnumFacing.values()) {
			EnumFacing textureUp;
			if (face == EnumFacing.UP) {
				textureUp = EnumFacing.NORTH;
			} else if (face == EnumFacing.DOWN) {
				textureUp = EnumFacing.SOUTH;
			} else {
				textureUp = EnumFacing.UP;
			}

			EnumFacing textureLeft = rotateAround(textureUp, face.getAxis(), face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE);


			EnumFacing[][] map = DIRECTION_MAPS[face.ordinal()];

			map[0] = new EnumFacing[] { textureLeft, textureUp.getOpposite(), textureLeft.getOpposite(), textureUp }; // l d r u
			map[1] = map[0].clone(); // d r u l
			ArrayUtils.shift(map[1], -1);
			map[2] = map[1].clone(); // r u l d
			ArrayUtils.shift(map[2], -1);
			map[3] = map[2].clone(); // u l d r
			ArrayUtils.shift(map[3], -1);

			map[4] = map[0].clone(); // u r d l ; v - 1 ; h - 3
			ArrayUtils.reverse(map[4]);
			map[5] = map[4].clone(); // l u r d ; v - 0 ; h - 2
			ArrayUtils.shift(map[5], 1);
			map[6] = map[5].clone(); // d l u r ; v - 3 ; h - 1
			ArrayUtils.shift(map[6], 1);
			map[7] = map[6].clone(); // r d l u ; v - 2 ; h - 0
			ArrayUtils.shift(map[7], 1);
		}
	}

	public static EnumFacing[][] getMap(EnumFacing direction) {
		return DIRECTION_MAPS[direction.ordinal()];
	}

	public static EnumFacing[] getDirections(QuadView quad) {
		return getMap(quad.lightFace())[QuadUtil.getTextureOrientation(quad)];
	}
}
