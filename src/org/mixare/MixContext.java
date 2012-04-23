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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import org.mixare.data.DataSourceStorage;
import org.mixare.lib.MixContextInterface;
import org.mixare.lib.render.Matrix;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Cares about location management and about
 * the data (source, inputstream)
 */
public class MixContext extends ContextWrapper implements MixContextInterface{

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
		
		DataSourceStorage.getInstance(this).fillDefaultDataSources();

		int size = DataSourceStorage.getInstance().getSize();
		
		// copy the value from shared preference to adapter
		for (int i = 0; i < size; i++) {
			String fields[] = DataSourceStorage.getInstance().getFields(i);
			this.allDataSources.add(new DataSource(fields[0], fields[1], fields[2], fields[3], fields[4]));
		}
	}
	public MixContext(Context appCtx) {

		super(appCtx);
		this.mixView = (MixView) appCtx;
		this.ctx = appCtx.getApplicationContext();

		refreshDataSources();

		boolean atLeastOneDatasourceSelected = false;

		for (DataSource ds : this.allDataSources) {
			if (ds.getEnabled())
				atLeastOneDatasourceSelected = true;
		}

		if (!atLeastOneDatasourceSelected) {
			rotationM.toIdentity();
		}

		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// frequency and minimum distance for update
		// this values will only be used after there's a good GPS fix
		// see back-off pattern discussion
		// http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
		// thanks Reto Meier for his presentation at gddde 2010
		long freq = 5000; // 5 seconds
		float dist = 20; // 20 meters
		requestAllLocationUpdates(freq, dist);

		// fallback for the case where GPS and network providers are disabled
		Location hardFix = new Location("reverseGeocoded");

		// Frangart, Eppan, Bozen, Italy
		hardFix.setLatitude(46.480302);
		hardFix.setLongitude(11.296005);
		hardFix.setAltitude(300);

		try {
			curLoc = getBestLocation();
		} catch (Exception ex2) {
			// ex2.printStackTrace();
			curLoc = hardFix;
			Toast.makeText(this,
					getString(DataView.CONNECTION_GPS_DIALOG_TEXT),
					Toast.LENGTH_LONG).show();
		}

		setLocationAtLastDownload(curLoc);
	}

	private Location getBestLocation() {
		float accuracy = 0;
		Location result = null;
		for (String provider : lm.getAllProviders()) {
			Location location = lm.getLastKnownLocation(provider);
			if (location != null) {
				if (location.getAccuracy() > accuracy) {
					accuracy = location.getAccuracy();
					result = location;
				}
			}
		}
		return result;
	}

	private void requestAllLocationUpdates(long freq, float dist) {
		for (String provider : lm.getAllProviders()) {
			try{
				lm.requestLocationUpdates(provider, freq, dist, lnormal);
			}catch(SecurityException se){
				throw new RuntimeException(se);
			}
		}
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
		if(curLoc == null){
			Toast.makeText(this, getResources().getString(R.string.location_not_found), Toast.LENGTH_LONG).show();
			throw new RuntimeException("No GPS Found");
		}
		synchronized (curLoc) {
			return curLoc;
		}
	}

	public InputStream getHttpGETInputStream(String urlStr)
	throws Exception {
		InputStream is = null;
		URLConnection conn = null;

	    // HTTP connection reuse which was buggy pre-froyo
	    if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
	        System.setProperty("http.keepAlive", "false");
	    }

		if (urlStr.startsWith("file://"))			
			return new FileInputStream(urlStr.replace("file://", ""));

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, null);

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
		WebView webview = new WebView(mixView);
		webview.getSettings().setJavaScriptEnabled(true);

		final Dialog d = new Dialog(mixView) {
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};

		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if(url.contains("return")){
					d.dismiss();
					mixView.repaint();
				}else if(processUrl(url, view)){
					return;
				}
				else{
					super.onPageFinished(view, url);
				}				
			}

		});
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setGravity(Gravity.BOTTOM);
		d.addContentView(webview, new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));

		d.show();
		webview.loadUrl(url);

		webview.loadUrl(url);
	}
	public void loadWebPage(String url, Context context) throws Exception {
		WebView webview = new WebView(context);
		webview.setBackgroundColor(0x99FFFFFF);

		final Dialog d = new Dialog(context) {
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};

		webview.setWebViewClient(new WebViewClient() {
			public boolean  shouldOverrideUrlLoading  (WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if(url.contains("return")){
					d.dismiss();
					mixView.repaint();
				}else if(processUrl(url, view)){
					return;
				}
				else{
					super.onPageFinished(view, url);
				}				
			}

		});

		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setGravity(Gravity.BOTTOM);
		d.addContentView(webview, new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));

		d.show();

		webview.loadUrl(url);
	}

	public boolean processUrl(String url, WebView view){
		//process audio
		if (url.endsWith(".mp3")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), "audio/*");
            view.getContext().startActivity(intent);   
            return true;
        //process video
        } else if (url.endsWith(".mp4") || url.endsWith(".3gp")) {
                Intent intent = new Intent(Intent.ACTION_VIEW); 
                intent.setDataAndType(Uri.parse(url), "video/*");
                view.getContext().startActivity(intent);   
                return true;
        }
		//dont do anything with the url
		return false;
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

			downloadManager.purgeLists();

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
				downloadManager.purgeLists();
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

				downloadManager.purgeLists();
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
