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
package org.mixare;

import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * This class is repsonsible for finding the location, and sending it back to the mixcontext.
 * It will also 
 * @author A. Egal
 */
public class LocationFinder {

	private LocationManager lm;
	private String bestLocationProvider;
	private DownloadManager downloadManager;
	private MixView mixView;
	private Location curLoc;
	private Location locationAtLastDownload;

	// frequency and minimum distance for update
	// this values will only be used after there's a good GPS fix
	// see back-off pattern discussion
	// http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
	// thanks Reto Meier for his presentation at gddde 2010
	private final long freq = 5000; // 5 seconds
	private final float dist = 20; // 20 meters

	public LocationFinder(DownloadManager downloadManager, MixView mixView){
		this.downloadManager = downloadManager;
		this.mixView = mixView;
	}

	public Location findLocation(Context ctx) {
		lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

		// fallback for the case where GPS and network providers are disabled
		Location hardFix = new Location("reverseGeocoded");

		// Frangart, Eppan, Bozen, Italy
		hardFix.setLatitude(46.480302);
		hardFix.setLongitude(11.296005);
		hardFix.setAltitude(300);

		try {
			requestBestLocationUpdates();
			curLoc = lm.getLastKnownLocation(bestLocationProvider);
		} catch (Exception ex2) {
			// ex2.printStackTrace();
			curLoc = hardFix;
			Toast.makeText(ctx,
					ctx.getString(R.string.connection_GPS_dialog_text),
					Toast.LENGTH_LONG).show();
		}

		setLocationAtLastDownload(curLoc);
		return curLoc;
	}

	private void requestBestLocationUpdates() {
		float accuracy = 0;
		String provider = null;  	
		for (String p : lm.getAllProviders()) {
			Location location = lm.getLastKnownLocation(p);
			if (location != null) {
				if (lm.getProvider(p).getAccuracy() > accuracy) {
					accuracy = location.getAccuracy();
					provider = p;
				}
			}
		}
		if(provider != null && !provider.equals(bestLocationProvider)){
			Log.i(MixContext.TAG, "Location provider has changed, old provider: "+ bestLocationProvider + " changed to new provider: "+ provider);
		 	lm.removeUpdates(lnormal);
		 	lm.requestLocationUpdates(provider, freq, dist, lnormal);
		 	bestLocationProvider = provider;
		}
	}

	public void unregisterLocationManager() {
		if (lm != null) {
			lm.removeUpdates(lnormal);
			lm = null;
		}
	}

	public Location getCurrentLocation() {
		if(curLoc == null){
			Toast.makeText(mixView, mixView.getResources().getString(R.string.location_not_found), Toast.LENGTH_LONG).show();
			throw new RuntimeException("No GPS Found");
		}
		synchronized (curLoc) {
			return curLoc;
		}
	}

	private LocationListener lnormal = new LocationListener() {
		public void onProviderDisabled(String provider) {}

		public void onProviderEnabled(String provider) {}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onLocationChanged(Location location) {
			Log.d(MixContext.TAG, "normal Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
			//Toast.makeText(ctx, "NORMAL: Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy(), Toast.LENGTH_LONG).show();
			try {
				MixMap.addWalkingPathPosition(new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6)));
				downloadManager.purgeLists();
				Log.v(MixContext.TAG,"Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
					synchronized (curLoc) {
						curLoc = location;
					}
					mixView.repaint();
					Location lastLoc=getLocationAtLastDownload();
					if(lastLoc==null)
						setLocationAtLastDownload(location);
				requestBestLocationUpdates();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	};

	public Location getLocationAtLastDownload() {
		return locationAtLastDownload;
	}

	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		this.locationAtLastDownload = locationAtLastDownload;
	}

	public void setDownloadManager(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}


}