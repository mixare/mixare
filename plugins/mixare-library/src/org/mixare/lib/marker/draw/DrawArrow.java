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

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.render.MixVector;

import android.graphics.Path;
import android.os.Parcel;

/**
 * A draw command that can be send by a plugin marker to draw an arrow on the client.
 * This class extends the DrawCommand, that stores the properties, so that it can be
 * transfered to the client.
 * @author A. Egal
 */
public class DrawArrow extends DrawCommand{

	private static String CLASS_NAME = DrawArrow.class.getName();
	
	private static String PROPERTY_NAME_VISIBLE = "visible";
	private static String PROPERTY_NAME_CMARKER = "cMarker";
	private static String PROPERTY_NAME_SIGNMARKER = "signMarker";
	
	public static DrawArrow init(Parcel in){
		Boolean visible = Boolean.valueOf(in.readString());
		ParcelableProperty cMarkerHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		ParcelableProperty signMarkerHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		return new DrawArrow(visible, (MixVector)cMarkerHolder.getObject(), (MixVector)signMarkerHolder.getObject());
	}
	
	public DrawArrow(boolean visible, MixVector cMarker, MixVector signMarker) {
		super(CLASS_NAME);
		setProperty(PROPERTY_NAME_VISIBLE, visible);
		setProperty(PROPERTY_NAME_CMARKER, cMarker);
		setProperty(PROPERTY_NAME_SIGNMARKER, signMarker);
	}

	/**
	 * The main method that draws the arrow.
	 */
	@Override
	public void draw(PaintScreen dw) {
		if (getBooleanProperty(PROPERTY_NAME_VISIBLE)) {
			MixVector cMarker = getMixVectorProperty(PROPERTY_NAME_CMARKER);
			MixVector signMarker = getMixVectorProperty(PROPERTY_NAME_SIGNMARKER);
			
			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);
			float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

			dw.setStrokeWidth(maxHeight / 10f);
			dw.setFill(false);

			float radius = maxHeight / 1.5f;
			
			Path arrow = buildArrow(maxHeight, radius);			
			dw.paintPath(arrow,cMarker.x,cMarker.y,radius*2,radius*2,currentAngle+90,1);			
		}
	}
	
	private Path buildArrow(float maxHeight, float radius){
		Path arrow = new Path();
		float x=0;
		float y=0;
		arrow.moveTo(x-radius/3, y+radius);
		arrow.lineTo(x+radius/3, y+radius);
		arrow.lineTo(x+radius/3, y);
		arrow.lineTo(x+radius, y);
		arrow.lineTo(x, y-radius);
		arrow.lineTo(x-radius, y);
		arrow.lineTo(x-radius/3,y);
		arrow.close();
		return arrow;
	}

}
