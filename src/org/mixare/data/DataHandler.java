package org.mixare.data;

import java.util.ArrayList;

import org.mixare.Marker;
import org.mixare.reality.PhysicalPlace;

/**
 * DataHandler is the model which provides the Marker Objects.
 * 
 * DataHandler is also the Factory for new Marker objects.
 */
public class DataHandler {

	private static final int MAX_OBJECTS = 50;

	private ArrayList<Marker> markerList = new ArrayList<Marker>();

	protected Marker createMarker(String title, double latitude, double longitude, double elevation, String link) {
		PhysicalPlace refpt = new PhysicalPlace();
		Marker ma = new Marker();

		if (link != null && link.length() > 0) {
			ma.mOnPress = "webpage:" + java.net.URLDecoder.decode(link);
		}

		ma.mText = title;
		refpt.setLatitude(latitude);
		refpt.setLongitude(longitude);
		refpt.setAltitude(elevation);
		ma.mGeoLoc.setTo(refpt);
		if (markerList.size() < MAX_OBJECTS)
			markerList.add(ma);
		return ma;
	}
	
	/**
	 * @deprecated Nobody should get direct access to the list
	 */
	public ArrayList getMarkerList() {
		return markerList;
	}
	
	/**
	 * @deprecated Nobody should get direct access to the list
	 */
	public void setMarkerList(ArrayList markerList) {
		this.markerList = markerList;
	}

	public int getMarkerCount() {
		return markerList.size();
	}
	
	public Marker getMarker(int index) {
		return markerList.get(index);
	}
}
