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

package org.mixare;

import org.mixare.data.DataSource.DATASOURCE;

import android.location.Location;

/**
 * Represents a marker
 * 
 * @author hannes
 *
 */
public class POIMarker extends Marker {
	
	public static final int MAX_OBJECTS=20;

	public POIMarker(String title, double latitude, double longitude,
			double altitude, String URL, DATASOURCE datasource) {
		super(title, latitude, longitude, altitude, URL, datasource);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(Location curGPSFix) {
		super.update(curGPSFix);
	}

	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}

}
