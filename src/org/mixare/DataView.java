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

import static android.view.KeyEvent.KEYCODE_CAMERA;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.mixare.data.Json;
import org.mixare.gui.PaintScreen;
import org.mixare.gui.RadarPoints;
import org.mixare.gui.ScreenLine;
import org.mixare.render.Camera;
import org.mixare.render.Matrix;
import org.mixare.render.MixVector;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;


/**
 * @author daniele
 *
 */
public class DataView {

	/**current context */
	MixContext ctx;

	/** is the view Inited? */
	boolean isInit = false;
	/** width and height of the view*/
	int width, height;
	/** _NOT_ the android camera, the class that takes care of the transformation*/
	Camera cam;
	/** */
	public MixState state = new MixState();
	/** The view can be "frozen" for debug purposes */
	boolean frozen = false;
	/** how many times to re-attempt download */
	int retry = 0;
	/** default URL */
	String WIKI_HOME_URL = "http://ws.geonames.org/findNearbyWikipediaJSON";
	String TWITTER_HOME_URL = "http://search.twitter.com/search.json";
	String BUZZ_HOME_URL = "https://www.googleapis.com/buzz/v1/activities/search?alt=json&max-results=20";
	
	private Location curFix;
	private String startUrl = "";
	public float screenWidth, screenHeight;
	
	public Json jLayer = new Json();
	
	public float radius = 20;
	DownloadResult dRes;
	
	/**IDs for the MENU ITEMS and MENU OPTIONS used in MixView class*/
	public int EMPTY_LIST_STRING_ID = R.string.empty_list;
	public int OPTION_NOT_AVAILABLE_STRING_ID = R.string.option_not_available;
	public int EMPTY_LIST_STRIG_ID = R.string.empty_list;
	public int MENU_ITEM_1 = R.string.menu_item_1;
	public int MENU_ITEM_2 = R.string.menu_item_2;
	public int MENU_ITEM_3 = R.string.menu_item_3;
	public int MENU_ITEM_4 = R.string.menu_item_4;
	public int MENU_ITEM_5 = R.string.menu_item_5;
	public int MENU_ITEM_6 = R.string.menu_item_6;
	public int MENU_ITEM_7 = R.string.menu_item_7;

	
	public int CONNECITON_ERROR_DIALOG_TEXT = R.string.connection_error_dialog;
	public int CONNECITON_ERROR_DIALOG_BUTTON1 = R.string.connection_error_dialog_button1;
	public int CONNECITON_ERROR_DIALOG_BUTTON2 = R.string.connection_error_dialog_button2;
	public int CONNECITON_ERROR_DIALOG_BUTTON3 = R.string.connection_error_dialog_button3;
	
	public int CONNECITON_GPS_DIALOG_TEXT = R.string.connection_GPS_dialog_text;
	public int CONNECITON_GPS_DIALOG_BUTTON1 = R.string.connection_GPS_dialog_button1;
	public int CONNECITON_GPS_DIALOG_BUTTON2 = R.string.connection_GPS_dialog_button2;
	
	public boolean isLauncherStarted=false;
	
	/*if in the listview option for a specific title no website is provided*/
	public int NO_WEBINFO_AVAILABLE = R.string.no_website_available;
	public int LICENSE_TEXT = R.string.license;
	public int LICENSE_TITLE = R.string.license_title;
	public int CLOSE_BUTTON = R.string.close_button;
	
	/*Strings for general information*/
	public int GENERAL_INFO_TITLE = R.string.general_info_title;
	public int GENERAL_INFO_TEXT = R.string.general_info_text;
	public int GPS_LONGITUDE = R.string.longitude;
	public int GPS_LATITUDE = R.string.latitude;
	public int GPS_ALTITUDE = R.string.altitude;
	public int GPS_SPEED = R.string.speed;
	public int GPS_ACCURACY = R.string.accuracy;
	public int GPS_LAST_FIX = R.string.gps_last_fix;

	public int MAP_MENU_NORMAL_MODE = R.string.map_menu_normal_mode;
	public int MAP_MENU_SATELLITE_MODE = R.string.map_menu_satellite_mode;
	public int MENU_CAM_MODE = R.string.map_menu_cam_mode;
	public int MAP_MY_LOCATION = R.string.map_my_location;
	public int MAP_CURRENT_LOCATION_CLICK = R.string.map_current_location_click;


//	public int ORIENTATON_NORD_ID = R.string.N;
	
	ArrayList<UIEvent> uiEvents = new ArrayList<UIEvent>();

