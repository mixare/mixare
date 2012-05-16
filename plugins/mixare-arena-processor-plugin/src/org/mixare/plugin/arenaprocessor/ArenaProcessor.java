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
package org.mixare.plugin.arenaprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.lib.HtmlUnescape;
import org.mixare.lib.data.PluginDataProcessor;
import org.mixare.lib.marker.InitialMarkerData;
import org.mixare.lib.marker.draw.ParcelableProperty;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ArenaProcessor extends PluginDataProcessor {

	public static final int MAX_JSON_OBJECTS = 1000;

	@Override
	public String[] getUrlMatch() {
		String[] str = { "arena" };
		return str;
	}

	@Override
	public String[] getDataMatch() {
		String[] str = { "arena" };
		return str;
	}

	@Override
	public List<InitialMarkerData> load(String rawData, int taskId, int colour)
			throws JSONException {
		List<InitialMarkerData> initialMarkerDatas = new ArrayList<InitialMarkerData>();
		JSONObject root = convertToJSON(rawData);
		JSONArray dataArray = root.getJSONArray("results");
		int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

		for (int i = 0; i < top; i++) {
			JSONObject jo = dataArray.getJSONObject(i);
			if (jo.has("title") && jo.has("lat") && jo.has("lng")
					&& jo.has("elevation")) {

				String link = null;

				if (jo.has("has_detail_page")
						&& jo.getInt("has_detail_page") != 0
						&& jo.has("webpage"))
					link = jo.getString("webpage");

				Bitmap image = getBitmapFromURL(jo.getString("object_url"));
				
				InitialMarkerData ma = new InitialMarkerData(jo.getInt("id"),
						HtmlUnescape.unescapeHTML(jo.getString("title"), 0),
						jo.getDouble("lat"), jo.getDouble("lng"),
						jo.getDouble("elevation"), link, taskId, colour);
				ma.setMarkerName("imagemarker");
				ma.setExtras("bitmap", new ParcelableProperty(
						"android.graphics.Bitmap", image));
				initialMarkerDatas.add(ma);
			}
		}
		return initialMarkerDatas;
	}

	public Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			return BitmapFactory.decodeStream(input);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}		
	}
}
