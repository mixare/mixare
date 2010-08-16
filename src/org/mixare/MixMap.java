package org.mixare;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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
	
	private static ArrayList<Marker> markerList;
	private static DataView dataView;
	private static MixContext ctx;
	private static GeoPoint startPoint;
	private MapView mapView;
	static MixMap map;
	private static Context thisContext;
	
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    map = this;
	    
	    setMapContext(this);
	    mapView= new MapView(this, "0327vO6h2PcKeMFBCtVK4XcPTq-b2tsXrsdbSqw");
	    mapView.setBuiltInZoomControls(true);
	    mapView.setClickable(true);
	    mapView.setSatellite(true);
	    mapView.setEnabled(true);

	    this.setContentView(mapView);
	       
		setStartPoint();
		createOverlay();
	}
	
	public void setStartPoint(){
		Location location = ctx.getCurrentLocation();
		MapController controller;

		double latitude = location.getLatitude()*1E6;
		double longitude = location.getLongitude()*1E6;

		controller = mapView.getController();
		startPoint = new GeoPoint((int)latitude, (int)longitude);
		controller.setCenter(startPoint);
		controller.setZoom(14);
	}
	
	public void createOverlay(){
		mapOverlays= mapView.getOverlays();
		OverlayItem item; 
		drawable = this.getResources().getDrawable(R.drawable.icon_map);
		MixOverlay mixOverlay = new MixOverlay(drawable);
		
		for (int i = 0; i < markerList.size(); i++) {
			GeoPoint point = new GeoPoint((int)(markerList.get(i).mGeoLoc.getLatitude()*1E6), (int)(markerList.get(i).mGeoLoc.getLongitude()*1E6));
			item = new OverlayItem(point, "", "");
			mixOverlay.addOverlay(item);
			mapOverlays.add(mixOverlay);
		}
		MixOverlay myOverlay;
	    drawable = this.getResources().getDrawable(R.drawable.loc_icon);
	    myOverlay = new MixOverlay(drawable);
	       
	    item= new OverlayItem(startPoint, "Your Position", "");
		myOverlay.addOverlay(item);
		mapOverlays.add(myOverlay); 
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
			int base = Menu.FIRST;
			/*define the first*/
			MenuItem item1 =menu.add(base, base, base, getString(dataView.MAP_MENU_NORMAL_MODE)); 
			MenuItem item2 =menu.add(base, base+1, base+1, getString(dataView.MAP_MENU_SATELLITE_MODE));
			MenuItem item3 =menu.add(base, base+2, base+2, getString(dataView.MAP_MY_LOCATION)); 
			MenuItem item4 =menu.add(base, base+3, base+3, getString(dataView.MENU_ITEM_2)); 
			MenuItem item5 =menu.add(base, base+4, base+4, getString(dataView.MENU_CAM_MODE)); 


			/*assign icons to the menu items*/
			item1.setIcon(android.R.drawable.ic_menu_gallery);
			item2.setIcon(android.R.drawable.ic_menu_mapmode);
			item3.setIcon(android.R.drawable.ic_menu_mylocation);
			item4.setIcon(android.R.drawable.ic_menu_view);
			item5.setIcon(android.R.drawable.ic_menu_camera);

			return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			/*Satellite View*/
			case 1:
			    mapView.setSatellite(false);
				break;
			/*street View*/
			case 2:		
				mapView.setSatellite(true);
				break;
			/*go to users location*/
			case 3:
				setStartPoint();
				break;
			/*List View*/
			case 4:
				createListView();
				finish();
				break;
			/*back to Camera View*/
			case 5:
				finish();
				break;
		}
		return true;
	}
	public void createListView(){
		Vector<String> listDataVector;
		Vector<String> listURL;
		MixListView.setList(2);
		listDataVector = new Vector();
		listURL = new Vector();
		/*add all marker items to a title and a URL Vector*/
		for(int i = 0; i<dataView.jLayer.markers.size();i++){
			Marker ma = new Marker();
			ma = dataView.jLayer.markers.get(i);
				listDataVector.add(ma.getText());
				/*the website for the corresponding title*/
				if(ma.getURL()!=null)
					listURL.add(ma.getURL());
				/*if no website is available for a specific title*/
				else
					listURL.add("");
		}
		/*if the list of titles to show in alternative list view is not empty*/
		if(listDataVector.size()>0){
			MixListView.setTitleVector(listDataVector);
			MixListView.setURLVector(listURL);
			MixListView.setMixContext(ctx);
			MixListView.setDataView(dataView);
			MixListView.setInfoText(getString(dataView.NO_WEBINFO_AVAILABLE));
			Intent intent1 = new Intent(MixMap.this, MixListView.class); 
			startActivityForResult(intent1, 42);
		}
		/*if the list is empty*/
		else{
			Toast.makeText( this, dataView.EMPTY_LIST_STRING_ID, Toast.LENGTH_LONG ).show();			
		}
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
	
	public static void setMapContext(Context context){
		thisContext= context;
	}
	public static Context getMapContext(){
		return thisContext;
	}
	public static void startPointMsg(){
		Toast.makeText(getMapContext(), getDataView().MAP_CURRENT_LOCATION_CLICK, Toast.LENGTH_LONG).show();
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
		return overlayItems.size();
	}
	
	@Override
	protected boolean onTap(int index){
		if(size()==1){
			MixMap.startPointMsg();
		}
		
		else if(MixMap.getDataView().jLayer.markers.get(index).getURL()!= null){
			String url = MixMap.getDataView().jLayer.markers.get(index).getURL();
			Log.d("MapView", "opern url: "+url);
			try {
				if (url != null && url.startsWith("webpage")) {
					String newUrl = MixUtils.parseAction(url);
					MixMap.getDataView().ctx.loadWebPage(newUrl, MixMap.getMapContext());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	public void addOverlay(OverlayItem overlay) {
		overlayItems.add(overlay);
	    populate();
	}
}

