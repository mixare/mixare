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
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map.Entry;

import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.gui.PaintScreen;
import org.mixare.gui.RadarPoints;
import org.mixare.gui.ScreenLine;
import org.mixare.render.Camera;

import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;


/**
 * This class is able to update the markers and the radar.
 * It also handles some user events
 * 
 * @author daniele
 *
 */
public class DataView {

	/**current context */
	private MixContext mixContext;
	/** is the view Inited? */
	private boolean isInit;
	
	/** width and height of the view*/
	private int width, height;
	
	/** _NOT_ the android camera, the class that takes care of the transformation*/
	private Camera cam;

	private MixState state = new MixState();
	
	/** The view can be "frozen" for debug purposes */
	private boolean frozen;

	private Location curFix;
	private DataHandler dataHandler = new DataHandler();	
	private float radius = 20;
	
	/**IDs for the MENU ITEMS and MENU OPTIONS used in MixView class*/
	public static final int EMPTY_LIST_STRING_ID = R.string.empty_list;
	public static final int OPTION_NOT_AVAILABLE_STRING_ID = R.string.option_not_available;
	public static final int EMPTY_LIST_STRIG_ID = R.string.empty_list;
	public static final int MENU_ITEM_1 = R.string.menu_item_1;
	public static final int MENU_ITEM_2 = R.string.menu_item_2;
	public static final int MENU_ITEM_3 = R.string.menu_item_3;
	public static final int MENU_ITEM_4 = R.string.menu_item_4;
	public static final int MENU_ITEM_5 = R.string.menu_item_5;
	public static final int MENU_ITEM_6 = R.string.menu_item_6;
	public static final int MENU_ITEM_7 = R.string.menu_item_7;

	public static final int CONNECTION_ERROR_DIALOG_TEXT = R.string.connection_error_dialog;
	public static final int CONNECTION_ERROR_DIALOG_BUTTON1 = R.string.connection_error_dialog_button1;
	public static final int CONNECTION_ERROR_DIALOG_BUTTON2 = R.string.connection_error_dialog_button2;
	public static final int CONNECTION_ERROR_DIALOG_BUTTON3 = R.string.connection_error_dialog_button3;
	
	public static final int CONNECTION_GPS_DIALOG_TEXT = R.string.connection_GPS_dialog_text;
	public static final int CONNECTION_GPS_DIALOG_BUTTON1 = R.string.connection_GPS_dialog_button1;
	public static final int CONNECTION_GPS_DIALOG_BUTTON2 = R.string.connection_GPS_dialog_button2;

	/*if in the listview option for a specific title no website is provided*/
	public static final int NO_WEBINFO_AVAILABLE = R.string.no_website_available;
	public static final int LICENSE_TEXT = R.string.license;
	public static final int LICENSE_TITLE = R.string.license_title;
	public static final int CLOSE_BUTTON = R.string.close_button;
	
	/*Strings for general information*/
	public static final int GENERAL_INFO_TITLE = R.string.general_info_title;
	public static final int GENERAL_INFO_TEXT = R.string.general_info_text;
	public static final int GPS_LONGITUDE = R.string.longitude;
	public static final int GPS_LATITUDE = R.string.latitude;
	public static final int GPS_ALTITUDE = R.string.altitude;
	public static final int GPS_SPEED = R.string.speed;
	public static final int GPS_ACCURACY = R.string.accuracy;
	public static final int GPS_LAST_FIX = R.string.gps_last_fix;

	public static final int MAP_MENU_NORMAL_MODE = R.string.map_menu_normal_mode;
	public static final int MAP_MENU_SATELLITE_MODE = R.string.map_menu_satellite_mode;
	public static final int MENU_CAM_MODE = R.string.map_menu_cam_mode;
	public static final int MAP_MY_LOCATION = R.string.map_my_location;
	public static final int MAP_CURRENT_LOCATION_CLICK = R.string.map_current_location_click;

	public static final int DATA_SOURCE_CHANGE_WIKIPEDIA = R.string.data_source_change_wikipedia;
	public static final int DATA_SOURCE_CHANGE_TWITTER = R.string.data_source_change_twitter;
	public static final int DATA_SOURCE_CHANGE_OSM = R.string.data_source_change_osm;
	public static final int SEARCH_FAILED_NOTIFICATION = R.string.search_failed_notification;
	public static final int SOURCE_OPENSTREETMAP=R.string.source_openstreetmap;
	public static final int SEARCH_ACTIVE_1=R.string.search_active_1;
	public static final int SEARCH_ACTIVE_2=R.string.search_active_2;
		
	private boolean isStartedByLauncher;
	
	private ArrayList<UIEvent> uiEvents = new ArrayList<UIEvent>();

	private RadarPoints radarPoints = new RadarPoints();
	private ScreenLine lrl = new ScreenLine();
	private ScreenLine rrl = new ScreenLine();
	private float rx = 10, ry = 20;
	private float addX = 0, addY = 0;


	/**
	 * Constructor
	 */
	public DataView(MixContext ctx) {
		this.mixContext = ctx;
	}
	
	public MixContext getContext() {
		return mixContext;
	}

