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
package org.mixare.lib.marker.draw;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.render.MixVector;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A click validator class to handle the clicks on the core side, 
 * the plugin will create a click validator and send it to the core
 * And the core will validate the click and then reacts on the click. 
 * This is done because we are unable to send a MixContext and
 * a MixState to the plugin because they cannot be parselable. 
 * --
 * The main function of this class is to be a container of objects, sending
 * them to the core, and use them to check for clicks and handle it.
 * @author A. Egal
 */
public class ClickHandler implements Parcelable{

	private String url;
	private boolean active;
	private Label txtLab;
	private MixVector signMarker;
	private MixVector cMarker;
	private final ScreenLine pPt = new ScreenLine();
	
	public ClickHandler(String url, boolean active, Label txtLab, MixVector signMarker, MixVector cMarker){
		this.url = url;
		this.active = active;
		this.txtLab = txtLab;
		this.signMarker = signMarker;
		this.cMarker = cMarker;		
	}
	
	public ClickHandler(Parcel in){
		readFromParcel(in);
	}
	
	/**
	 * A click handler without a click valid check.
	 */
	public boolean fakeClick(MixContextInterface ctx, MixStateInterface state){
		return state.handleEvent(ctx, url);
	}
	
	public boolean handleClick(float x, float y, MixContextInterface ctx, MixStateInterface state){
		if(isClickValid(x, y)){
			return state.handleEvent(ctx, url);
		}
		return false;
	}
	
	private boolean isClickValid(float x, float y) {
	
		float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				signMarker.x, signMarker.y);
		//if the marker is not active (i.e. not shown in AR view) we don't have to check it for clicks
		if (!active)
			return false;

		//TODO adapt the following to the variable radius!
		pPt.x = x - signMarker.x;
		pPt.y = y - signMarker.y;
		pPt.rotate((float) Math.toRadians(-(currentAngle + 90)));
		pPt.x += txtLab.getX();
		pPt.y += txtLab.getY();

		float objX = txtLab.getX() - txtLab.getWidth() / 2;
		float objY = txtLab.getY() - txtLab.getHeight() / 2;
		float objW = txtLab.getWidth();
		float objH = txtLab.getHeight();

		if (pPt.x > objX && pPt.x < objX + objW && pPt.y > objY
				&& pPt.y < objY + objH) {
			return true;
		} else {
			return false;
		}
	}
	
	public static final Parcelable.Creator<ClickHandler> CREATOR = new Parcelable.Creator<ClickHandler>() {
		public ClickHandler createFromParcel(Parcel in) {
			return new ClickHandler(in);
		}
		
		public ClickHandler[] newArray(int size) {
			return new ClickHandler[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(url);	
		dest.writeString(String.valueOf(active));	
		dest.writeParcelable(txtLab, 0);
		dest.writeParcelable(signMarker, 0);
		dest.writeParcelable(cMarker, 0);
	}
	
	public void readFromParcel(Parcel in){
		url = in.readString();
		active = Boolean.valueOf(in.readString());
		txtLab = in.readParcelable(Label.class.getClassLoader());
		signMarker = in.readParcelable(MixVector.class.getClassLoader());
		cMarker = in.readParcelable(MixVector.class.getClassLoader());
	}

}
