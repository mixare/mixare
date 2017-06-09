/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.lib;

import android.util.FloatMath;

/**
 * This class has the ability to calculate the declination of a line between two
 * points. It is able to check if a point is in a given rectangle and it also can
 * make a String out of a given distance-value which contains number and unit.
 */
public class MixUtils {
	public static String parseAction(String action) {
		return (action.substring(action.indexOf(':') + 1, action.length()))
				.trim();
	}

	public static String formatDist(float meters) {
		if (meters < 1000) {
			return ((int) meters) + "m";
		} else if (meters < 10000) {
			return formatDec(meters / 1000f, 1) + "km";
		} else {
			return ((int) (meters / 1000f)) + "km";
		}
	}

	static String formatDec(float val, int dec) {
		int factor = (int) Math.pow(10, dec);

		int front = (int) (val );
		int back = (int) Math.abs(val * (factor) ) % factor;

		return front + "." + back;
	}

	public static boolean pointInside(float P_x, float P_y, float r_x,
		float r_y, float r_w, float r_h) {
		return (P_x > r_x && P_x < r_x + r_w && P_y > r_y && P_y < r_y + r_h);
	}

	public static float getAngle(float center_x, float center_y, float post_x,
			float post_y) {
		float tmpv_x = post_x - center_x;
		float tmpv_y = post_y - center_y;
		float d = (float) FloatMath.sqrt(tmpv_x * tmpv_x + tmpv_y * tmpv_y);
		float cos = tmpv_x / d;
		float angle = (float) Math.toDegrees(Math.acos(cos));

		angle = (tmpv_y < 0) ? angle * -1 : angle;

		return angle;
	}
}
