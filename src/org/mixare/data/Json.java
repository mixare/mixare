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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.Marker;
import org.mixare.MixView;
import org.mixare.POIMarker;
import org.mixare.SocialMarker;
import org.mixare.data.DataSource.DATAFORMAT;

import android.util.Log;

public class Json extends DataHandler {

	public static final int MAX_JSON_OBJECTS=1000;

	public List<Marker> load(JSONObject root, DATAFORMAT dataformat) {
		JSONObject jo = null;
		JSONArray dataArray = null;
    	List<Marker> markers=new ArrayList<Marker>();

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

				Log.i(MixView.TAG, "processing "+dataformat+" JSON Data Array");
				int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

				for (int i = 0; i < top; i++) {					
					
					jo = dataArray.getJSONObject(i);
					Marker ma = null;
					switch(dataformat) {
						case MIXARE: ma = processMixareJSONObject(jo); break;
						case BUZZ: ma = processBuzzJSONObject(jo); break;
						case TWITTER: ma = processTwitterJSONObject(jo); break;
						case WIKIPEDIA: ma = processWikipediaJSONObject(jo); break;
						default: ma = processMixareJSONObject(jo); break;
					}
					if(ma!=null)
						markers.add(ma);
				}
			}
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return markers;
	}
	
	public Marker processBuzzJSONObject(JSONObject jo) throws NumberFormatException, JSONException {
		Marker ma = null;
		if (jo.has("title") && jo.has("geocode") && jo.has("links")) {
			Log.v(MixView.TAG, "processing Google Buzz JSON object");

			ma = new SocialMarker(
					jo.getString("title"), 
					Double.valueOf(jo.getString("geocode").split(" ")[0]), 
					Double.valueOf(jo.getString("geocode").split(" ")[1]), 
					0, 
					jo.getJSONObject("links").getJSONArray("alternate").getJSONObject(0).getString("href"), 
					DataSource.DATASOURCE.BUZZ);
		}
		return ma;
	}

	public Marker processTwitterJSONObject(JSONObject jo) throws NumberFormatException, JSONException {
		Marker ma = null;
		if (jo.has("geo")&& !jo.isNull("geo")) {
			Log.v(MixView.TAG, "processing Twitter JSON object");
			JSONObject geo = jo.getJSONObject("geo");
			JSONArray coordinates = geo.getJSONArray("coordinates");
			String user=jo.getString("from_user");
			String url="http://twitter.com/"+user;
			
			ma = new SocialMarker(
					user+": "+jo.getString("text"), 
					Double.parseDouble(coordinates.getString(0)), 
					Double.parseDouble(coordinates.getString(1)), 
					0, url, 
					DataSource.DATASOURCE.TWITTER);
		}
		return ma;
	}

	public Marker processMixareJSONObject(JSONObject jo) throws JSONException {

		Marker ma = null;
		if (jo.has("title") && jo.has("lat") && jo.has("lng") && jo.has("elevation") && jo.has("has_detail_page")) {
	
			Log.v(MixView.TAG, "processing Mixare JSON object");
			String link=null;
	
			if(jo.getInt("has_detail_page")!=0 && jo.has("webpage"))
				link=jo.getString("webpage");
			
			ma = new POIMarker(
					jo.getString("title"), 
					jo.getDouble("lat"), 
					jo.getDouble("lng"), 
					jo.getDouble("elevation"), 
					link, 
					DataSource.DATASOURCE.OWNURL);
		}
		return ma;
	}

	public Marker processWikipediaJSONObject(JSONObject jo) throws JSONException {

		Marker ma = null;
		if (jo.has("title") && jo.has("lat") && jo.has("lng") && jo.has("elevation") && jo.has("wikipediaUrl")) {

			Log.v(MixView.TAG, "processing Wikipedia JSON object");
	
			ma = new POIMarker(
					jo.getString("title"), 
					jo.getDouble("lat"), 
					jo.getDouble("lng"), 
					jo.getDouble("elevation"), 
					"http://"+jo.getString("wikipediaUrl"), 
					DataSource.DATASOURCE.WIKIPEDIA);
		}
		return ma;
	}
}