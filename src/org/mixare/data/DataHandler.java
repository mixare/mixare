package org.mixare.data;

import java.net.URLDecoder;
import java.util.ArrayList;

import org.mixare.Marker;

/**
 * DataHandler is the model which provides the Marker Objects.
 * 
 * DataHandler is also the Factory for new Marker objects.
 */
public class DataHandler {

	private static final int MAX_OBJECTS = 50;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();

	public void createMarker(String title, double latitude, double longitude, double elevation, String link, DataSource.DATASOURCE datasource) {
		if (markerList.size() < MAX_OBJECTS) {
			String URL = null;
			if (link != null && link.length() > 0)
				URL = "webpage:" + URLDecoder.decode(link);
			Marker ma = new Marker(title, latitude, longitude, elevation, URL, datasource);
			markerList.add(ma);
		}
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
