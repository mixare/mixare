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

package org.mixare.data;

import java.net.MalformedURLException;
import java.net.URL;

import org.mixare.R;
import org.mixare.data.convert.DataConvertor;
import org.mixare.data.convert.PanoramioDataProcessor;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * The DataSource class is able to create the URL where the information about a
 * place can be found.
 * 
 * @author hannes
 * @author KlemensE
 */
public class DataSource {
	private static int DataSourceId = 0;

	private int id;
	private String name;
	private String url;
	private boolean enabled;
	private TYPE type;
	private DISPLAY display;
	private boolean editable;
	private BLUR blur;

	/**
	 * Recreate's a previously existing DataSource
	 * 
	 * @param id
	 *            The id the DataSource previously had
	 * @param name
	 *            The name of the DataSource
	 * @param url
	 *            The URL of the DataSource
	 * @param typeString
	 *            The type of the DataSource using DataSource.TYPE, it has to be
	 *            a integer in a String
	 * @param display
	 *            The type of the DataSource using DataSource.DISPLAY, it has to
	 *            be a integer in a String
	 * @param enabled
	 *            Whether the DataSource is enabled or not
	 */
	public DataSource(int id, String name, String url, String typeString,
			String displayString, String enabledString, boolean editable) {
		DataSource.DataSourceId = id + 1;
		this.id = id;
		this.name = name;
		this.url = url;
		this.type = TYPE.values()[Integer.parseInt(typeString)];
		this.display = DISPLAY.values()[Integer.parseInt(displayString)];
		this.enabled = Boolean.parseBoolean(enabledString);
		this.editable = editable;
		this.blur = BLUR.NONE;
	}

	/**
	 * Create's a new DataSource
	 * 
	 * @param name
	 *            The name of the DataSource
	 * @param url
	 *            The URL of the DataSource
	 * @param type
	 *            The type of the DataSource using DataSource.TYPE
	 * @param display
	 *            The type of the DataSource using DataSource.DISPLAY
	 * @param enabled
	 *            Whether the DataSource is enabled or not
	 */
	public DataSource(String name, String url, TYPE type, DISPLAY display,
			boolean enabled) {
		this.id = DataSourceId;
		this.name = name;
		this.url = url;
		this.type = type;
		this.display = display;
		this.enabled = enabled;
		this.editable = true;
		this.blur = BLUR.NONE;
		increasId();
	}

	/* Methods */
	public String createRequestParams(double lat, double lon, double alt,
			float radius, String locale) {
		String ret = "";
		if (!ret.startsWith("file://")) {
			switch (this.type) {

			case WIKIPEDIA:
				// Free service limited to 20km
				float geoNamesRadius = radius > 20 ? 20 : radius;
				ret += "?lat=" + lat + "&lng=" + lon + "&radius="
						+ geoNamesRadius + "&maxRows=50" + "&lang=" + locale
						+ "&username=mixare";
				break;

			case TWITTER:
				ret += "?geocode=" + lat + "%2C" + lon + "%2C"
						+ Math.max(radius, 1.0) + "km";
				break;

			case MIXARE:
				ret += "?latitude=" + Double.toString(lat) + "&longitude="
						+ Double.toString(lon) + "&altitude="
						+ Double.toString(alt) + "&radius="
						+ Double.toString(radius);
				break;

			case ARENA:
				ret += "&lat=" + Double.toString(lat) + "&lng="
						+ Double.toString(lon);
				break;

			case OSM:
				ret += DataConvertor.getOSMBoundingBox(lat, lon, radius);
				break;
			case PANORAMIO:
				final float minLong = (float) (lon - radius / 100.0);
				final float minLat = (float) (lat - radius / 100.0);
				final float maxLong = (float) (lon + radius / 100.0);
				final float maxLat = (float) (lat + radius / 100.0);
				ret += "?set=public&from=0&to="
						+ PanoramioDataProcessor.MAX_JSON_OBJECTS + "&minx="
						+ minLong + "&miny=" + minLat + "&maxx=" + maxLong
						+ "&maxy=" + maxLat + "&size=thumbnail&mapfilter=true";
			}

		}

		return ret;
	}

	private void increasId() {
		DataSourceId++;
	}

	/**
	 * Check the minimum required data
	 * 
	 * @return true if URL and Name are correct
	 */
	public boolean isWellFormed() {
		boolean out = false;
		try {
			URL asdf = new URL(getUrl());
		} catch (MalformedURLException e) {
			return false;
		}
		if (getName() != null || !getName().isEmpty()) {
			out = true;
		}
		return out;
	}

	@Override
	public String toString() {
		return "DataSource [name=" + name + ", url=" + url + ", enabled="
				+ enabled + ", type=" + type + ", display=" + display
				+ ", blur=" + blur + "]";
	}

	/* Getter and Setter */

	public BLUR getBlur() {
		return this.blur;
	}

	public int getBlurId() {
		return this.blur.ordinal();
	}

	public void setBlur(BLUR blur) {
		this.blur = blur;
	}
	
	public void setBlur(int id) {
		this.blur = BLUR.values()[id];
	}

	public int getColor() {
		int ret;
		switch (this.type) {
		case TWITTER:
			ret = Color.rgb(50, 204, 255);
			break;
		case WIKIPEDIA:
			ret = Color.RED;
			break;
		case ARENA:
			ret = Color.RED;
			break;
		case PANORAMIO:
			ret = Color.GREEN;
			break;
		default:
			ret = Color.WHITE;
			break;
		}
		return ret;
	}

	public int getDataSourceIcon() {
		int ret;
		switch (this.type) {
		case TWITTER:
			ret = R.drawable.twitter;
			break;
		case OSM:
			ret = R.drawable.osm;
			break;
		case WIKIPEDIA:
			ret = R.drawable.wikipedia;
			break;
		case ARENA:
			ret = R.drawable.arena;
			break;
		case PANORAMIO:
//			ret = R.drawable.ic_launcher;
			/*
			 * Logo from http://www.tilo-hensel.de/free-glossy-community-icons
			 * Created by Tilo Hensel licensed under
			 * http://creativecommons.org/licenses/by-nc-sa/3.0/
			 */
			 ret = R.drawable.panoramio;
			break;
		default:
			ret = R.drawable.ic_launcher;
			break;
		}
		return ret;
	}

	public int getDataSourceId() {
		return this.id;
	}

	public int getDisplayId() {
		return this.display.ordinal();
	}

	public int getTypeId() {
		return this.type.ordinal();
	}

	public DISPLAY getDisplay() {
		return this.display;
	}
	
	public void setDisplay(int id) {
		this.display = DISPLAY.values()[id];
	}
	
	public void setDisplay(DISPLAY display) {
		this.display = display;
	}

	public TYPE getType() {
		return this.type;
	}
	
	public void setType(int id) {
		this.type = TYPE.values()[id];
	}
	
	public void setType(TYPE type) {
		this.type = type;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean isChecked) {
		this.enabled = isChecked;
	}

	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return this.url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isEditable() {
		return editable;
	}

	/* ENUM */
	
	public enum TYPE {
		WIKIPEDIA, TWITTER, OSM, MIXARE, ARENA, PANORAMIO
	};

	public enum DISPLAY {
		CIRCLE_MARKER, NAVIGATION_MARKER, IMAGE_MARKER
	};

	public enum BLUR {
		NONE, ADD_RANDOM, TRUNCATE
	}
}
