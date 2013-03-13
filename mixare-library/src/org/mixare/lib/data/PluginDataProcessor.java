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
package org.mixare.lib.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.lib.marker.InitialMarkerData;

/**
 * DataProcessor abstract class for plugins. This dataprocessor can be picked by the 
 * DataConvertor class. It checks if the url matches the strings returned from the getUrlMatch()
 * or the data that matches the strings returned from the getDataMatch(). If this dataprocessor is selected
 * the the load method will be called. The load method converts that rawdata (xml, json, html) to markers.
 * @author A. Egal
 */
public abstract class PluginDataProcessor {

	/**
	 * @return the strings that should match the url.
	 */
	public abstract String[] getUrlMatch();

	/**
	 * @return the strings that should match the content of the url.
	 */
	public abstract String[] getDataMatch();
	
	/**
	 * This method converts raw data (xml, json, html) from the content from an url, to marker objects
	 * @return a list of markerdata, which can be used to build markers.
	 */
	public abstract List<InitialMarkerData> load(String rawData, int taskId, int colour) throws JSONException;
	
	protected JSONObject convertToJSON(String rawData){
		try {
			return new JSONObject(rawData);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
