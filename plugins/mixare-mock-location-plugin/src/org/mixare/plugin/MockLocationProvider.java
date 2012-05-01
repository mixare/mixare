/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
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
package org.mixare.plugin;

import java.io.IOException;
import java.util.List;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * MockLocation Provider registers a new LocationProvider for the telephone The
 * MockLocationProvider has an accuracy of 100%, so mixare will obviously choose
 * this provider as its main provider (GPS can not achieve this value).
 * 
 * @author A.Egal
 */
public class MockLocationProvider extends Thread {

	private List<String> data;

	private LocationManager locationManager;

	private String mocLocationProvider;

	private String LOG_TAG = "mixare-mock-location";

	public MockLocationProvider(LocationManager locationManager,
			String mocLocationProvider, List<String> data) throws IOException {
		this.locationManager = locationManager;
		this.mocLocationProvider = mocLocationProvider;
		this.data = data;
	}

	/**
	 * Changes the location every 30 seconds to the next entry of the ArrayList
	 */
	@Override
	public void run() {
		
		for (String str : data) {

			// Set one position
			String[] parts = str.split(",");
			Double latitude = Double.valueOf(parts[0]);
			Double longitude = Double.valueOf(parts[1]);
			Double altitude = Double.valueOf(parts[2]);
			Location location = new Location(mocLocationProvider);
			location.setLatitude(latitude);
			location.setLongitude(longitude);
			location.setAltitude(altitude);
			location.setAccuracy(100.0f); //set accuracy to max

			Log.e(LOG_TAG, location.toString());

			// set the time in the location. If the time on this location
			// matches the time on the one in the previous set call, it will be
			// ignored
			location.setTime(System.currentTimeMillis());

			locationManager.setTestProviderLocation(mocLocationProvider,
					location);

			try {
				Thread.sleep(30 * 1000);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		Log.e(LOG_TAG, "LOCATION MOCKING ENDED");
		locationManager.removeTestProvider(mocLocationProvider);
		MockLocationService.instance.stopSelf();
	}

}
