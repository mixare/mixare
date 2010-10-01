package org.mixare.data;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mixare.Marker;
import org.mixare.MarkersOrder;
import org.mixare.MixView;

import android.location.Location;
import android.util.Log;

/**
 * DataHandler is the model which provides the Marker Objects.
 * 
 * DataHandler is also the Factory for new Marker objects.
 */
public class DataHandler {

	private List<Marker> markerList = new ArrayList<Marker>();
	
	public void createMarker(String title, double latitude, double longitude, double elevation, String link, DataSource.DATASOURCE datasource) {
		String URL = null;
		if (link != null && link.length() > 0)
			URL = "webpage:" + URLDecoder.decode(link);
		Marker ma = new Marker(title, latitude, longitude, elevation, URL, datasource);
		markerList.add(ma);
	}
	
	public void addMarkers(List<Marker> markers) {
		markerList.addAll(markers);
		Log.d(MixView.TAG, "Marker count: "+markerList.size());
	}
	
	public void sortMarkerList() {
		Collections.sort(markerList, MarkersOrder.getInstance()); 
	}
	
	public void updateDistances(double lat, double lon) {
		for(Marker ma: markerList) {
			float[] dist=new float[3];
			Location.distanceBetween(ma.getLatitude(), ma.getLongitude(), lat, lon, dist);
			ma.setDistance(dist[0]);
		}
		sortMarkerList();
	}
	
	public void clearMarkerList() {
		markerList.clear();
	}

	/**
	 * @deprecated Nobody should get direct access to the list
	 */
	public List getMarkerList() {
		return markerList;
	}
	
	/**
	 * @deprecated Nobody should get direct access to the list
	 */
	public void setMarkerList(List markerList) {
		this.markerList = markerList;
	}

	public int getMarkerCount() {
		return markerList.size();
	}
	
	public Marker getMarker(int index) {
		return markerList.get(index);
	}
}
