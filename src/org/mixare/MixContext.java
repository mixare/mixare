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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Random;

import org.mixare.data.DataSource;
import org.mixare.render.Matrix;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

public class MixContext extends ContextWrapper {

	//TAG for logging
	public static final String TAG = "Mixare";
	
	public MixView mixView;
	Context ctx;
	boolean isURLvalid = true;
	Random rand;

	DownloadManager downloadManager;

	Location curLoc;
	private boolean isGpsEnabled;
	Location locationAtLastDownload;
	Matrix rotationM = new Matrix();

	float declination = 0f;
	private boolean actualLocation=false;
	private LocationManager lm;
	
	private HashMap<DataSource.DATASOURCE,Boolean> selectedDataSources=new HashMap<DataSource.DATASOURCE,Boolean>();
	
	public MixContext(Context appCtx) {
	
		super(appCtx);
		this.mixView = (MixView) appCtx;
		this.ctx = appCtx.getApplicationContext();

		SharedPreferences settings = getSharedPreferences(MixView.PREFS_NAME, 0);
		for(DataSource.DATASOURCE source: DataSource.DATASOURCE.values()) {
			// fill the selectedDataSources HashMap with saved settings
			selectedDataSources.put(source, settings.getBoolean(source.toString(), false));
		}

		rotationM.toIdentity();
		
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		Criteria c = new Criteria();
		//try to use the coarse provider first to get a rough position
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		String coarseProvider = lm.getBestProvider(c, true);
		
		//need to be precise
		c.setAccuracy(Criteria.ACCURACY_FINE);				
		//fineProvider will be used for the initial phase (requesting fast updates)
		//as well as during normal program usage
		//NB: using "true" as second parameters means we get the provider only if it's enabled
		String fineProvider = lm.getBestProvider(c, true);

		//frequency and minimum distance for update
		//this values will only be used after there's a good GPS fix
		//see back-off pattern discussion 
		//http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
		//thanks Reto Meier for his presentation at gddde 2010
		long lFreq = 60000;	//60 seconds
		float lDist = 20;		//20 meters

		//fallback for the case where GPS and network providers are disabled
		Location hardFix = new Location("reverseGeocoded");

		//Frangart, Eppan, Bozen, Italy
		hardFix.setLatitude(46.480302);
		hardFix.setLongitude(11.296005);
		hardFix.setAltitude(300);

		/*New York*/
//		hardFix.setLatitude(40.731510);
//		hardFix.setLongitude(-73.991547);
		
		// TU Wien
//		hardFix.setLatitude(48.196349);
//		hardFix.setLongitude(16.368653);
//		hardFix.setAltitude(180);

		lm.requestLocationUpdates(fineProvider, lFreq , lDist, lnormal);
		lm.requestLocationUpdates(coarseProvider, 0 , 0, lcoarse);
		lm.requestLocationUpdates(fineProvider, 0 , 0, lbounce);


		try {
			Location lastFinePos=lm.getLastKnownLocation(fineProvider);
			Location lastCoarsePos=lm.getLastKnownLocation(coarseProvider);
			if(lastFinePos!=null)
				curLoc = lastFinePos;
			else if (lastCoarsePos!=null)
				curLoc = lastCoarsePos;
			else
				curLoc = hardFix;
			
		} catch (Exception ex2) {
			ex2.printStackTrace();
			curLoc = hardFix;
		}
		
		setLocationAtLastDownload(curLoc);

	}
	
	public void unregisterLocationManager() {
		if (lm != null) {
			lm.removeUpdates(lnormal);
			lm.removeUpdates(lcoarse);
			lm.removeUpdates(lbounce);
			lm = null;
		}
	}
	
	public Location getCurrentGPSInfo() {
		return curLoc != null ? curLoc : lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}

	public boolean isGpsEnabled() {
		return isGpsEnabled;
	}

	public boolean isActualLocation(){
		return actualLocation;
	}

	public DownloadManager getDownloader() {
		return downloadManager;
	}
	
	public void setLocationManager(LocationManager locationMgr){
		this.lm = locationMgr;
	}
	
	public LocationManager getLocationManager(){
		return lm;
	}

