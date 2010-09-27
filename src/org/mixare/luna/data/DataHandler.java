package org.mixare.data;

import java.util.ArrayList;

import org.json.JSONObject;
import org.mixare.Marker;
import org.mixare.reality.PhysicalPlace;

public class DataHandler {

	public ArrayList<Marker> markers = new ArrayList<Marker>();
	public static final int MAX_OBJECTS = 50;
	
	protected Marker createMarker(String title, double latitude, double longitude, double elevation, String link) {
		PhysicalPlace refpt = new PhysicalPlace();
		Marker ma = new Marker();
		
		if(link != null && link.length()>0) {
			ma.mOnPress = "webpage:" + java.net.URLDecoder.decode(link);
		}
		
		ma.mText = title;
		refpt.setLatitude(latitude);
		refpt.setLongitude(longitude);
		refpt.setAltitude(elevation);
		ma.mGeoLoc.setTo(refpt);
		if(markers.size()<MAX_OBJECTS)
			markers.add(ma);
		return ma;
	}

	public ArrayList<Marker> getMarkers() {
		return markers;
	}

	public void setMarkers(ArrayList<Marker> markers) {
		this.markers = markers;
	}
	
}
