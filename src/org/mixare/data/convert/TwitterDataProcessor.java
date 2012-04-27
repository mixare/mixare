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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.MixView;
import org.mixare.SocialMarker;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.lib.marker.Marker;

import android.util.Log;

/**
 * A data processor for twitter urls or data, Responsible for converting raw data (to json and then) to marker data.
 * @author A. Egal
 */
public class TwitterDataProcessor extends DataHandler implements DataProcessor{

	public static final int MAX_JSON_OBJECTS = 1000;
	
	@Override
	public String[] getUrlMatch() {
		String[] str = {"twitter"};
		return str;
	}

	@Override
	public String[] getDataMatch() {
		String[] str = {"twitter"};
		return str;
	}
	
	@Override
	public boolean matchesRequiredType(String type) {
		if(type.equals(DataSource.TYPE.TWITTER.name())){
			return true;
		}
		return false;
	}

	@Override
	public List<Marker> load(String rawData, int taskId, int colour)
			throws JSONException {
		List<Marker> markers = new ArrayList<Marker>();
		JSONObject root = convertToJSON(rawData);
		JSONArray dataArray = root.getJSONArray("results");
		int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

		for (int i = 0; i < top; i++) {
			JSONObject jo = dataArray.getJSONObject(i);
			
			Marker ma = null;
			if (jo.has("geo")) {
				Double lat = null, lon = null;
	
				if (!jo.isNull("geo")) {
					JSONObject geo = jo.getJSONObject("geo");
					JSONArray coordinates = geo.getJSONArray("coordinates");
					lat = Double.parseDouble(coordinates.getString(0));
					lon = Double.parseDouble(coordinates.getString(1));
				} else if (jo.has("location")) {
	
					// Regex pattern to match location information
					// from the location setting, like:
					// iPhone: 12.34,56.78
					// ÜT: 12.34,56.78
					// 12.34,56.78
	
					Pattern pattern = Pattern
							.compile("\\D*([0-9.]+),\\s?([0-9.]+)");
					Matcher matcher = pattern.matcher(jo.getString("location"));
	
					if (matcher.find()) {
						lat = Double.parseDouble(matcher.group(1));
						lon = Double.parseDouble(matcher.group(2));
					}
				}
				if (lat != null) {
					Log.v(MixView.TAG, "processing Twitter JSON object");
					String user=jo.getString("from_user");
					String url="http://twitter.com/"+user;
					
					//no ID is needed, since identical tweet by identical user may be safely merged into one.
					ma = new SocialMarker(
							"",
							user+": "+jo.getString("text"), 
							lat, 
							lon, 
							0, url, 
							taskId, colour);
					markers.add(ma);
				}
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
