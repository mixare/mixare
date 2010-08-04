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
import org.mixare.MixListView;
import org.mixare.reality.PhysicalPlace;

import android.util.Log;

public class Json {

	public String lUrl;

	public ArrayList<Marker> markers = new ArrayList<Marker>();
	//Vector to store the data (titles) showed in the alternative list view
	public Vector<String> listData= new Vector();
	//Vector to store the URLs to the corresponding titles 
	public Vector<String> listOnPress= new Vector();


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

				jo = root.getJSONObject(i);
				
				//südtirolerland
				if (jo.has("id")&& jo.has("title")&& jo.has("lat")) {
					//Our own schema
					if (jo.getInt("has_detail_page") != 0) {
						ma.mOnPress = "webpage:" + java.net.URLDecoder.decode(jo.getString("webpage"));
						//a Vector with the URLs corresponding to the titles is created
						listOnPress.add("webpage:" + java.net.URLDecoder.decode(jo.getString("webpage")));
					}
					ma.mText = jo.getString("title");
					refpt.setLatitude(jo.getDouble("lat"));
					refpt.setLongitude(jo.getDouble("lng"));
					refpt.setAltitude(jo.getDouble("elevation"));
					ma.mGeoLoc.setTo(refpt);

					markers.add(ma);
					
					//a vector with the titles for the alternative list view is created
					String title = jo.getString("title");
					listData.add(title);


				} 
				//wikipedia
				else if(MixListView.getDataSource()=="Wikipedia"){ 
					ma.mOnPress = "webpage:http://" + java.net.URLDecoder.decode(jo.getString("wikipediaUrl"));
					listOnPress.add("webpage:http://" + java.net.URLDecoder.decode(jo.getString("wikipediaUrl")));
					
					ma.mText = jo.getString("title");
					refpt.setLatitude(jo.getDouble("lat"));
					refpt.setLongitude(jo.getDouble("lng"));
					refpt.setAltitude(jo.getDouble("elevation"));
					ma.mGeoLoc.setTo(refpt);

					markers.add(ma);
					
					//a vector with the titles for the alternative list view is created
					String title = jo.getString("title");
					listData.add(title);

				}
				//twitter
				else				
					if(MixListView.getDataSource()=="Twitter") {
						ma.mOnPress = "";
						listOnPress.add("");
	
						ma.mText = jo.getString("text");					
						JSONObject geo = jo.getJSONObject("geo");
						JSONArray coordinates = geo.getJSONArray("coordinates");
						
						String lat = coordinates.getString(0);
						String lng= coordinates.getString(1);
						
						refpt.setLatitude(Double.parseDouble(lat));
						refpt.setLongitude(Double.parseDouble(lng));
			
						refpt.setAltitude(0);
						ma.mGeoLoc.setTo(refpt);
						
						markers.add(ma);
						
						//a vector with the titles for the alternative list view is created
						String title = jo.getString("location");
						listData.add(title);
					
					}
				// Buzz
				else if (MixListView.getDataSource()=="Buzz") {
					String webpage = jo.getJSONObject("links").getJSONArray("alternate").getJSONObject(0).getString("href");
					ma.mOnPress = "webpage:" + java.net.URLDecoder.decode(webpage);
					listOnPress.add("webpage:" + java.net.URLDecoder.decode(webpage));
					
					ma.mText = jo.getString("title");
					refpt.setLatitude(Double.valueOf(jo.getString("geocode").split(" ")[0]));
					refpt.setLongitude(Double.valueOf(jo.getString("geocode").split(" ")[1]));
					refpt.setAltitude(0);
					
					ma.mGeoLoc.setTo(refpt);
					markers.add(ma);
					
					String title = jo.getString("title");
					listData.add(title);
				}

				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
