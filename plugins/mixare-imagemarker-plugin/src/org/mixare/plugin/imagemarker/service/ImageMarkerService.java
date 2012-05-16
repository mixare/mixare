/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
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
package org.mixare.plugin.imagemarker.service;

import java.util.HashMap;
import java.util.Map;

import org.mixare.lib.gui.Label;
import org.mixare.lib.marker.PluginMarker;
import org.mixare.lib.marker.draw.ClickHandler;
import org.mixare.lib.marker.draw.DrawCommand;
import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;
import org.mixare.lib.service.IMarkerService;
import org.mixare.plugin.imagemarker.ImageMarker;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;

public class ImageMarkerService extends Service{
	
	public final String PLUGIN_NAME = "imagemarker";
	public final String CATEGORY_PLUGIN= "mixare.intent.category.MARKER_PLUGIN";
	private Map<String, PluginMarker> markers = new HashMap<String, PluginMarker>();
	private Integer count = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

    private final IMarkerService.Stub binder = new IMarkerService.Stub() {

		@Override
		public int getPid() throws RemoteException {
			return 0;
		}

		@Override
		public String buildMarker(int id, String title, double latitude, double longitude, double altitude, String URL, int type, int color)
				throws RemoteException {
			PluginMarker marker = new ImageMarker(id, title, latitude, longitude, altitude, URL, type, color);
			String markerName = "imageMarker-"+count+"-"+marker.getID();
			markers.put(markerName, marker);
			return markerName;
		}

		@Override
		public String getPluginName() throws RemoteException {
			return PLUGIN_NAME;
		}

		@Override
		public void calcPaint(String markerName, Camera viewCam, float addX, float addY)
				throws RemoteException {
			markers.get(markerName).calcPaint(viewCam, addX, addY);			
		}

		@Override
		public DrawCommand[] remoteDraw(String markerName) throws RemoteException {
			return markers.get(markerName).remoteDraw();
		}

		@Override
		public double getAltitude(String markerName) throws RemoteException {
			return markers.get(markerName).getAltitude();
		}

		@Override
		public int getColour(String markerName) throws RemoteException {
			return markers.get(markerName).getColour();
		}

		@Override
		public double getDistance(String markerName) throws RemoteException {
			return markers.get(markerName).getDistance();
		}

		@Override
		public String getID(String markerName) throws RemoteException {
			return markers.get(markerName).getID();
		}

		@Override
		public double getLatitude(String markerName) throws RemoteException {
			return markers.get(markerName).getLatitude();
		}

		@Override
		public MixVector getLocationVector(String markerName) throws RemoteException {
			return markers.get(markerName).getLocationVector();
		}

		@Override
		public double getLongitude(String markerName) throws RemoteException {
			return markers.get(markerName).getLongitude();
		}

		@Override
		public int getMaxObjects(String markerName) throws RemoteException {
			return markers.get(markerName).getMaxObjects();
		}

		@Override
		public String getTitle(String markerName) throws RemoteException {
			return markers.get(markerName).getTitle();
		}
		
		@Override
		public String getURL(String markerName) throws RemoteException {
			return markers.get(markerName).getURL();
		}

		@Override
		public boolean isActive(String markerName) throws RemoteException {
			return markers.get(markerName).isActive();
		}

		@Override
		public void setActive(String markerName, boolean active) throws RemoteException {
			markers.get(markerName).setActive(active);
		}

		@Override
		public void setDistance(String markerName, double distance) throws RemoteException {
			markers.get(markerName).setDistance(distance);
		}

		@Override
		public void setID(String markerName, String iD) throws RemoteException {
			markers.get(markerName).setID(iD);
		}

		@Override
		public void update(String markerName, Location curGPSFix) throws RemoteException {
			markers.get(markerName).update(curGPSFix);
		}

		@Override
		public void removeMarker(String markerName) throws RemoteException {
			markers.remove(markerName);			
		}

		@Override
		public ClickHandler fClick(String markerName)
				throws RemoteException {
			return markers.get(markerName).fClick();
		}
		
		@Override
		public MixVector getCMarker(String markerName) throws RemoteException {
			return markers.get(markerName).getCMarker();
		}

		@Override
		public MixVector getSignMarker(String markerName) throws RemoteException {
			return markers.get(markerName).getSignMarker();
		}

		@Override
		public boolean getUnderline(String markerName) throws RemoteException {
			return markers.get(markerName).getUnderline();
		}

		@Override
		public boolean isVisible(String markerName) throws RemoteException {
			return markers.get(markerName).isVisible();
		}
		
		@Override
		public void setTxtLab(String markerName, Label txtLab) throws RemoteException {
			markers.get(markerName).setTxtLab(txtLab);
		}
		
		@Override
		public Label getTxtLab(String markerName) throws RemoteException {
			return markers.get(markerName).getTxtLab();
		}

		@Override
		public void setExtrasParc(String markerName, String name,
				ParcelableProperty value) throws RemoteException {
			markers.get(markerName).setExtras(name, value);
		}

		@Override
		public void setExtrasPrim(String markerName, String name,
				PrimitiveProperty value) throws RemoteException {
			markers.get(markerName).setExtras(name, value);
			
		}
    };

}