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

import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.render.MixVector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.util.Log;

/**
 * A draw command that can be send by a plugin marker to draw an image on the client.
 * This class extends the DrawCommand, that stores the properties, so that it can be
 * transfered to the client.
 * @author A. Egal
 */
public class DrawImage extends DrawCommand{
	
	private static String CLASS_NAME = DrawImage.class.getName();
	
	private static String PROPERTY_NAME_VISIBLE = "visible";
	private static String PROPERTY_NAME_SIGNMARKER = "signMarker";
	private static String PROPERTY_NAME_IMAGE = "image";
	
	public static DrawImage init(Parcel in){
		Boolean visible = Boolean.valueOf(in.readString());
		ParcelableProperty signMarkerHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		ParcelableProperty bitmapHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		return new DrawImage(visible, (MixVector)signMarkerHolder.getObject(), (Bitmap)bitmapHolder.getObject());
	}
	
	public DrawImage(boolean visible,MixVector signMarker, Bitmap image) {
		super(CLASS_NAME);
		setProperty(PROPERTY_NAME_VISIBLE, visible);
		setProperty(PROPERTY_NAME_SIGNMARKER, new ParcelableProperty("org.mixare.lib.render.MixVector", signMarker));
		setProperty(PROPERTY_NAME_IMAGE,  new ParcelableProperty("android.graphics.Bitmap",image));
	}
	
	@Override
	public void draw(PaintScreen dw){
		if (getBooleanProperty(PROPERTY_NAME_VISIBLE)) {
			MixVector signMarker = getMixVectorProperty(PROPERTY_NAME_SIGNMARKER);
			Bitmap bitmap = getBitmapProperty(PROPERTY_NAME_IMAGE);
			
			dw.setColor(Color.argb(155, 255, 255, 255));
			if(bitmap == null){
				Log.e("mixare-lib", "bitmap = null");
				return;
			}
			dw.paintBitmap(bitmap, signMarker.x - (bitmap.getWidth()/2), signMarker.y - (bitmap.getHeight() / 2));
		}
	}	
	
}
