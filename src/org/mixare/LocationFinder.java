package org.mixare;

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
	private DownloadManager downloadManager;
	private MixView mixView;
	private Location curLoc;
	private Location locationAtLastDownload;
	
	public LocationFinder(DownloadManager downloadManager, MixView mixView){
		this.downloadManager = downloadManager;
		this.mixView = mixView;
	}
	
	public Location findLocation(Context ctx) {
		lm = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

		// frequency and minimum distance for update
		// this values will only be used after there's a good GPS fix
		// see back-off pattern discussion
		// http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
		// thanks Reto Meier for his presentation at gddde 2010
		long freq = 5000; // 5 seconds
		float dist = 20; // 20 meters

		// fallback for the case where GPS and network providers are disabled
		Location hardFix = new Location("reverseGeocoded");

		// Frangart, Eppan, Bozen, Italy
		hardFix.setLatitude(46.480302);
		hardFix.setLongitude(11.296005);
		hardFix.setAltitude(300);

		try {
			curLoc = getBestLocation();
			lm.requestLocationUpdates(curLoc.getProvider(), freq, dist, lnormal);
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

	private Location getBestLocation() {
		float accuracy = 0;
		Location result = null;
		for (String provider : lm.getAllProviders()) {
			Location location = lm.getLastKnownLocation(provider);
			if (location != null) {
				if (lm.getProvider(provider).getAccuracy() > accuracy) {
					accuracy = location.getAccuracy();
					result = location;
				}
			}
		}
		return result;
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

				downloadManager.purgeLists();
				Log.v(MixContext.TAG,"Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
					synchronized (curLoc) {
						curLoc = location;
					}
					mixView.repaint();
					Location lastLoc=getLocationAtLastDownload();
					if(lastLoc==null)
						setLocationAtLastDownload(location);
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
