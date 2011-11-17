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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.mixare.data.DataSource;
import org.mixare.data.DataSourceList;
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
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Cares about location management and about
 * the data (source, inputstream)
 */
public class MixContext extends ContextWrapper {

	//TAG for logging
	public static final String TAG = "Mixare";

	public MixView mixView;
	Context ctx;
	boolean isURLvalid = true;
	Random rand;

	DownloadManager downloadManager;

	Matrix rotationM = new Matrix();
	float declination = 0f;

	//Location related
	private LocationManager lm;
	Location curLoc;
	Location locationAtLastDownload;

	private ArrayList<DataSource> allDataSources=new ArrayList<DataSource>();


	public ArrayList<DataSource> getAllDataSources() {
		return this.allDataSources;
	}

	public void setAllDataSourcesforLauncher(DataSource datasource) {
		this.allDataSources.clear();
		this.allDataSources.add(datasource);
	}

	public void refreshDataSources() {
		this.allDataSources.clear();
		SharedPreferences settings = getSharedPreferences(
				DataSourceList.SHARED_PREFS, 0);
		int size = settings.getAll().size();
		if (size == 0){
			SharedPreferences.Editor dataSourceEditor = settings.edit();
			dataSourceEditor.putString("DataSource0", "Wikipedia|http://ws.geonames.org/findNearbyWikipediaJSON|0|0|true");
			dataSourceEditor.putString("DataSource1", "Twitter|http://search.twitter.com/search.json|2|0|true");
			dataSourceEditor.putString("DataSource2", "OpenStreetmap|http://open.mapquestapi.com/xapi/api/0.6/node[railway=station]|3|1|true");
			dataSourceEditor.putString("DataSource3", "Own URL|http://mixare.org/geotest.php|4|0|false");
			dataSourceEditor.commit();
			size = settings.getAll().size();
		}

		// copy the value from shared preference to adapter
		for (int i = 0; i < size; i++) {
			String fields[] = settings.getString("DataSource" + i, "").split("\\|", -1);
			this.allDataSources.add(new DataSource(fields[0], fields[1], fields[2], fields[3], fields[4]));
		}
	}
	public MixContext(Context appCtx) {

		super(appCtx);
		this.mixView = (MixView) appCtx;
		this.ctx = appCtx.getApplicationContext();

		refreshDataSources();

		boolean atLeastOneDatasourceSelected=false;

		for(DataSource ds: this.allDataSources) {
			if(ds.getEnabled())
				atLeastOneDatasourceSelected=true;
		}
		// select Wikipedia if nothing was previously selected  
		if(!atLeastOneDatasourceSelected) {
			//TODO>: start intent data source select
		}

		// HTTP connection reuse which was buggy pre-froyo
		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}

		rotationM.toIdentity();

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria c = new Criteria();
		//try to use the coarse provider first to get a rough position
		c.setAccuracy(Criteria.ACCURACY_COARSE);
		String coarseProvider = lm.getBestProvider(c, true);
		try {
			lm.requestLocationUpdates(coarseProvider, 0 , 0, lcoarse);
		} catch (Exception e) {
			Log.d(TAG, "Could not initialize the coarse provider");
		}

		//need to be precise
		c.setAccuracy(Criteria.ACCURACY_FINE);				
		//fineProvider will be used for the initial phase (requesting fast updates)
		//as well as during normal program usage
		//NB: using "true" as second parameters means we get the provider only if it's enabled
		String fineProvider = lm.getBestProvider(c, true);
		try {
			lm.requestLocationUpdates(fineProvider, 0 , 0, lbounce);
		} catch (Exception e) {
			Log.d(TAG, "Could not initialize the bounce provider");
		}

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

		//frequency and minimum distance for update
		//this values will only be used after there's a good GPS fix
		//see back-off pattern discussion 
		//http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
		//thanks Reto Meier for his presentation at gddde 2010
		long lFreq = 60000;	//60 seconds
		float lDist = 50;		//20 meters
		try {
			lm.requestLocationUpdates(fineProvider, lFreq , lDist, lnormal);
		} catch (Exception e) {
			Log.d(TAG, "Could not initialize the normal provider");
			Toast.makeText( this, getString(DataView.CONNECTION_GPS_DIALOG_TEXT), Toast.LENGTH_LONG ).show();
		}

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
			//ex2.printStackTrace();
			curLoc = hardFix;
			Toast.makeText( this, getString(DataView.CONNECTION_GPS_DIALOG_TEXT), Toast.LENGTH_LONG ).show();
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

	public DownloadManager getDownloader() {
		return downloadManager;
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

	public String openURL(String urlStr, String params)
    throws Exception {

		InputStream is = null;
		URLConnection conn = null;

		if (urlStr.startsWith("file://")){
			is = new FileInputStream(urlStr.replace("file://", ""));
			return parseInputString(is, null);
		}
		if (urlStr.startsWith("content://")) {		
			is = getContentInputStream(urlStr, null);
			return parseInputString(is, null);
		}

		if (urlStr.startsWith("https://")) {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[]{new X509TrustManager(){
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {}
				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {}
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}}}, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(
					context.getSocketFactory());
		}

		try {
			URL url = new URL(urlStr+params);
			conn =  url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);
			String contentType = conn.getHeaderField("Content-Type");
			String charset = "UTF-8";

      if(contentType != null) {
        for (String param : contentType.replace(" ", "").split(";")) {
          if (param.startsWith("charset=")) {
            charset = param.split("=", 2)[1];
            break;
          }
        }
      }

			is = conn.getInputStream();
			return parseInputString(is, charset);

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

	private String parseInputString(InputStream is, String charset) throws UnsupportedEncodingException {	
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset), 8 * 1024);
		StringBuilder sb = new StringBuilder();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
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

	public void closeHttpInputStream(InputStream is) throws Exception {
		if (is != null) {
			is.close();
		}
	}

	public InputStream getResourceInputStream(String name) throws Exception {
		AssetManager mgr = mixView.getAssets();
		return mgr.open(name);
	}

	public void closeResourceInputStream(InputStream is) throws Exception {
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

	public Location getLocationAtLastDownload() {
		return locationAtLastDownload;
	}

	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		this.locationAtLastDownload = locationAtLastDownload;
	}

	private LocationListener lbounce = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			Log.d(TAG, "bounce Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
			//Toast.makeText(ctx, "BOUNCE: Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy(), Toast.LENGTH_LONG).show();
			if (location.getAccuracy() < 40) {
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
			try {
				Log.d(TAG, "coarse Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
				//Toast.makeText(ctx, "COARSE: Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy(), Toast.LENGTH_LONG).show();
				lm.removeUpdates(lcoarse);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void onProviderDisabled(String arg0) {}

		@Override
		public void onProviderEnabled(String arg0) {}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {}

	};

	private LocationListener lnormal = new LocationListener() {
		public void onProviderDisabled(String provider) {}

		public void onProviderEnabled(String provider) {}

		public void onStatusChanged(String provider, int status, Bundle extras) {}

		public void onLocationChanged(Location location) {
			Log.d(TAG, "normal Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
			//Toast.makeText(ctx, "NORMAL: Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy(), Toast.LENGTH_LONG).show();
			try {

				Log.v(TAG,"Location Changed: "+location.getProvider()+" lat: "+location.getLatitude()+" lon: "+location.getLongitude()+" alt: "+location.getAltitude()+" acc: "+location.getAccuracy());
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
}
