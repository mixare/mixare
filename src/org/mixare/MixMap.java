package org.mixare;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MixMap extends MapActivity{
	
	private static List<Overlay> mapOverlays;
	private Drawable drawable;
	private MixOverlay myOverlay;
	private static ArrayList<Marker> markerList;
	private static DataView dataView;
	private static MixContext ctx;
	private static GeoPoint geoPoint;
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    Log.d("MAP-------------------------", "MAP created");
	    
	    MapController controller;
	    
	    MapView mapView;
	    mapView= new MapView(this, "0327vO6h2PcKeMFBCtVK4XcPTq-b2tsXrsdbSqw");//private key : "0327vO6h2PcJo-bWWc6Vuc9woahxZV7ZMjFe4YQ");//
	    mapView.setBuiltInZoomControls(true);
	    mapView.setClickable(true);
	    mapView.setSatellite(true);
	    mapView.setStreetView(true);
	    mapView.setEnabled(true);

	    this.setContentView(mapView);
	    
	    LocationManager locationMgr = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Location location = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		double latitude = location.getLatitude()*1E6;
		double longitude = location.getLongitude()*1E6;

		controller = mapView.getController();
		GeoPoint startPoint = new GeoPoint((int)latitude, (int)longitude);
		setGeoPoint(startPoint);
		
		controller.setCenter(startPoint);
		controller.setZoom(14);
		
		mapOverlays= mapView.getOverlays();
	    drawable = this.getResources().getDrawable(R.drawable.loc_icon);
	    myOverlay = new MixOverlay(drawable);
	    
	    OverlayItem item = new OverlayItem(startPoint, "Your Position", "");
		myOverlay.addOverlay(item);
		mapOverlays.add(myOverlay);    
		
		drawable = this.getResources().getDrawable(R.drawable.icon_map);
		MixOverlay mixOverlay = new MixOverlay(drawable);
		
		for (int i = 0; i < markerList.size(); i++) {
			GeoPoint point = new GeoPoint((int)(markerList.get(i).mGeoLoc.getLatitude()*1E6), (int)(markerList.get(i).mGeoLoc.getLongitude()*1E6));
			OverlayItem newItem = new OverlayItem(point, markerList.get(i).getText(), "");
			mixOverlay.addOverlay(newItem);
			mapOverlays.add(mixOverlay);
		}
		
		
	}
	
	public static GeoPoint getGeoPoint(){
		return geoPoint;
	}
	public static void setGeoPoint(GeoPoint p){
		 geoPoint=p;
	}
	
	public static ArrayList<Marker> getMarkerList(){
		return markerList;
	}
	
	public static void setMarkerList(ArrayList<Marker> maList){
		markerList= maList;
	}
	public static DataView getDataView(){
		return dataView;
	}
	
	public static void setDataView(DataView view){
		dataView= view;
	}
	public static void setMixContext(MixContext context){
		ctx= context;
	}
	public static MixContext getMixContext(){
		return ctx;
	}
	public static List<Overlay> getMapOverlayList(){
		return mapOverlays;
	}

}


class MixOverlay extends ItemizedOverlay{

	private ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
	
	public MixOverlay(Drawable marker){
		super (boundCenterBottom(marker));
	}
	
	@Override
	protected OverlayItem createItem(int i) {
	  return overlayItems.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		  return overlayItems.size();
	}
	
	@Override
	protected boolean onTap(int index){
		Log.d("-----------MAP---------------", "clicked");
		Log.d("-----------MAP---------------", "clicked "+MixMap.getDataView().jLayer.markers.get(index).getURL());
		if(MixMap.getDataView().jLayer.markers.get(index).getURL()!= null){
			MixMap.getDataView().state.handleEvent(MixMap.getDataView().ctx, MixMap.getDataView().jLayer.markers.get(index).getURL());
		}
		else
			Log.d("-----------MAP---------------", "no further info available");

		return true;
	}
//	public boolean onTouchEvent (MotionEvent event, MapView mapView){
//		if(event.getAction()==MotionEvent.ACTION_UP){
//			MixMap.getMapOverlayList().
//			MixMap.getDataView().state.handleEvent(MixMap.getMixContext(), selectedItemURL.get(position));
//		}
//		return false;
//	}
	
	public void addOverlay(OverlayItem overlay) {
		overlayItems.add(overlay);
	    populate();
	}
}

