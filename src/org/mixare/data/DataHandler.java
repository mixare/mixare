package org.mixare.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mixare.Marker;
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
	//private Set<Marker> markerList = new HashSet<Marker>();
	
	public void addMarkers(List<Marker> markers) {

		Log.d(MixView.TAG, "Marker before: "+markerList.size());
		for(Marker ma:markers) {
			if(!markerList.contains(ma))
				markerList.add(ma);
		}
		//markerList.addAll(markers);
		
		Log.d(MixView.TAG, "Marker count: "+markerList.size());
	}
	
	public void sortMarkerList() {
		Collections.sort(markerList); 
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
