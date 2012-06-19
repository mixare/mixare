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
 * TODO
 * @author DevBinnooh
 *
 */
public class PanoramioDataProcessor extends DataHandler implements DataProcessor{

	
	public static final int MAX_JSON_OBJECTS = ImageMarker.maxObjects;

	@Override
	public String[] getUrlMatch() {
		String[] str = {"Panoramio"};
		return str;
	}

	@Override
	public String[] getDataMatch() {
		String[] str = {"Panoramio"};
		return str;
	}

	@Override
	public boolean matchesRequiredType(String type) {
		if(type.equals(DataSource.TYPE.PANORAMIO.name())){
			return true;
		}
		return false;
	}

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
