package org.mixare.lib.data;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.lib.marker.InitialMarkerData;

public abstract class PluginDataProcessor {

	public abstract String[] getUrlMatch();
	
	public abstract String[] getDataMatch();
	
	public abstract List<InitialMarkerData> load(String rawData, int taskId, int colour) throws JSONException;
	
	protected JSONObject convertToJSON(String rawData){
		try {
			return new JSONObject(rawData);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