	public String getStartUrl() {
		Intent intent = ((Activity) mixView).getIntent();
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_VIEW)) { 
			return intent.getData().toString(); 
		} 
		else { 
			return ""; 
		}
	}

	public void getRM(Matrix dest) {
		synchronized (rotationM) {
			dest.set(rotationM);
		}
	}

	public Location getCurrentLocation() {
		synchronized (curLoc) {
			return curLoc;
		}
	}

	public InputStream getHttpGETInputStream(String urlStr)
	throws Exception {
		InputStream is = null;
		URLConnection conn = null;
		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, null);

		try {
			URL url = new URL(urlStr);
			conn =  url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			is = conn.getInputStream();
			
			return is;
		} catch (Exception ex) {
			try {
				is.close();
			} catch (Exception ignore) {			
			}
			try {
				if(conn instanceof HttpURLConnection)
					((HttpURLConnection)conn).disconnect();
			} catch (Exception ignore) {			
			}
			
			throw ex;				

		}
	}

	public String getHttpInputString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8 * 1024);
		StringBuilder sb = new StringBuilder();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private static HashMap<String, String> htmlEntities;
	static {
		htmlEntities = new HashMap<String, String>();
		htmlEntities.put("&lt;", "<");
		htmlEntities.put("&gt;", ">");
		htmlEntities.put("&amp;", "&");
		htmlEntities.put("&quot;", "\"");
		htmlEntities.put("&agrave;", "à");
		htmlEntities.put("&Agrave;", "À");
		htmlEntities.put("&acirc;", "â");
		htmlEntities.put("&auml;", "ä");
		htmlEntities.put("&Auml;", "Ä");
		htmlEntities.put("&Acirc;", "Â");
		htmlEntities.put("&aring;", "å");
		htmlEntities.put("&Aring;", "Å");
		htmlEntities.put("&aelig;", "æ");
		htmlEntities.put("&AElig;", "Æ");
		htmlEntities.put("&ccedil;", "ç");
		htmlEntities.put("&Ccedil;", "Ç");
		htmlEntities.put("&eacute;", "é");
		htmlEntities.put("&Eacute;", "É");
		htmlEntities.put("&egrave;", "è");
		htmlEntities.put("&Egrave;", "È");
		htmlEntities.put("&ecirc;", "ê");
		htmlEntities.put("&Ecirc;", "Ê");
		htmlEntities.put("&euml;", "ë");
		htmlEntities.put("&Euml;", "Ë");
		htmlEntities.put("&iuml;", "ï");
		htmlEntities.put("&Iuml;", "Ï");
		htmlEntities.put("&ocirc;", "ô");
		htmlEntities.put("&Ocirc;", "Ô");
		htmlEntities.put("&ouml;", "ö");
		htmlEntities.put("&Ouml;", "Ö");
		htmlEntities.put("&oslash;", "ø");
		htmlEntities.put("&Oslash;", "Ø");
		htmlEntities.put("&szlig;", "ß");
		htmlEntities.put("&ugrave;", "ù");
		htmlEntities.put("&Ugrave;", "Ù");
		htmlEntities.put("&ucirc;", "û");
		htmlEntities.put("&Ucirc;", "Û");
		htmlEntities.put("&uuml;", "ü");
		htmlEntities.put("&Uuml;", "Ü");
		htmlEntities.put("&nbsp;", " ");
		htmlEntities.put("&copy;", "\u00a9");
		htmlEntities.put("&reg;", "\u00ae");
		htmlEntities.put("&euro;", "\u20a0");
	}

	public static String unescapeHTML(String source, int start) {
		int i, j;

		i = source.indexOf("&", start);
		if (i > -1) {
			j = source.indexOf(";", i);
			if (j > i) {
				String entityToLookFor = source.substring(i, j + 1);
				String value = (String) htmlEntities.get(entityToLookFor);
				if (value != null) {
					source = new StringBuffer().append(source.substring(0, i))
					.append(value).append(source.substring(j + 1))
					.toString();
					return unescapeHTML(source, i + 1); // recursive call
				}
			}
		}
		return source;
	}

	public InputStream getHttpPOSTInputStream(String urlStr,
			String params) throws Exception {
		InputStream is = null;
		OutputStream os = null;
		HttpURLConnection conn = null;

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, params);

		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			if (params != null) {
				conn.setDoOutput(true);
				os = conn.getOutputStream();
				OutputStreamWriter wr = new OutputStreamWriter(os);
				wr.write(params);
				wr.close();
			}

			is = conn.getInputStream();
			
			return is;
		} catch (Exception ex) {

			try {
				is.close();
			} catch (Exception ignore) {			

			}
			try {
				os.close();
			} catch (Exception ignore) {			

			}
			try {
				conn.disconnect();
			} catch (Exception ignore) {
			}

			if (conn != null && conn.getResponseCode() == 405) {
				return getHttpGETInputStream(urlStr);
			} else {		

				throw ex;
			}
		}
	}

	public InputStream getContentInputStream(String urlStr, String params)
	throws Exception {
		ContentResolver cr = mixView.getContentResolver();
		Cursor cur = cr.query(Uri.parse(urlStr), null, params, null, null);

		cur.moveToFirst();
		int mode = cur.getInt(cur.getColumnIndex("MODE"));

		if (mode == 1) {
			String result = cur.getString(cur.getColumnIndex("RESULT"));
			cur.deactivate();

			return new ByteArrayInputStream(result
					.getBytes());
		} else {
			cur.deactivate();

			throw new Exception("Invalid content:// mode " + mode);
		}
	}

	public void returnHttpInputStream(InputStream is) throws Exception {
		if (is != null) {
			is.close();
		}
	}

	public InputStream getResourceInputStream(String name) throws Exception {
		AssetManager mgr = mixView.getAssets();
		return mgr.open(name);
	}

	public void returnResourceInputStream(InputStream is) throws Exception {
		if (is != null)
			is.close();
	}

	public void loadMixViewWebPage(String url) throws Exception {
		// TODO
		WebView webview = new WebView(mixView);
		webview.getSettings().setJavaScriptEnabled(true);

		webview.setWebViewClient(new WebViewClient() {
			public boolean  shouldOverrideUrlLoading  (WebView view, String url) {
			     view.loadUrl(url);
				return true;
			}

		});
				
		Dialog d = new Dialog(mixView) {
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setGravity(Gravity.BOTTOM);
		d.addContentView(webview, new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));

		d.show();
		
		webview.loadUrl(url);
	}
	public void loadWebPage(String url, Context context) throws Exception {
		// TODO
		WebView webview = new WebView(context);
		
		webview.setWebViewClient(new WebViewClient() {
			public boolean  shouldOverrideUrlLoading  (WebView view, String url) {
			     view.loadUrl(url);
				return true;
			}

		});
				
		Dialog d = new Dialog(context) {
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setGravity(Gravity.BOTTOM);
		d.addContentView(webview, new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));

		d.show();
		
		webview.loadUrl(url);
	}



	public void setDataSource(DataSource.DATASOURCE source, Boolean selection){
		selectedDataSources.put(source,selection);
		SharedPreferences settings = getSharedPreferences(MixView.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(source.toString(), selection);
		editor.commit();
	}
               
	public Boolean isDataSourceSelected(DataSource.DATASOURCE source) {
		return selectedDataSources.get(source);
	}
	
	public void toogleDataSource(DataSource.DATASOURCE source) {
		setDataSource(source, !selectedDataSources.get(source));
	}
	
	
	public String getDataSourcesStringList() {
		String ret="";
		boolean first=true;
		for(DataSource.DATASOURCE source: DataSource.DATASOURCE.values()) {
			if(isDataSourceSelected(source)) {
				if(!first) {
					ret+=", ";
				}	
				ret+=source.toString();
				first=false;
			}	
		}
		return ret;
	}

	public Location getLocationAtLastDownload() {
		return locationAtLastDownload;
	}

	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		this.locationAtLastDownload = locationAtLastDownload;
	}
	
	private LocationListener lbounce = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "bounce");
			Log.v(TAG,"Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
			
			if (location.getAccuracy() < 20) {
				lm.removeUpdates(lcoarse);
				lm.removeUpdates(lbounce);			
			}
		}

		@Override
		public void onProviderDisabled(String arg0) {
			Log.d(TAG, "bounce disabled");
		}

		@Override
		public void onProviderEnabled(String arg0) {
			Log.d(TAG, "bounce enabled");

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
		
	};
	
	private LocationListener lcoarse = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "coarse");
			Log.v(TAG,"Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
			
			lm.removeUpdates(lcoarse);
		}

		@Override
		public void onProviderDisabled(String arg0) {
			Log.d(TAG, "coarse disabled");
		}

		@Override
		public void onProviderEnabled(String arg0) {
			Log.d(TAG, "coarse enabled");

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}
		
	};

	private LocationListener lnormal = new LocationListener() {
		public void onProviderDisabled(String provider) {
			isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}

		public void onProviderEnabled(String provider) {
			isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		public void onLocationChanged(Location location) {
			Log.d(TAG, "normal");
			Log.v(TAG,"Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
			
			try {
				Log.v(TAG,"Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
				if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
					synchronized (curLoc) {
						curLoc = location;
					}
					//dataView.getDataHandler().onLocationChanged(location);
					// If we have moved more than radius/3 km away from the 
					// location where the last download occured we should start 
					// a fresh download
					Location lastLoc=getLocationAtLastDownload();
					if(lastLoc==null)
						setLocationAtLastDownload(location);
					else {
						//float threshold = dataView.getRadius()*1000f/3f;
						//Log.v(TAG,"Location Change: "+" threshold "+threshold+" distanceto "+location.distanceTo(lastLoc));
						//if(location.distanceTo(lastLoc)>threshold)  {
						//	Log.d(TAG,"Restarting download due to location change");
						//	mixView.repaint();
						//}	
					}
					isGpsEnabled = true;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	};

	

}