	public boolean isStartedByLauncher() {
		return isStartedByLauncher;
	}
	
	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}
	
	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public DataHandler getDataHandler() {
		return dataHandler;
	}
	
	public boolean isDetailsView() {
		return state.isDetailsView();
	}
	
	public void setDetailsView(boolean detailsView) {
		state.setDetailsView(detailsView);
	}

	public void doStart() {
		state.nextLStatus = MixState.NOT_STARTED;
		mixContext.setLocationAtLastDownload(curFix);
	}

	public boolean isInited() {
		return isInit;
	}

	public void init(int widthInit, int heightInit) {
		try {
			width = widthInit;
			height = heightInit;

			cam = new Camera(width, height, true);
			float hva = Compatibility.getHorizontalViewAngle();
			cam.setViewAngle(hva);

			lrl.set(0, -RadarPoints.RADIUS);
			lrl.rotate(hva / 2);
			lrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
			rrl.set(0, -RadarPoints.RADIUS);
			rrl.rotate(-hva / 2);
			rrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		frozen = false;
		isInit = true;
	}
	
	public void requestData(String url) {
		DownloadRequest request = new DownloadRequest();
		request.source = new DataSource("LAUNCHER", url, DataSource.TYPE.MIXARE, DataSource.DISPLAY.CIRCLE_MARKER, true);
		request.params = "";
		mixContext.setAllDataSourcesforLauncher(request.source);
		new DownloadManager(mixContext).execute(request);
		state.nextLStatus = MixState.PROCESSING;
	}

	public void requestData(DataSource datasource, double lat, double lon, double alt, float radius, String locale) {
		DownloadRequest request = new DownloadRequest();
		request.params = datasource.createRequestParams(lat, lon, alt, radius, locale);
		request.source = datasource;
		
		new DownloadManager(mixContext).execute(request);
		state.nextLStatus = MixState.PROCESSING;
		
	}
	public void draw(PaintScreen dw) {
		mixContext.getRM(cam.transform);
		curFix = mixContext.getCurrentLocation();

		state.calcPitchBearing(cam.transform);

		// Load Layer
		if (state.nextLStatus == MixState.NOT_STARTED && !frozen) {
						
			if (mixContext.getStartUrl().length() > 0){
				requestData(mixContext.getStartUrl());
				isStartedByLauncher = true;
			}

			else {
				double lat = curFix.getLatitude(), lon = curFix.getLongitude(),alt = curFix.getAltitude();
				ArrayList<DataSource> allDataSources = mixContext.getAllDataSources();
				for(DataSource ds: allDataSources) {
					/*when type is OpenStreetMap
					 * iterate the URL list and for selected URL send data request 
					 * */
					if (ds.getEnabled()) {
						requestData(ds,lat, lon, alt, radius, Locale.getDefault().getLanguage());
					}
				}
			}
			
			// if no datasources are activated
			if(state.nextLStatus==MixState.NOT_STARTED) 
				state.nextLStatus=MixState.DONE;
			
			//TODO:
			//state.downloadId = mixContext.getDownloader().submitJob(request);

		
		} else if (state.nextLStatus == MixState.PROCESSING) {
			dataHandler.onLocationChanged(curFix);
		}

		
		// Update markers
		dataHandler.updateActivationStatus(mixContext);
		for (int i = dataHandler.getMarkerCount()-1; i >= 0; i--) {
			Marker ma = dataHandler.getMarker(i);
			//if (ma.isActive() && (ma.getDistance() / 1000f < radius || ma instanceof NavigationMarker || ma instanceof SocialMarker)) {
			if (ma.isActive() && (ma.getDistance() / 1000f < radius)) {
				
				// To increase performance don't recalculate position vector
				// for every marker on every draw call, instead do this only 
				// after onLocationChanged and after downloading new marker
				//if (!frozen) 
				//	ma.update(curFix);
				if(!frozen) 
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
		if (evt != null) {
			switch (evt.type) {
				case UIEvent.KEY:	handleKeyEvent((KeyEvent) evt);		break;
				case UIEvent.CLICK:	handleClickEvent((ClickEvent) evt);	break;
			}
		}
		state.nextLStatus = MixState.PROCESSING;				
	}

	private void handleKeyEvent(KeyEvent evt) {
		/** Adjust marker position with keypad */
		final float CONST = 10f;
		switch (evt.keyCode) {
			case KEYCODE_DPAD_LEFT:		addX -= CONST;		break;
			case KEYCODE_DPAD_RIGHT:	addX += CONST;		break;
			case KEYCODE_DPAD_DOWN:		addY += CONST;		break;
			case KEYCODE_DPAD_UP:		addY -= CONST;		break;
			case KEYCODE_DPAD_CENTER:	frozen = !frozen;		break;
			case KEYCODE_CAMERA:		frozen = !frozen;	break;	// freeze the overlay with the camera button
		}
	}
	
	boolean handleClickEvent(ClickEvent evt) {
		boolean evtHandled = false;

		// Handle event
		if (state.nextLStatus == MixState.DONE) {
			//the following will traverse the markers in ascending order (by distance) the first marker that 
			//matches triggers the event.
			for (int i = 0 ; i < dataHandler.getMarkerCount() && !evtHandled; i++) {
				Marker pm = dataHandler.getMarker(i);

				evtHandled = pm.fClick(evt.x, evt.y, mixContext, state);
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
		dw.paintText(padw + x - w / 2, padh + dw.getTextAsc() + y - h / 2, txt, false);
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
	public static final int CLICK	= 0;
	public static final int KEY		= 1;
	
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
