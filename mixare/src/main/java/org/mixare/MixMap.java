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
import org.mixare.lib.marker.Marker;
import org.mixare.lib.MixUtils;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
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
import com.google.android.maps.Projection;

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
	private static List<GeoPoint> walkingPath = new ArrayList<GeoPoint>();
	
	public static final String PREFS_NAME = "MixMapPrefs";

	private MixContext mixContext;
	private MapView mapView;

	//static MixMap map; 
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
		dataView = MixView.getDataView();
		setMixContext(dataView.getContext());
		setMarkerList(dataView.getDataHandler().getMarkerList());
		//map = this; //savedInstanceState will save the instance for you.

		setMapContext(this);
		setMapView(new MapView(this, "0bynx7meN9jlSdHQ4-lK_Vzsw-T82UVibnI0nCA"));
		getMapView().setBuiltInZoomControls(true);
		getMapView().setClickable(true);
		getMapView().setSatellite(true);
		getMapView().setEnabled(true);

		this.setContentView(getMapView());

		setStartPoint();
		createOverlay();
		createWalkingPath();
		
		if (dataView.isFrozen()){
			searchNotificationTxt = new TextView(this);
			searchNotificationTxt.setWidth(MixView.getdWindow().getWidth());
			searchNotificationTxt.setPadding(10, 2, 0, 0);			
			searchNotificationTxt.setText(getString(R.string.search_active_1)+" "+ DataSourceList.getDataSourcesStringList() + getString(R.string.search_active_2));
			searchNotificationTxt.setBackgroundColor(Color.DKGRAY);
			searchNotificationTxt.setTextColor(Color.WHITE);

			searchNotificationTxt.setOnTouchListener(this);
			addContentView(searchNotificationTxt, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}
	
	/**
	 * Closes MapView Activity and returns that request to NOT refresh screen by default.
	 * @param boolean do refresh? true or false
	 */
	private void closeMapViewActivity(boolean doRefreshScreen) {
		Intent closeMapView = new Intent();
		closeMapView.putExtra("RefreshScreen", doRefreshScreen);
		setResult(RESULT_OK, closeMapView);
		finish();
	}
	
	/**
	 * Closes MapView Activity and returns that request to NOT refresh screen.
	 * Default value is false
	 */
	private void closeMapViewActivity() {
		closeMapViewActivity(false);
	}

	/* ********* Operators ***********/ 
	public void setStartPoint() {
		Location location = getMixContext().getLocationFinder().getCurrentLocation();
		MapController controller;

		double latitude = location.getLatitude()*1E6;
		double longitude = location.getLongitude()*1E6;

		controller = getMapView().getController();
		startPoint = new GeoPoint((int)latitude, (int)longitude);
		controller.setCenter(startPoint);
		controller.setZoom(15);
	}

	public void createOverlay(){
		setMapOverlays(getMapView().getOverlays());
		OverlayItem item; 
		setDrawable(this.getResources().getDrawable(R.drawable.icon_map));
		MixOverlay mixOverlay = new MixOverlay(this, getDrawable());

		for(Marker marker:markerList) {
			if(marker.isActive()) {
				GeoPoint point = new GeoPoint((int)(marker.getLatitude()*1E6), (int)(marker.getLongitude()*1E6));
				item = new OverlayItem(point, "", "");
				mixOverlay.addOverlay(item);
			}
		}
		//Solved issue 39: only one overlay with all marker instead of one overlay for each marker
		getMapOverlays().add(mixOverlay);

		MixOverlay myOverlay;
		setDrawable(this.getResources().getDrawable(R.drawable.loc_icon));
		myOverlay = new MixOverlay(this, getDrawable());

		item = new OverlayItem(startPoint, "Your Position", "");
		myOverlay.addOverlay(item);
		getMapOverlays().add(myOverlay); 
	}
	
	public void createWalkingPath(){
		if(isPathVisible()){
			mapOverlays=mapView.getOverlays();
			Overlay item = new MixPath(walkingPath);
			mapOverlays.add(item);
		}
	}

	public void createListView(){
		if (dataView.getDataHandler().getMarkerCount() > 0) {
			Intent intent1 = new Intent(MixMap.this, MixListView.class); 
			startActivityForResult(intent1, 42);
		}
		/*if the list is empty*/
		else{
			Toast.makeText( this, R.string.empty_list, Toast.LENGTH_LONG ).show();			
		}
	}
	
	private void togglePath(){
		final String property = "pathVisible";
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		boolean result = settings.getBoolean(property, true);
		editor.putBoolean(property, !result);
		editor.commit();		
	}

	/* ********* Operator - Menu ******/
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int base = Menu.FIRST;
		/*define the first*/

		MenuItem item1 =menu.add(base, base, base, getString(R.string.map_menu_normal_mode)); 
		MenuItem item2 =menu.add(base, base+1, base+1, getString(R.string.map_menu_satellite_mode));
		MenuItem item3 =menu.add(base, base+2, base+2, getString(R.string.map_my_location)); 
		MenuItem item4 =menu.add(base, base+3, base+3, getString(R.string.menu_item_2)); 
		MenuItem item5 =menu.add(base, base+4, base+4, getString(R.string.map_menu_cam_mode)); 
		MenuItem item6 =null;
		if(isPathVisible()){
			item6 =menu.add(base, base+5, base+5, getString(R.string.map_toggle_path_off)); 
		}else{
			item6 =menu.add(base, base+5, base+5, getString(R.string.map_toggle_path_on));
		}
		/*assign icons to the menu items*/
		item1.setIcon(android.R.drawable.ic_menu_gallery);
		item2.setIcon(android.R.drawable.ic_menu_mapmode);
		item3.setIcon(android.R.drawable.ic_menu_mylocation);
		item4.setIcon(android.R.drawable.ic_menu_view);
		item5.setIcon(android.R.drawable.ic_menu_camera);
		item6.setIcon(android.R.drawable.ic_menu_directions);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		/*Satellite View*/
		case 1:
			getMapView().setSatellite(false);
			break;
			/*street View*/
		case 2:		
			getMapView().setSatellite(true);
			break;
			/*go to users location*/
		case 3:
			setStartPoint();
			break;
			/*List View*/
		case 4:
			createListView();
			//finish(); don't close map if list view created
			break;
			/*back to Camera View*/
		case 5:
			closeMapViewActivity();
			break;
		case 6:
			togglePath();
			//refresh:
			startActivity(getIntent()); 
			closeMapViewActivity();
		}
		return true;
	}

	public void startPointMsg(){
		Toast.makeText(getMapContext(), R.string.map_current_location_click, Toast.LENGTH_LONG).show();
	}

	/* ************ Handlers *************/
	
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
			Toast.makeText( this, getString(R.string.search_failed_notification), Toast.LENGTH_LONG ).show();
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
	
	/* ******* Getter and Setters ***********/


	/**
	 * @return the mapOverlays
	 */
	private static List<Overlay> getMapOverlays() {
		return mapOverlays;
	}

	/**
	 * @param mapOverlays the mapOverlays to set
	 */
	private static void setMapOverlays(List<Overlay> mapOverlays) {
		MixMap.mapOverlays = mapOverlays;
	}

	/**
	 * @return the drawable
	 */
	private Drawable getDrawable() {
		return drawable;
	}

	/**
	 * @param drawable the drawable to set
	 */
	private void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}

	/**
	 * @return the mixContext
	 */
	private MixContext getMixContext() {
		return mixContext;
	}

	/**
	 * @param mixContext the mixContext to set
	 */
	private void setMixContext(MixContext mixContext) {
		this.mixContext = mixContext;
	}

	/**
	 * @return the mapView
	 */
	private MapView getMapView() {
		return mapView;
	}

	/**
	 * @param mapView the mapView to set
	 */
	private void setMapView(MapView mapView) {
		this.mapView = mapView;
	}
	
	public void setMarkerList(List<Marker> maList){
		markerList = maList;
	}

	public DataView getDataView(){
		return dataView;
	}

	public List<Overlay> getMapOverlayList(){
		return getMapOverlays();
	}

	public void setMapContext(Context context){
		thisContext= context;
	}

	public Context getMapContext(){
		return thisContext;
	}
	
	/**
	 * Adds a position to the walking route.(This route will be drawn on the map)
	 */
	public static void addWalkingPathPosition(GeoPoint geoPoint){
		walkingPath.add(geoPoint);
	}

	private boolean isPathVisible(){
		final String property = "pathVisible";
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		return settings.getBoolean(property, true);
	}
}

/**
 * Draws Items on the map.
 */
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
					mixMap.getDataView().getContext().getWebContentManager().loadWebPage(newUrl, mixMap.getMapContext());
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

/**
 * Draws a path(line) on the map.
 */
class MixPath extends Overlay{

	private List<GeoPoint> geoPoints;

	public MixPath(List<GeoPoint> geoPoints) {
		Log.i("MapActivity", geoPoints.toString());
		this.geoPoints = geoPoints;
	}

	public void draw(Canvas canvas, MapView mapv, boolean shadow){
        super.draw(canvas, mapv, shadow);

        if(geoPoints.size() <= 0){
        	return;
        }
        
        Projection projection = mapv.getProjection();
        Paint mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(3);

        Path path = new Path();
       
        Point start = new Point();
        projection.toPixels(geoPoints.get(0), start);        
        path.moveTo(start.x, start.y);
               
        for(GeoPoint gp : geoPoints){
        	Point p = new Point();
            projection.toPixels(gp, p);        
            path.lineTo(p.x, p.y);
        }
        
        canvas.drawPath(path, mPaint);
    }
}
