package org.mixare.mgr.location;

import org.mixare.MixContext;
import org.mixare.MixMap;
import org.mixare.mgr.downloader.DownloadManager;

import com.google.android.maps.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

class LocationObserver implements LocationListener {
	
	private DownloadManager downloadManager;
	private LocationMgrImpl myController;

	public LocationObserver(LocationMgrImpl myController) {
		super();
		this.myController=myController;
	}
	
	

	public DownloadManager getDownloadManager() {
		return downloadManager;
	}



	public void setDownloadManager(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
	}



	public void onLocationChanged(Location location) {
		Log.d(MixContext.TAG, "Normal Location Changed: " + location.getProvider()
						+ " lat: " + location.getLatitude() + " lon: "
						+ location.getLongitude() + " alt: "
						+ location.getAltitude() + " acc: "
						+ location.getAccuracy());
		try {
			addWalkingPathPosition(location);
			deleteAllDownloadActivity();
			Log.v(MixContext.TAG, "Location Changed: " + location.getProvider()
							+ " lat: " + location.getLatitude() + " lon: "
							+ location.getLongitude() + " alt: "
							+ location.getAltitude() + " acc: "
							+ location.getAccuracy());
			myController.setPosition(location);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void deleteAllDownloadActivity() {
		if (downloadManager != null) {
			downloadManager.purgeLists();
		}
	}

	private void addWalkingPathPosition(Location location) {
		MixMap.addWalkingPathPosition(new GeoPoint((int) (location.getLatitude() * 1E6),(int) (location.getLongitude() * 1E6)));
	}
	
	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	
}
