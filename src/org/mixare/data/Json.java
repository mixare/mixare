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

import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.Marker;
import org.mixare.MixView;
import org.mixare.reality.PhysicalPlace;

import android.util.Log;

public class Json {

	public String lUrl;

	public ArrayList<Marker> markers = new ArrayList<Marker>();
	//Vector to store the data (titles) showed in the alternative list view
	public Vector<String> listData= new Vector();
	//Vector to store the URLs to the corresponding titles 
	public Vector<String> listOnPress= new Vector();
	public static final int MAX_OBJECTS = 50;


	public Json() {
	}
	
	public void processBuzzJSONObject(JSONObject jo) throws NumberFormatException, JSONException {
		if (jo.has("title") && jo.has("geocode") && jo.has("links")) {
			Log.d(MixView.TAG, "processing Google Buzz JSON data");
			createMarker(	jo.getString("title"),
							Double.valueOf(jo.getString("geocode").split(" ")[0]),
							Double.valueOf(jo.getString("geocode").split(" ")[1]),
							0,
							jo.getJSONObject("links").getJSONArray("alternate").getJSONObject(0).getString("href"));
		}
	}

	public void processTwitterJSONObject(JSONObject jo) throws NumberFormatException, JSONException {
		if (jo.has("geo")) {

			Log.d(MixView.TAG, "processing Twitter JSON data");
			JSONObject geo = jo.getJSONObject("geo");
			JSONArray coordinates = geo.getJSONArray("coordinates");
	
			createMarker(	jo.getString("text"),
							Double.parseDouble(coordinates.getString(0)),
							Double.parseDouble(coordinates.getString(1)),
							0,
							null);
		}
	}
	
	public void processMixareJSONObject(JSONObject jo) throws JSONException {
		
		if (jo.has("title") && jo.has("lat") && jo.has("lng") && jo.has("elevation") && jo.has("has_detail_page")) {

			Log.d(MixView.TAG, "processing Mixare JSON data");
			String link=null;
			
			if(jo.getInt("has_detail_page")!=0 && jo.has("webpage"))
				link=jo.getString("webpage");
	
			createMarker(	jo.getString("title"),
							jo.getDouble("lat"),
							jo.getDouble("lng"),
							jo.getDouble("elevation"),
							link);
			
		}
	}
	
	public void processWikipediaJSONObject(JSONObject jo) throws JSONException {

		if (jo.has("title") && jo.has("lat") && jo.has("lng") && jo.has("elevation") && jo.has("wikipediaUrl")) {

			Log.d(MixView.TAG, "processing Wikipedia JSON data");
			createMarker(	jo.getString("title"),
							jo.getDouble("lat"),
							jo.getDouble("lng"),
							jo.getDouble("elevation"),
							jo.getString("wikipediaUrl"));
			
		}
	}
	
	private void createMarker(String title, double latitude, double longitude, double elevation, String link) {

		PhysicalPlace refpt = new PhysicalPlace();
		Marker ma = new Marker();
		
		if(link != null && link.length()>0) {
			ma.mOnPress = "webpage:" + java.net.URLDecoder.decode(link);
			//a Vector with the URLs corresponding to the titles is created
			listOnPress.add("webpage:" + java.net.URLDecoder.decode(link));
		}
		
		ma.mText = title;
		refpt.setLatitude(latitude);
		refpt.setLongitude(longitude);
		refpt.setAltitude(elevation);
		ma.mGeoLoc.setTo(refpt);

		markers.add(ma);
		//a vector with the titles for the alternative list view is created
		listData.add(title);
	}

	public void load(JSONObject root) {
		JSONObject jo = null;
		JSONArray dataArray = null;

		try {

			// Twitter & own schema
			if(root.has("results"))
				dataArray = root.getJSONArray("results");
			// Wikipedia
			else if (root.has("geonames"))
				dataArray = root.getJSONArray("geonames");
			// Google Buzz
			else if (root.has("data") && root.getJSONObject("data").has("items"))
				dataArray = root.getJSONObject("data").getJSONArray("items");

			if (dataArray != null) {

				Log.i(MixView.TAG, "processing JSON Data Array");
				int top = Math.min(50, dataArray.length());
 
				for (int i = 0; i < top; i++) {
					jo = dataArray.getJSONObject(i);
					
					processMixareJSONObject(jo);
					processWikipediaJSONObject(jo); 
					processTwitterJSONObject(jo);
					processBuzzJSONObject(jo);
					
				} 
			}
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
