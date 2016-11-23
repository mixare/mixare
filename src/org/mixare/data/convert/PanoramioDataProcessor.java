/*
 * Copyright (C) 2012 
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
 * 
 * ----------------------Images Licenses ----------------------
 * 
 * Photos provided by Panoramio are under the copyright of 
 * their owners" under Panoramio photos. Please, check the 
 * Panoramio API - Terms of Use for detailed requirements
 */
package org.mixare.data.convert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.ImageMarker;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.lib.HtmlUnescape;
import org.mixare.lib.marker.Marker;
import android.util.Log;

/**
 * Data Processor that Handles Panoramio API results.
 * 
 * <b>
 * Note: Images and data MUST link to Panoramio photo page.
 * (NOT THE IMAGE IT SELF)
 * <b>
 * Note: Photos provided by Panoramio are under the copyright of 
 * their owners" under Panoramio photos. Please, check the 
 * Panoramio API - Terms of Use for detailed requirements
 * </b>
 * 
 * @see http://www.panoramio.com/api/data/api.html
 * @author devBinnooh
 *
 */
public class PanoramioDataProcessor extends DataHandler implements DataProcessor{

	
	public static final int MAX_JSON_OBJECTS = ImageMarker.maxObjects;

	/**
	 * Panoramio URL match
	 * {@inheritDoc}
	 */
	@Override
	public String[] getUrlMatch() {
		String[] str = {"Panoramio"};
		return str;
	}

	/**
	 * Panoramio data match
	 * {@inheritDoc}
	 */
	@Override
	public String[] getDataMatch() {
		String[] str = {"photos"};
		return str;
	}

	/**
	 * Panoramio
	 * {@inheritDoc}
	 */
	@Override
	public boolean matchesRequiredType(String type) {
		if(type.equals(DataSource.TYPE.PANORAMIO.name())){
			return true;
		}
		return false;
	}

	/**
	 * Reads and creates Markers based on Panoramio API returned results.
	 * Example JSON:
	 *  <pre>
	 *  {
	 *  "count": 773840,"photos": [
	 *  	{
	 *  		"photo_id": 532693,
	 *  		"photo_title": "Wheatfield in afternoon light",
	 *  		"photo_url": "http://www.panoramio.com/photo/532693",
	 *  		"photo_file_url": "http://static2.bareka.com/photos/medium/532693.jpg",
	 *  		"longitude": 11.280727,
	 *  		"latitude": 59.643198,
	 *  		"width": 500,
	 *  		"height": 333,
	 *  		"upload_date": "22 January 2007",
	 *  		"owner_id": 39160,
	 *  		"owner_name": "Snemann",
	 *  		"owner_url": "http://www.panoramio.com/user/39160",
	 *  }, ...
	 *  </pre>
	 *  
	 * @param String rawData 
	 * @param int taskId
	 * @param int color
	 * @return List<Marker> List of Markers
	 */
	@Override
	public List<Marker> load(String rawData, int taskId, int colour)
			throws JSONException {
		final List<Marker> markers = new ArrayList<Marker>();
		final JSONObject root = convertToJSON(rawData);
		JSONArray dataArray = root.getJSONArray("photos");
		int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());
		
		Log.i("Mixare", "Processing Panoramio Results ...");
		
		for (int i = 0; i < top; i++) {
			JSONObject jo = dataArray.getJSONObject(i);
			
			if (jo.has("photo_id") && jo.has("latitude") && jo.has("longitude")
					&& jo.has("photo_file_url")) {

				// For Panoramio elevation, generate a random number ranged [30 -
				// 120]
				// @TODO find better way
				// http://www.geonames.org/export/web-services.html#astergdem
				// http://asterweb.jpl.nasa.gov/gdem.asp
				final Random elevation = new Random();
				markers.add(new ImageMarker(jo.getString("photo_id"),
						HtmlUnescape.unescapeHTML(jo.getString("photo_title"), 0),
						jo.getDouble("latitude"), 
						jo.getDouble("longitude"),
						(elevation.nextInt(90) + 30), // @TODO elevation level for Panoramio
						jo.getString("photo_url"),
						taskId,
						colour,
						jo.getString("owner_name"),
						jo.getString("photo_file_url")));
			}
		}
		return markers;
	}

	private JSONObject convertToJSON(String rawData) {
		try {
			return new JSONObject(rawData);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

}
