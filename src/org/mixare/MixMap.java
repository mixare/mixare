/*
 * Copyright (C) 2010- Peer internet solutions
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

package org.mixare;

import java.util.ArrayList;
import java.util.List;

import org.mixare.data.DataHandler;
import org.mixare.data.DataSourceList;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * This class creates the map view and its overlay. It also adds an overlay with
 * the markers to the map.
 */
public class MixMap extends MapActivity implements OnTouchListener{

	private static List<Overlay> mapOverlays;
	private Drawable drawable;

	private static List<Marker> markerList;
	private static DataView dataView;
	private static GeoPoint startPoint;

	private MixContext mixContext;
	private MapView mapView;

	static MixMap map;
	private static Context thisContext;
	private static TextView searchNotificationTxt;
	public static List<Marker> originalMarkerList;


	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataView = MixView.dataView;
		mixContext = dataView.getContext();
		setMarkerList(dataView.getDataHandler().getMarkerList());
		map = this;

		setMapContext(this);
		mapView= new MapView(this, "0bynx7meN9jlSdHQ4-lK_Vzsw-T82UVibnI0nCA");
		mapView.setBuiltInZoomControls(true);
		mapView.setClickable(true);
		mapView.setSatellite(true);
		mapView.setEnabled(true);

		this.setContentView(mapView);

		setStartPoint();
		createOverlay();

		if (dataView.isFrozen()){
			searchNotificationTxt = new TextView(this);
			searchNotificationTxt.setWidth(MixView.dWindow.getWidth());
			searchNotificationTxt.setPadding(10, 2, 0, 0);			
			searchNotificationTxt.setText(getString(DataView.SEARCH_ACTIVE_1)+" "+ DataSourceList.getDataSourcesStringList() + getString(DataView.SEARCH_ACTIVE_2));
			searchNotificationTxt.setBackgroundColor(Color.DKGRAY);
			searchNotificationTxt.setTextColor(Color.WHITE);

			searchNotificationTxt.setOnTouchListener(this);
			addContentView(searchNotificationTxt, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}

	public void setStartPoint() {
		Location location = mixContext.getCurrentLocation();
		MapController controller;

		double latitude = location.getLatitude()*1E6;
		double longitude = location.getLongitude()*1E6;

		controller = mapView.getController();
		startPoint = new GeoPoint((int)latitude, (int)longitude);
		controller.setCenter(startPoint);
		controller.setZoom(15);
	}

	public void createOverlay(){
		mapOverlays=mapView.getOverlays();
		OverlayItem item; 
		drawable = this.getResources().getDrawable(R.drawable.icon_map);
		MixOverlay mixOverlay = new MixOverlay(this, drawable);

		for(Marker marker:markerList) {
			if(marker.isActive()) {
				GeoPoint point = new GeoPoint((int)(marker.getLatitude()*1E6), (int)(marker.getLongitude()*1E6));
				item = new OverlayItem(point, "", "");
				mixOverlay.addOverlay(item);
			}
		}
		//Solved issue 39: only one overlay with all marker instead of one overlay for each marker
		mapOverlays.add(mixOverlay);

		MixOverlay myOverlay;
		drawable = this.getResources().getDrawable(R.drawable.loc_icon);
		myOverlay = new MixOverlay(this, drawable);

		item = new OverlayItem(startPoint, "Your Position", "");
		myOverlay.addOverlay(item);
		mapOverlays.add(myOverlay); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int base = Menu.FIRST;
		/*define the first*/
		MenuItem item1 =menu.add(base, base, base, getString(DataView.MAP_MENU_NORMAL_MODE)); 
		MenuItem item2 =menu.add(base, base+1, base+1, getString(DataView.MAP_MENU_SATELLITE_MODE));
		MenuItem item3 =menu.add(base, base+2, base+2, getString(DataView.MAP_MY_LOCATION)); 
		MenuItem item4 =menu.add(base, base+3, base+3, getString(DataView.MENU_ITEM_2)); 
		MenuItem item5 =menu.add(base, base+4, base+4, getString(DataView.MENU_CAM_MODE)); 

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
		if (dataView.getDataHandler().getMarkerCount() > 0) {
			Intent intent1 = new Intent(MixMap.this, MixListView.class); 
			startActivityForResult(intent1, 42);
		}
		/*if the list is empty*/
		else{
			Toast.makeText( this, DataView.EMPTY_LIST_STRING_ID, Toast.LENGTH_LONG ).show();			
		}
	}

//	public static ArrayList<Marker> getMarkerList(){
//		return markerList;
//	}

	public void setMarkerList(List<Marker> maList){
		markerList = maList;
	}

	public DataView getDataView(){
		return dataView;
	}

//	public static void setDataView(DataView view){
//		dataView= view;
//	}

//	public static void setMixContext(MixContext context){
//		ctx= context;
//	}
//
//	public static MixContext getMixContext(){
//		return ctx;
//	}

	public List<Overlay> getMapOverlayList(){
		return mapOverlays;
	}

	public void setMapContext(Context context){
		thisContext= context;
	}

	public Context getMapContext(){
		return thisContext;
	}

	public void startPointMsg(){
		Toast.makeText(getMapContext(), DataView.MAP_CURRENT_LOCATION_CLICK, Toast.LENGTH_LONG).show();
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMixSearch(query);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void doMixSearch(String query) {
		DataHandler jLayer = dataView.getDataHandler();
		if (!dataView.isFrozen()) {
			originalMarkerList = jLayer.getMarkerList();
			MixListView.originalMarkerList = jLayer.getMarkerList();
		}
		markerList = new ArrayList<Marker>();

		for(int i = 0; i < jLayer.getMarkerCount(); i++) {
			Marker ma = jLayer.getMarker(i);

			if (ma.getTitle().toLowerCase().indexOf(query.toLowerCase())!=-1){
				markerList.add(ma);
			}
		}
		if(markerList.size()==0){
			Toast.makeText( this, getString(DataView.SEARCH_FAILED_NOTIFICATION), Toast.LENGTH_LONG ).show();
		}
		else{
			jLayer.setMarkerList(markerList);
			dataView.setFrozen(true);

			finish();
			Intent intent1 = new Intent(this, MixMap.class); 
			startActivityForResult(intent1, 42);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		dataView.setFrozen(false);
		dataView.getDataHandler().setMarkerList(originalMarkerList);

		searchNotificationTxt.setVisibility(View.INVISIBLE);
		searchNotificationTxt = null;
		finish();
		Intent intent1 = new Intent(this, MixMap.class); 
		startActivityForResult(intent1, 42);

		return false;
	}

}


class MixOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
	private MixMap mixMap;

	public MixOverlay(MixMap mixMap, Drawable marker){
		super (boundCenterBottom(marker));
		//need to call populate here. See
		//http://code.google.com/p/android/issues/detail?id=2035
		populate();
		this.mixMap = mixMap;
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
		if (size() == 1)
			mixMap.startPointMsg();
		else if (mixMap.getDataView().getDataHandler().getMarker(index).getURL() !=  null) {
			String url = mixMap.getDataView().getDataHandler().getMarker(index).getURL();
			Log.d("MapView", "opern url: "+url);
			try {
				if (url != null && url.startsWith("webpage")) {
					String newUrl = MixUtils.parseAction(url);
					mixMap.getDataView().getContext().loadWebPage(newUrl, mixMap.getMapContext());
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

