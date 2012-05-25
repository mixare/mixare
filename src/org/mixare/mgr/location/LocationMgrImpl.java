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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.mixare.MixContext;
import org.mixare.MixView;
import org.mixare.R;
import org.mixare.mgr.downloader.DownloadManager;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

/**
 * This class is repsonsible for finding the location, and sending it back to
 * the mixcontext.
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
	private List<LocationResolver> locationResolvers;

	// frequency and minimum distance for update
	// this values will only be used after there's a good GPS fix
	// see back-off pattern discussion
	// http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
	// thanks Reto Meier for his presentation at gddde 2010
	private final long freq = 5000; // 5 seconds
	private final float dist = 20; // 20 meters

	public LocationMgrImpl(MixContext mixContext) {
		this.mixContext = mixContext;
		this.lob = new LocationObserver(this);
		this.state = LocationFinderState.Inactive;
		this.locationResolvers = new ArrayList<LocationResolver>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#findLocation(android.content.Context
	 * )
	 */
	public void findLocation() {

		// fallback for the case where GPS and network providers are disabled
		Location hardFix = new Location("reverseGeocoded");

		// Frangart, Eppan, Bozen, Italy
		hardFix.setLatitude(46.480302);
		hardFix.setLongitude(11.296005);
		hardFix.setAltitude(300);

		try {
			requestBestLocationUpdates();
			//temporary set the current location, until a good provider is found
			curLoc = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));
		} catch (Exception ex2) {
			// ex2.printStackTrace();
			curLoc = hardFix;
			mixContext.doPopUp(R.string.connection_GPS_dialog_text);

		}
	}

	private void requestBestLocationUpdates() {
		Timer timer = new Timer();
		for (String p : lm.getAllProviders()) {
			if(lm.isProviderEnabled(p)){
				LocationResolver lr = new LocationResolver(lm, p, this);
				locationResolvers.add(lr);
				lm.requestLocationUpdates(p, 0, 0, lr);
			}
		}
		timer.schedule(new LocationTimerTask(),20* 1000); //wait 20 seconds for the location updates to find the location
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#locationCallback(android.content
	 * .Context)
	 */
	public void locationCallback(String provider) {
		Location foundLocation = lm.getLastKnownLocation(provider);
		if (bestLocationProvider != null) {
			Location bestLocation = lm
					.getLastKnownLocation(bestLocationProvider);
			if (foundLocation.getAccuracy() < bestLocation.getAccuracy()) {
				curLoc = foundLocation;
				bestLocationProvider = provider;
			}
		} else {
			curLoc = foundLocation;
			bestLocationProvider = provider;
		}
		setLocationAtLastDownload(curLoc);		
	}

	/*
	 * (non-Javadoc)
	 * 
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.location.LocationFinder#getLocationAtLastDownload()
	 */
	public Location getLocationAtLastDownload() {
		return locationAtLastDownload;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#setLocationAtLastDownload(android
	 * .location.Location)
	 */
	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		this.locationAtLastDownload = locationAtLastDownload;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#setDownloadManager(org.mixare.
	 * mgr.downloader.DownloadManager)
	 */
	public void setDownloadManager(DownloadManager downloadManager) {
		getObserver().setDownloadManager(downloadManager);
	}

	/*
	 * (non-Javadoc)
	 * 
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
		mixContext.getActualMixView().refresh();
		Location lastLoc = getLocationAtLastDownload();
		if (lastLoc == null) {
			setLocationAtLastDownload(location);
		}
	}

	@Override
	public void switchOn() {
		if (!LocationFinderState.Active.equals(state)) {
			lm = (LocationManager) mixContext
					.getSystemService(Context.LOCATION_SERVICE);
			state = LocationFinderState.Confused;
		}
	}

	@Override
	public void switchOff() {
		if (lm != null) {
			lm.removeUpdates(getObserver());
			state = LocationFinderState.Inactive;
		}
	}

	@Override
	public LocationFinderState getStatus() {
		return state;
	}

	private synchronized LocationObserver getObserver() {
		return lob;
	}

	class LocationTimerTask extends TimerTask {

		@Override
		public void run() {
			//remove all location updates
			for(LocationResolver locationResolver: locationResolvers){
				lm.removeUpdates(locationResolver);
			}
			if(bestLocationProvider != null){
				lm.removeUpdates(getObserver());
				state=LocationFinderState.Confused;
				mixContext.getActualMixView().runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						lm.requestLocationUpdates(bestLocationProvider, freq, dist, getObserver());		
					}
				});
				state=LocationFinderState.Active;
			}
			else{ //no location found
				mixContext.getActualMixView().runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						Toast.makeText(mixContext.getActualMixView(), 
								mixContext.getActualMixView().getResources().getString(
								R.string.location_not_found), Toast.LENGTH_LONG);
					}
				});
				
			}
			
			
		}

	}

}