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
package org.mixare.mgr.location;


import org.mixare.DataView;
import org.mixare.MixContext;
import org.mixare.MixView;
import org.mixare.R;
import org.mixare.mgr.downloader.DownloadManager;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

/**
 * This class is repsonsible for finding the location, and sending it back to
 * the mixcontext. It will also
 * 
 * @author A. Egal
 */
class LocationMgrImpl implements LocationFinder {

	private LocationManager lm;
	private String bestLocationProvider;
	private final MixContext mixContext;
	private Location curLoc;
	private Location locationAtLastDownload;
	private LocationFinderState state;
	private final LocationObserver lob;

	// frequency and minimum distance for update
	// this values will only be used after there's a good GPS fix
	// see back-off pattern discussion
	// http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
	// thanks Reto Meier for his presentation at gddde 2010
	private final long freq = 5000; // 5 seconds
	private final float dist = 20; // 20 meters

	public LocationMgrImpl(MixContext mixContext) {
		this.mixContext = mixContext;
		this.lob=new LocationObserver(this);
		this.state=LocationFinderState.Inactive;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.location.LocationFinder#findLocation(android.content.Context)
	 */
	public Location findLocation(Context ctx) {
		

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
					ctx.getString(DataView.CONNECTION_GPS_DIALOG_TEXT),
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
		if (provider != null && !provider.equals(bestLocationProvider)) {
			Log.i(MixContext.TAG,
					"Location provider has changed, old provider: "
							+ bestLocationProvider
							+ " changed to new provider: " + provider);
			
			lm.removeUpdates(getObserver());
			state=LocationFinderState.Confused;
			lm.requestLocationUpdates(provider, freq, dist, getObserver());
			state=LocationFinderState.Active;
			bestLocationProvider = provider;
		}
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.location.LocationFinder#unregisterLocationManager()
	 */
	public void unregisterLocationManager() {
		
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.location.LocationFinder#getCurrentLocation()
	 */
	public Location getCurrentLocation() {
		if (curLoc == null) {
			MixView mixView = mixContext.getActualMixView();
			Toast.makeText(
					mixView,
					mixView.getResources().getString(
							R.string.location_not_found), Toast.LENGTH_LONG)
					.show();
			throw new RuntimeException("No GPS Found");
		}
		synchronized (curLoc) {
			return curLoc;
		}
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.location.LocationFinder#getLocationAtLastDownload()
	 */
	public Location getLocationAtLastDownload() {
		return locationAtLastDownload;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.location.LocationFinder#setLocationAtLastDownload(android.location.Location)
	 */
	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		this.locationAtLastDownload = locationAtLastDownload;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.location.LocationFinder#setDownloadManager(org.mixare.mgr.downloader.DownloadManager)
	 */
	public void setDownloadManager(DownloadManager downloadManager) {
        getObserver().setDownloadManager(downloadManager);
	}



	/* (non-Javadoc)
	 * @see org.mixare.mgr.location.LocationFinder#getGeomagneticField()
	 */
	public GeomagneticField getGeomagneticField() {
		Location location = getCurrentLocation();
		GeomagneticField gmf = new GeomagneticField(
				(float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(), System.currentTimeMillis());
		return gmf;
	}

	
	public void setPosition(Location location) {
		synchronized (curLoc) {
			curLoc = location;
		}
		mixContext.getActualMixView().repaint();
		Location lastLoc = getLocationAtLastDownload();
		if (lastLoc == null){
			setLocationAtLastDownload(location);
		}
		requestBestLocationUpdates();
	}

	@Override
	public void switchOn() {
		if (!LocationFinderState.Active.equals(state) ){
		   lm = (LocationManager) mixContext.getSystemService(Context.LOCATION_SERVICE);
		   state=LocationFinderState.Confused;
		}
	}

	@Override
	public void switchOff() {
		if (lm != null) {
			lm.removeUpdates(getObserver());
			lm = null; //TODO WHY?
			state=LocationFinderState.Inactive;
		}
	}

	@Override
	public LocationFinderState getStatus() {
		return state;
	}
	
	private synchronized LocationObserver getObserver() {
		return lob;
	}

}