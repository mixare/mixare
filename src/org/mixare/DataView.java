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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;


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
	String HOME_URL = "http://ws.geonames.org/findNearbyWikipediaJSON";


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
		state.curFix = ctx.getCurrentLocation();

		state.calcPitchBearing(cam.transform);

		state.screenWidth = width;
		state.screenHeight = height;

		// Load Layer
		if (state.nextLStatus == MixState.NOT_STARTED ) {

			Intent intent = ((Activity) ctx.mixView).getIntent();
			if (intent.getAction() != null &&
					intent.getAction().equals("org.mixare.VIEW")) {
				String[] data = intent.getStringArrayExtra("DATA");
				for (int i = 0; i < data.length; i++) {
					Marker marker = Marker.parse(data[0]);
					System.out.println("marker:"+marker.mId);
					state.jLayer.markers.add(marker);
				}

				state.nextLStatus = MixState.DONE;
			} else {


				DownloadRequest request = new DownloadRequest();

				if (!ctx.getStartUrl().equals(""))
					request.url = ctx.getStartUrl();
				else 
					request.url = HOME_URL + "?lat="+state.curFix.getLatitude()+"&lng=" + state.curFix.getLongitude() + "&radius="+ state.radius +"&maxRows=50&lang=" + Locale.getDefault().getLanguage();

				state.startUrl = ctx.getStartUrl();

				state.downloadId = ctx.getDownloader().submitJob(request);


				state.nextLStatus = MixState.PROCESSING;
			}

		} else if (state.nextLStatus == MixState.PROCESSING) {
			if (ctx.getDownloader().isReqComplete(state.downloadId)) {
				state.dRes = ctx.getDownloader().getReqResult(state.downloadId);

				if (state.dRes.error && retry < 3) {
					retry++;
					state.nextLStatus = MixState.NOT_STARTED;

				} else {
					retry = 0;
					state.nextLStatus = MixState.DONE;
					state.jLayer = (Json) state.dRes.obj;

					// Sort markers by cMarker.z
					Collections.sort(state.jLayer.markers, new MarkersOrder());

				}


			}
		} 

		// Update markers
		for (int i = 0; i < state.jLayer.markers.size(); i++) {
			Marker ma = state.jLayer.markers.get(i);
			float[] dist = new float[1];
			dist[0] = 0;
			Location.distanceBetween(ma.mGeoLoc.getLatitude(), ma.mGeoLoc.getLongitude(), ctx.getCurrentLocation().getLatitude(), ctx.getCurrentLocation().getLongitude(), dist);
			if (dist[0] / 1000f < Float.parseFloat(state.radius)) {
				if (!frozen) ma.update(state.curFix, System.currentTimeMillis());
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
		radarText(dw, MixUtils.formatDist(Float.parseFloat(state.radius) * 1000), rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS*2 -10, false);
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
			for (int i = state.jLayer.markers.size() - 1; i >= 0
			&& !evtHandled; i--) {
				Marker pm = state.jLayer.markers.get(i);

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
