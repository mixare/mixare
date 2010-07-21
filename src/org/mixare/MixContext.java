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
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.mixare.render.Matrix;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;
import android.util.TimeUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.FrameLayout;

public class MixContext {
	public MixView mixView;
	Context ctx;

	Random rand;

	DownloadManager downloadManager;

	Location curLoc;
	Matrix rotationM = new Matrix();

	float declination = 0f;
	private boolean actualLocation=false;

	public MixContext(Context appCtx) {
		this.mixView = (MixView) appCtx;
		this.ctx = appCtx.getApplicationContext();

		rotationM.toIdentity();

		int locationHash = 0;
		try {
			LocationManager locationMgr = (LocationManager) appCtx.getSystemService(Context.LOCATION_SERVICE);
			Location lastFix = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			
			Date dt = new Date();
			long actualTime= dt.getTime();
			long lastFixTime = lastFix.getTime();
			long timeDifference = actualTime-lastFixTime;


			if(timeDifference> 1200000){//300000 milliseconds = 5 min
				actualLocation=false;
			}
			actualLocation=true;
			if (lastFix == null){
				lastFix = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				actualLocation=false;
			}

			if (lastFix != null){
				locationHash = ("HASH_" + lastFix.getLatitude() + "_" + lastFix.getLongitude()).hashCode();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		rand = new Random(System.currentTimeMillis() + locationHash);
	}

	public boolean isGpsEnabled() {
		return mixView.isGpsEnabled;
	}
	public boolean isActualLocation(){
		return actualLocation;
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

	public InputStream getHttpGETInputStream(String urlStr)
	throws Exception {
		InputStream is = null;
		HttpURLConnection conn = null;

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, null);

		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
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
				conn.disconnect();
			} catch (Exception ignore) {			

			}
			
			throw ex;				

		}
	}

	public String getHttpInputString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is),
				8 * 1024);
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
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
		htmlEntities.put("&agrave;", "ÃƒÂ ");
		htmlEntities.put("&Agrave;", "Ãƒâ‚¬");
		htmlEntities.put("&acirc;", "ÃƒÂ¢");
		htmlEntities.put("&auml;", "ÃƒÂ¤");
		htmlEntities.put("&Auml;", "Ãƒâ€ž");
		htmlEntities.put("&Acirc;", "Ãƒâ€š");
		htmlEntities.put("&aring;", "ÃƒÂ¥");
		htmlEntities.put("&Aring;", "Ãƒâ€¦");
		htmlEntities.put("&aelig;", "ÃƒÂ¦");
		htmlEntities.put("&AElig;", "Ãƒâ€ ");
		htmlEntities.put("&ccedil;", "ÃƒÂ§");
		htmlEntities.put("&Ccedil;", "Ãƒâ€¡");
		htmlEntities.put("&eacute;", "ÃƒÂ©");
		htmlEntities.put("&Eacute;", "Ãƒâ€°");
		htmlEntities.put("&egrave;", "ÃƒÂ¨");
		htmlEntities.put("&Egrave;", "ÃƒË†");
		htmlEntities.put("&ecirc;", "ÃƒÂª");
		htmlEntities.put("&Ecirc;", "ÃƒÅ ");
		htmlEntities.put("&euml;", "ÃƒÂ«");
		htmlEntities.put("&Euml;", "Ãƒâ€¹");
		htmlEntities.put("&iuml;", "ÃƒÂ¯");
		htmlEntities.put("&Iuml;", "Ãƒï¿½");
		htmlEntities.put("&ocirc;", "ÃƒÂ´");
		htmlEntities.put("&Ocirc;", "Ãƒâ€�");
		htmlEntities.put("&ouml;", "ÃƒÂ¶");
		htmlEntities.put("&Ouml;", "Ãƒâ€“");
		htmlEntities.put("&oslash;", "ÃƒÂ¸");
		htmlEntities.put("&Oslash;", "ÃƒËœ");
		htmlEntities.put("&szlig;", "ÃƒÅ¸");
		htmlEntities.put("&ugrave;", "ÃƒÂ¹");
		htmlEntities.put("&Ugrave;", "Ãƒâ„¢");
		htmlEntities.put("&ucirc;", "ÃƒÂ»");
		htmlEntities.put("&Ucirc;", "Ãƒâ€º");
		htmlEntities.put("&uuml;", "ÃƒÂ¼");
		htmlEntities.put("&Uuml;", "ÃƒÅ“");
		htmlEntities.put("&nbsp;", " ");
		htmlEntities.put("&copy;", "\u00a9");
		htmlEntities.put("&reg;", "\u00ae");
		htmlEntities.put("&euro;", "\u20a0");
	}

	public String unescapeHTML(String source, int start) {
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

	public void loadWebPage(String url) throws Exception {
		// TODO
		WebView webview = new WebView(mixView);
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

}
