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
package org.mixare.data.convert;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.MixView;
import org.mixare.POIMarker;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.lib.HtmlUnescape;
import org.mixare.data.convert.DataProcessor;
import org.mixare.lib.marker.Marker;

import android.util.Log;

/**
 * A data processor for wikipedia urls or data, Responsible for converting raw data (to json and then) to marker data.
 * @author A. Egal
 */
public class WikiDataProcessor extends DataHandler implements DataProcessor{

	public static final int MAX_JSON_OBJECTS = 1000;
	
	@Override
	public String[] getUrlMatch() {
		String[] str = {"wiki"};
		return str;
	}

	@Override
	public String[] getDataMatch() {
		String[] str = {"wiki"};
		return str;
	}
	
	@Override
	public boolean matchesRequiredType(String type) {
		if(type.equals(DataSource.TYPE.WIKIPEDIA.name())){
			return true;
		}
		return false;
	}

	@Override
	public List<Marker> load(String rawData, int taskId, int colour) throws JSONException {
		List<Marker> markers = new ArrayList<Marker>();
		JSONObject root = convertToJSON(rawData);
		JSONArray dataArray = root.getJSONArray("geonames");
		int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

		for (int i = 0; i < top; i++) {
			JSONObject jo = dataArray.getJSONObject(i);
			
			Marker ma = null;
			if (jo.has("title") && jo.has("lat") && jo.has("lng")
					&& jo.has("elevation") && jo.has("wikipediaUrl")) {
	
				Log.v(MixView.TAG, "processing Wikipedia JSON object");
		
				//no unique ID is provided by the web service according to http://www.geonames.org/export/wikipedia-webservice.html
				ma = new POIMarker(
						"",
						HtmlUnescape.unescapeHTML(jo.getString("title"), 0), 
						jo.getDouble("lat"), 
						jo.getDouble("lng"), 
						jo.getDouble("elevation"), 
						"http://"+jo.getString("wikipediaUrl"), 
						taskId, colour);
				markers.add(ma);
			}
		}
		return markers;
	}
	
	private JSONObject convertToJSON(String rawData){
		try {
			return new JSONObject(rawData);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	
}
