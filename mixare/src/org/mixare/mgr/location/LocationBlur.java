/*
 * Copyright (C) 2012- Peer internet solutions
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
package org.mixare.mgr.location;

import java.math.BigDecimal;
import java.util.Random;

/**
 * This class is used to blur the position of the user and make the requests
 * with a slightly different position. Markers however will be positioned in the
 * right spot.
 * 
 * @author KlemensE
 * 
 */
public class LocationBlur {
	/* The radius of the earth */
	private final static double radius = 6371;
	/* The different possible distances */
	private final static double[] distanceLevel = { 0.25, 0.5, 0.8 };

	/**
	 * This method truncates the latitude and longitude to blur the position of
	 * the user.
	 * 
	 * @param lat
	 *            The latitude of the position
	 * @param lng
	 *            The longitude of the position
	 * @param level
	 *            The level how much the position should be truncated
	 * @return Double[2], [0] is truncated lat, [1] is truncated lng
	 */
	public static double[] truncateLocation(double lat, double lng) {
		BigDecimal blurredLat = new BigDecimal(lat);
		blurredLat = blurredLat.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		BigDecimal blurredLng = new BigDecimal(lng);
		blurredLng = blurredLng.setScale(2, BigDecimal.ROUND_HALF_EVEN);

		return new double[] { blurredLat.doubleValue(),
				blurredLng.doubleValue() };
	}

	/**
	 * This method adds a random distance in a random bearing to the position.
	 * 
	 * Formula:
	 * lat2 = asin(sin(lat1)*cos(d/R) + cos(lat1)*sin(d/R)*cos(θ))
	 * lon2 = lon1 + atan2(sin(θ)*sin(d/R)*cos(lat1), cos(d/R)−sin(lat1)*sin(lat2))
	 * 
	 * θ is the bearing (in radians, clockwise from north);
	 * d/R is the angular distance (in radians), where d is the distance travelled and R is the earth’s radius
	 * 
	 * @param lat
	 *            The latitude of the position
	 * @param lng
	 *            The longitude of the position
	 * @param level
	 *            The level of how should be added to the position
	 * @return
	 */
	public static double[] addRandomDistance(double lat, double lng) {
		Random rand = new Random();
		double dist = (rand.nextDouble() * distanceLevel[rand.nextInt(3)])
				/ radius;
		double bearing = Math.toRadians(rand.nextInt(360));
		lat = Math.toRadians(lat);
		lng = Math.toRadians(lng);

		double lat2 = Math.asin(Math.sin(lat) * Math.cos(dist) + 
					Math.cos(lat) * Math.sin(dist) * Math.cos(bearing));
		double lng2 = lng
				+ Math.atan2(
						Math.sin(bearing) * Math.sin(dist) * Math.cos(lat),
						Math.cos(dist) - Math.sin(lat) * Math.sin(lat2));

		lng2 = (lng2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

		return new double[] { Math.toDegrees(lat2), Math.toDegrees(lng2) };
	}
}