	RadarPoints radarPoints = new RadarPoints();
	Matrix rInv = new Matrix();
	MixVector looking = new MixVector();
	ScreenLine lrl = new ScreenLine();
	ScreenLine rrl = new ScreenLine();
	float rx = 10, ry = 20;
	public float addX = 0, addY = 0;

	public DataView(MixContext ctx) {
		this.ctx = ctx;
	}

	public void doStart() {
		state.nextLStatus = MixState.NOT_STARTED;
	}

	public boolean isInited() {
		return isInit;
	}

	public void init(int widthInit, int heightInit) {
		try {
			width = widthInit;
			height = heightInit;

			cam = new Camera(width, height, true);
			cam.setViewAngle(Camera.DEFAULT_VIEW_ANGLE);

			lrl.set(0, -RadarPoints.RADIUS);
			lrl.rotate(Camera.DEFAULT_VIEW_ANGLE / 2);
			lrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
			rrl.set(0, -RadarPoints.RADIUS);
			rrl.rotate(-Camera.DEFAULT_VIEW_ANGLE / 2);
			rrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		frozen = false;
		isInit = true;
	}

	public void draw(PaintScreen dw) {
		ctx.getRM(cam.transform);
		curFix = ctx.getCurrentLocation();

		state.calcPitchBearing(cam.transform);

		screenWidth = width;
		screenHeight = height;

		// Load Layer
		if (state.nextLStatus == MixState.NOT_STARTED ) {

			DownloadRequest request = new DownloadRequest();

			if (!ctx.getStartUrl().equals("")){
				request.url = ctx.getStartUrl();
				isLauncherStarted=true;
			}
			//http://www.suedtirolerland.it/api/map/getARData/?client[lat]=46.4786481&client[lng]=11.29534&client[rad]=100&lang_id=1&project_id=15&showTypes=52&key=287235f7ca18ef2afb719bc616288353

			else {
				if(MixListView.getDataSource()=="Wikipedia")
					request.url = WIKI_HOME_URL + "?lat="+curFix.getLatitude()+"&lng=" + curFix.getLongitude() + "&radius="+ radius +"&maxRows=50&lang=" + Locale.getDefault().getLanguage();
				else if(MixListView.getDataSource()=="Twitter")
					request.url = TWITTER_HOME_URL +"?geocode="+curFix.getLatitude() + "%2C" + curFix.getLongitude()+"%2C" + radius + "km" ;
				else if(MixListView.getDataSource()=="Buzz")  
					request.url = BUZZ_HOME_URL + "&lat="+curFix.getLatitude()+"&lon=" + curFix.getLongitude() + "&radius="+ radius*1000;
					//https://www.googleapis.com/buzz/v1/activities/search?alt=json&lat=46.47122383117541&lon=11.260278224944742&radius=20000
			}
			Log.i(MixView.TAG,""+request.url);
			startUrl = ctx.getStartUrl();
			state.downloadId = ctx.getDownloader().submitJob(request);

			state.nextLStatus = MixState.PROCESSING;

		} else if (state.nextLStatus == MixState.PROCESSING) {
			if (ctx.getDownloader().isReqComplete(state.downloadId)) {
				dRes = ctx.getDownloader().getReqResult(state.downloadId);

				if (dRes.error && retry < 3) {
					retry++;
					state.nextLStatus = MixState.NOT_STARTED;

				} else {
					retry = 0;
					state.nextLStatus = MixState.DONE;
					jLayer = (Json) dRes.obj;

					//Sort markers by cMarker.z
					Collections.sort(jLayer.markers, new MarkersOrder());
				}	
			}
		} 

		// Update markers
		for (int i = 0; i < jLayer.markers.size(); i++) {
			Marker ma = jLayer.markers.get(i);
			float[] dist = new float[1];
			dist[0] = 0;
			Location.distanceBetween(ma.mGeoLoc.getLatitude(), ma.mGeoLoc.getLongitude(), ctx.getCurrentLocation().getLatitude(), ctx.getCurrentLocation().getLongitude(), dist);
			if (dist[0] / 1000f < radius) {
				if (!frozen) ma.update(curFix, System.currentTimeMillis());
				ma.calcPaint(cam, addX, addY);
				ma.draw(dw);
			}
		}
		// Draw Radar
		String	dirTxt = ""; 
		int bearing = (int) state.getCurBearing(); 
		int range = (int) (state.getCurBearing() / (360f / 16f)); 
		//TODO: get strings from the values xml file
		if (range == 15 || range == 0) dirTxt = "N"; 
		else if (range == 1 || range == 2) dirTxt = "NE"; 
		else if (range == 3 || range == 4) dirTxt = "E"; 
		else if (range == 5 || range == 6) dirTxt = "SE";
		else if (range == 7 || range == 8) dirTxt= "S"; 
		else if (range == 9 || range == 10) dirTxt = "SW"; 
		else if (range == 11 || range == 12) dirTxt = "W"; 
		else if (range == 13 || range == 14) dirTxt = "NW";

		radarPoints.view = this; 
		dw.paintObj(radarPoints, rx, ry, -state.getCurBearing(), 1); 
		dw.setFill(false);
		dw.setColor(Color.argb(150,0,0,220)); 
		dw.paintLine( lrl.x, lrl.y, rx+RadarPoints.RADIUS, ry+RadarPoints.RADIUS); 
		dw.paintLine( rrl.x, rrl.y, rx+RadarPoints.RADIUS, ry+RadarPoints.RADIUS); 
		dw.setColor(Color.rgb(255,255,255));
		dw.setFontSize(12);

		radarText(dw, MixUtils.formatDist(radius * 1000), rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS*2 -10, false);
		radarText(dw, "" + bearing + ((char) 176) + " " + dirTxt, rx + RadarPoints.RADIUS, ry - 5, true); 

		// Get next event
		UIEvent evt = null;
		synchronized (uiEvents) {
			if (uiEvents.size() > 0) {
				evt = uiEvents.get(0);
				uiEvents.remove(0);
			}
		}
		if (evt != null && evt.type == UIEvent.KEY) {
			handleKeyEvent((KeyEvent) evt);
			evt = null;
		}
		if (evt != null && evt.type == UIEvent.CLICK) {
			handleClickEvent((ClickEvent) evt);
		}

	}
	
	private void handleKeyEvent(KeyEvent evt) {
		/** Adjust marker position with keypad */
		final float CONST = 10f;
		if (evt.keyCode == KEYCODE_DPAD_LEFT) {
			addX -= CONST;
		} else if (evt.keyCode == KEYCODE_DPAD_RIGHT) {
			addX += CONST;
		} else if (evt.keyCode == KEYCODE_DPAD_DOWN) {
			addY += CONST;
		} else if (evt.keyCode == KEYCODE_DPAD_UP) {
			addY -= CONST;
		}

		/** freeze the overlay with the camera button */
		if (evt.keyCode == KEYCODE_CAMERA) {
			frozen = !frozen;
		}
	}

	boolean handleClickEvent(ClickEvent evt) {
		boolean evtHandled = false;

		// Handle event
		if (state.nextLStatus == MixState.DONE) {
			for (int i = jLayer.markers.size() - 1; i >= 0
			&& !evtHandled; i--) {
				Marker pm = jLayer.markers.get(i);

				evtHandled = pm.fClick(evt.x, evt.y, ctx, state);
			}
		}
		return evtHandled;
	}

	void radarText(PaintScreen dw, String txt, float x, float y, boolean bg) {
		float padw = 4, padh = 2;
		float w = dw.getTextWidth(txt) + padw * 2;
		float h = dw.getTextAsc() + dw.getTextDesc() + padh * 2;
		if (bg) {
			dw.setColor(Color.rgb(0, 0, 0));
			dw.setFill(true);
			dw.paintRect(x - w / 2, y - h / 2, w, h);
			dw.setColor(Color.rgb(255, 255, 255));
			dw.setFill(false);
			dw.paintRect(x - w / 2, y - h / 2, w, h);
		}
		dw.paintText(padw + x - w / 2, padh + dw.getTextAsc() + y - h / 2,
				txt);
	}


	public void clickEvent(float x, float y) {
		synchronized (uiEvents) {
			uiEvents.add(new ClickEvent(x, y));
		}
	}

	public void keyEvent(int keyCode) {
		synchronized (uiEvents) {
			uiEvents.add(new KeyEvent(keyCode));
		}
	}

	public void clearEvents() {
		synchronized (uiEvents) {
			uiEvents.clear();
		}
	}
}

class UIEvent {
	public static int CLICK = 0, KEY = 1;
	public int type;
}

class ClickEvent extends UIEvent {
	public float x, y;

	public ClickEvent(float x, float y) {
		this.type = CLICK;
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}


class KeyEvent extends UIEvent {
	public int keyCode;

	public KeyEvent(int keyCode) {
		this.type = KEY;
		this.keyCode = keyCode;
	}

	@Override
	public String toString() {
		return "(" + keyCode + ")";
	}
}
