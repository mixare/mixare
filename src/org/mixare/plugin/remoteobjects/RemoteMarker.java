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
package org.mixare.plugin.remoteobjects;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.marker.draw.ClickHandler;
import org.mixare.lib.marker.draw.DrawCommand;
import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;
import org.mixare.lib.service.IMarkerService;
import org.mixare.plugin.PluginNotFoundException;

import android.location.Location;
import android.os.RemoteException;

/**
 * The remote marker sends request to the (remote)plugin that it is connected to through IMarkerService.
 * the remote marker is treated like a normal marker in the core. And it overrides the marker interface.
 * @author A. Egal
 */
public class RemoteMarker implements Marker{

	private String markerName;
	private IMarkerService iMarkerService;
	
	public RemoteMarker(IMarkerService iMarkerService){
		this.iMarkerService = iMarkerService;
	}

	public int getPid() {
		return 0;
	}

	public void buildMarker(int id, String title, double latitude, double longitude, double altitude, String url, int type, int color){
		try {
			this.markerName = iMarkerService.buildMarker(id, title, latitude, longitude, altitude, url, type, color);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	public String getPluginName(){
		try {
			return iMarkerService.getPluginName();
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void calcPaint(Camera viewCam, float addX, float addY){	
		try {
			iMarkerService.calcPaint(markerName, viewCam, addX, addY);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void draw(PaintScreen dw) {
		try {
			DrawCommand[] drawCommands= iMarkerService.remoteDraw(markerName);
			for(DrawCommand drawCommand: drawCommands){
				drawCommand.draw(dw);
				if(drawCommand.getProperty("textlab") != null){
					setTxtLab((Label)((ParcelableProperty)drawCommand.getProperty("textlab")).getObject());
				}
			}
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		} catch (NullPointerException ne){
			throw new PluginNotFoundException(ne);
		}
	}

	@Override
	public double getAltitude() {
		try {
			return iMarkerService.getAltitude(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public int getColour() {
		try {
			return iMarkerService.getColour(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public double getDistance() {
		try {
			return iMarkerService.getDistance(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public String getID() {
		try {
			return iMarkerService.getID(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public double getLatitude() {
		try {
			return iMarkerService.getLatitude(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public MixVector getLocationVector() {
		try {
			return iMarkerService.getLocationVector(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public double getLongitude() {
		try {
			return iMarkerService.getLongitude(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public int getMaxObjects() {
		try {
			return iMarkerService.getMaxObjects(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public String getTitle() {
		try {
			return iMarkerService.getTitle(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public Label getTxtLab() {
		try {
			return iMarkerService.getTxtLab(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}
	
	public void setTxtLab(Label txtLab){
		try{
			if(txtLab != null){
				iMarkerService.setTxtLab(markerName, txtLab);
			}
		} catch (RemoteException e){
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public String getURL() {
		try {
			return iMarkerService.getURL(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public boolean isActive() {
		try {
			return iMarkerService.isActive(markerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void setActive(boolean active) {
		try {
			iMarkerService.setActive(markerName, active);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void setDistance(double distance) {
		try {
			iMarkerService.setDistance(markerName, distance);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void setID(String iD) {
		try {
			iMarkerService.setID(markerName, iD);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void update(Location curGPSFix) {
		try {
			iMarkerService.update(markerName, curGPSFix);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}
	
	public void setExtras(String name, ParcelableProperty parcelableProperty){
		try {
			iMarkerService.setExtrasParc(markerName, name, parcelableProperty);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}
	
	public void setExtras(String name, PrimitiveProperty primitiveProperty){
		try {
			iMarkerService.setExtrasPrim(markerName, name, primitiveProperty);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public boolean fClick(float x, float y, MixContextInterface ctx, MixStateInterface state) {
		ClickHandler clickHandler;
		try {
			clickHandler = iMarkerService.fClick(markerName);
			return clickHandler.handleClick(x, y, ctx, state);
		} catch (RemoteException e) {
			throw new PluginNotFoundException();
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof RemoteMarker){
			RemoteMarker rm = (RemoteMarker)o;
			if(rm.markerName.equals(this.markerName)){
				return true;
			}
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return markerName.hashCode() + iMarkerService.hashCode();
	}

	@Override
	public int compareTo(Marker another) {
		Marker rm = (Marker)another;
		return this.getID().compareTo(rm.getID());
	}

}