package com.game.util;

public class MapUtil {

	/**
	 *
	 * @param x
	 * @param y
	 * @param targetX
	 * @param targetY
	 * @return
	 */
	public static int distance(int x, int y, int targetX, int targetY) {
		return Math.abs(targetX - x) + Math.abs(targetY - y);
	}

}
