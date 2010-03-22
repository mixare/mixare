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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.Marker;
import org.mixare.reality.PhysicalPlace;

public class Json {

	public String lUrl;

	public ArrayList<Marker> markers = new ArrayList<Marker>();

	public Json() {
	}

	public void load(JSONArray root) {
		JSONObject jo = null;
		int top = 50;
		if (root.length() <= top) {
			top = root.length();
		}

		for (int i = 0; i < top; i++) {
			PhysicalPlace refpt = new PhysicalPlace();
			Marker ma = new Marker();

			try {

				String locId = null;
				jo = root.getJSONObject(i);

				if (jo.has("id")) {
					//Our own schema
					locId = jo.getString("id");
					if (jo.getInt("has_detail_page") != 0) {
						ma.mOnPress = "webpage:" + java.net.URLDecoder.decode(jo.getString("webpage"));
					}

				} else {
					//geonames
					locId = jo.getString("title");
					ma.mOnPress = "webpage:http://" + java.net.URLDecoder.decode(jo.getString("wikipediaUrl"));
				}

				ma.mText = jo.getString("title");
				refpt.setLatitude(jo.getDouble("lat"));
				refpt.setLongitude(jo.getDouble("lng"));
				refpt.setAltitude(jo.getDouble("elevation"));
				ma.mId = locId;
				ma.mGeoLoc.setTo(refpt);


				ma.layer = this;

				markers.add(ma);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
