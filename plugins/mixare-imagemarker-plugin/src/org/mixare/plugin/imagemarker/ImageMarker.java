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
package org.mixare.plugin.imagemarker;

import org.mixare.lib.marker.PluginMarker;
import org.mixare.lib.marker.draw.DrawCommand;
import org.mixare.lib.marker.draw.DrawImage;
import org.mixare.lib.marker.draw.DrawTextBox;
import org.mixare.lib.marker.draw.ParcelableProperty;

import android.graphics.Bitmap;

/**
 * @author A.Egal
 * An custom marker that shows a bitmap image in the augmented reality browser
 */
public class ImageMarker extends PluginMarker{

	public static final int MAX_OBJECTS = 20;
	private Bitmap image; 
	public static final int OSM_URL_MAX_OBJECTS = 5;

	public ImageMarker(int id, String title, double latitude, double longitude,
			double altitude, String URL, int type, int color) {
		super(id, title, latitude, longitude, altitude, URL, type, color);
	}

	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}
	
	@Override
	public DrawCommand[] remoteDraw(){
		DrawCommand[] dCommands = new DrawCommand[2];
		dCommands[0] = new DrawImage(isVisible, signMarker, image);
		dCommands[1] = new DrawTextBox(isVisible, distance, title, underline, textBlock, txtLab, signMarker);
		return dCommands;
	}

	@Override
	public void setExtras(String name, ParcelableProperty parcelableProperty) {
		if(name.equals("bitmap")){
			image = (Bitmap)parcelableProperty.getObject();
		}
	}
	
}