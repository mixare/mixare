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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * A service that registers the MockLocationProvider, and will carefully
 * terminate it when closed.
 * 
 * @author A.Egal
 */
public class MockLocationService extends Service implements LocationListener {

	private String LOG_TAG = "mixare-mock-location";
	private String mocLocationProvider;
	private LocationManager locationManager;
	public static Service instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		mocLocationProvider = LocationManager.GPS_PROVIDER;
		locationManager.addTestProvider(mocLocationProvider, false, false,
				false, false, true, true, true, 0, 5);
		locationManager.setTestProviderEnabled(mocLocationProvider, true);
		locationManager.requestLocationUpdates(mocLocationProvider, 0, 0, this);
		Log.e(LOG_TAG, "STARTING");
		try {

			List<String> data = new ArrayList<String>();
			InputStream is = getAssets().open("data.txt");
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {
				Log.e(LOG_TAG, line);
				data.add(line);
			}
			Log.e(LOG_TAG, data.size() + " lines");

			new MockLocationProvider(locationManager, mocLocationProvider, data)
					.start();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onDestroy() {
		Log.e(LOG_TAG, "Terminating mock-location-provider");
		locationManager.removeTestProvider(mocLocationProvider);
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

}
