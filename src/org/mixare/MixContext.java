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
import java.util.List;

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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/**
 * Cares about location management and about the data (source, inputstream)
 */
public class MixContext extends ContextWrapper implements MixContextInterface {

	// TAG for logging
	public static final String TAG = "Mixare";

	public MixView mixView;

	public Context ctx;
	
	private DownloadManager downloadManager;

	public Matrix rotationM = new Matrix();

	private ArrayList<DataSource> allDataSources = new ArrayList<DataSource>();

	/** Responsible for all location tasks */
	private LocationFinder locationFinder;
	
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
			this.allDataSources.add(new DataSource(fields[0], fields[1],
					fields[2], fields[3], fields[4]));
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
		
		locationFinder = new LocationFinder(downloadManager, mixView);
		locationFinder.findLocation(this);
	}

	/**
	 * @deprecated please use getDownloadManager
	 */
	public DownloadManager getDownloader() {
		return downloadManager;
	}

	public String getStartUrl() {
		Intent intent = ((Activity) mixView).getIntent();
		if (intent.getAction() != null
				&& intent.getAction().equals(Intent.ACTION_VIEW)) {
			return intent.getData().toString();
		} else {
			return "";
		}
	}

	public void getRM(Matrix dest) {
		synchronized (rotationM) {
			dest.set(rotationM);
		}
	}

	public InputStream getHttpGETInputStream(String urlStr) throws Exception {
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
			HttpsURLConnection
					.setDefaultHostnameVerifier(new HostnameVerifier() {
						public boolean verify(String hostname,
								SSLSession session) {
							return true;
						}
					});
			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new X509TrustManager[] { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			} }, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(context
					.getSocketFactory());
		}

		try {
			URL url = new URL(urlStr);
			conn = url.openConnection();
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
				if (conn instanceof HttpURLConnection)
					((HttpURLConnection) conn).disconnect();
			} catch (Exception ignore) {}
			throw ex;
		}
	}

	public String getHttpInputString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is),
				8 * 1024);
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

	public InputStream getHttpPOSTInputStream(String urlStr, String params)
			throws Exception {
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

			return new ByteArrayInputStream(result.getBytes());
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
	/**
	 * Returns the current location.
	 */
	public Location getCurrentLocation() {
		return locationFinder.getCurrentLocation();
	}

	/**
	 * Sets the property to the location with the last successfull download.
	 */
	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		locationFinder.setLocationAtLastDownload(locationAtLastDownload);
	}

	public void unregisterLocationManager(){
		locationFinder.unregisterLocationManager();
	}
	
	/**
	 * Shows a webpage with the given url when clicked on a marker.
	 */
	public void loadMixViewWebPage(String url) throws Exception {
		loadWebPage(url, mixView);
	}
	
	public DownloadManager getDownloadManager() {
		return downloadManager;
	}

	public void setDownloadManager(DownloadManager downloadManager) {
		this.downloadManager = downloadManager;
		locationFinder.setDownloadManager(downloadManager);
	}
	
	/**
	 * Shows a webpage with the given url if a markerobject is selected
	 * (mixlistview, mixoverlay).
	 */
	public void loadWebPage(String url, Context context) throws Exception {
		WebView webview = new WebView(context);
		webview.getSettings().setJavaScriptEnabled(true);
		if (webview.willNotCacheDrawing()){
			webview.buildDrawingCache(true);
			//@TODO CALL destroyDrawingCache() when destroying (manual Clean up)
		}
		final Dialog d = new Dialog(context) {
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};

		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (url.endsWith("return")) {
					d.dismiss();
					mixView.repaint();
				} else {
					super.onPageFinished(view, url);
				}
			}

		});

		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setGravity(Gravity.BOTTOM);
		d.addContentView(webview, new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));

		if(!processUrl(url, mixView)){ //if the url could not be processed by another intent
			d.setCancelable(true);
			d.show();
			webview.loadUrl(url);
		}
	}

	/**
	 * Checks if the url can be opened by another intent activity, instead of the webview
	 * This method searches for possible intents that can be used instead. I.E. a mp3 file 
	 * can be forwarded to a mediaplayer.
	 * @param url the url to process
	 * @param view
	 * @return
	 */
	public boolean processUrl(String url, Context ctx) {
		// get available packages from the given url
		List<ResolveInfo> resolveInfos = getAvailablePackagesForUrl(url, ctx);
		// filter the webbrowser > because the webview will replace it, using google as simple url
		List<ResolveInfo> webBrowsers = getAvailablePackagesForUrl("http://www.google.com", ctx);
		for (ResolveInfo resolveInfo : resolveInfos) {
			for(ResolveInfo webBrowser: webBrowsers){ //check if the found intent is not a webbrowser
				if(!resolveInfo.activityInfo.packageName.equals(webBrowser.activityInfo.packageName)){
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					intent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
					ctx.startActivity(intent);
					return true;
				}
			}			
		}
		return false;
	}

	private List<ResolveInfo> getAvailablePackagesForUrl(String url,
			 Context ctx) {
		PackageManager packageManager = ctx.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		return packageManager.queryIntentActivities(intent,
				PackageManager.GET_RESOLVED_FILTER);
	}

}
