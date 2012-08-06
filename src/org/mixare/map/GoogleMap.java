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

package org.mixare.map;

import java.util.ArrayList;
import java.util.List;

import org.mixare.DataView;
import org.mixare.MixListView;
import org.mixare.MixView;
import org.mixare.R;
import org.mixare.lib.MixUtils;
import org.mixare.lib.marker.Marker;

import android.app.AlertDialog;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * This class creates the map view and its overlay. It also adds an overlay with
 * the markers to the map.
 */
public class GoogleMap extends SherlockGoogleMapActivity implements
		OnTouchListener, ActionBar.OnNavigationListener {

	private static List<Overlay> mapOverlays;
	private Drawable drawable;

	// private static List<Marker> markerList;
	private static DataView dataView;
	private static List<GeoPoint> walkingPath = new ArrayList<GeoPoint>();

	public static final String PREFS_NAME = "MixMapPrefs";

	// private MixContext mixContext;
	private MapView mapView;

	// static MixMap map;
	private static Context thisContext;
	private static TextView searchNotificationTxt;
	// public static List<Marker> originalMarkerList;

	// the search keyword
	protected String searchKeyword = "";

	// Array which holds the available maps
	private String[] maps;

	/* Menu ID's */
	// Center my Position
	private static final int MENU_CENTER_POSITION_ID = Menu.FIRST;
	// Whether to display Satellite or Map
	private static final int MENU_CHANGE_MODE_ID = Menu.FIRST + 1;
	// Go to MixListView
	private static final int MENU_LIST_VIEW = Menu.FIRST + 2;
	// Go to AugmentedView
	private static final int MENU_CAMERA_VIEW = Menu.FIRST + 3;
	// Toggle show Path
	private static final int MENU_SHOW_PATH = Menu.FIRST + 4;

	/**
	 * Needs to be overridden because it's abstract
	 */
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * First Launched Method onCreate() Does: - initiate View - Retrieve markers
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataView = MixView.getDataView();

		setMapContext(this);
		setMapView(new MapView(this, "0zMCXwuwyQLKoOtdQc8VelAT_ipCTDn-h8R-p6A"));
		getMapView().setBuiltInZoomControls(true);
		getMapView().setClickable(true);
		getMapView().setSatellite(true);
		getMapView().setEnabled(true);

		this.setContentView(getMapView());

		Intent intent = this.getIntent();
		searchKeyword = intent.getStringExtra("search");

		createOverlay();
		createWalkingPath();

		// Set center of the Map to your position or a Position out of the
		// IntentExtras
		if (intent.getBooleanExtra("center", false)) {
			setCenterZoom(intent.getDoubleExtra("latitude", 0.0),
					intent.getDoubleExtra("longitude", 0.0), 16);
		} else {
			setOwnLocationToCenter();
			setZoomLevelBasedOnRadius();
		}

		maps = getResources().getStringArray(R.array.maps);

		Context context = getSupportActionBar().getThemedContext();
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(
				context, R.array.maps, R.layout.sherlock_spinner_item);
		list.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setSelectedNavigationItem(getOwnListPosition());
		getSupportActionBar().setListNavigationCallbacks(list, this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// if (dataView.isFrozen()) {
		// searchNotificationTxt = new TextView(this);
		// searchNotificationTxt.setWidth(MixView.getdWindow().getWidth());
		// searchNotificationTxt.setPadding(10, 2, 0, 0);
		// searchNotificationTxt.setText(getString(R.string.search_active_1)
		// + " " + DataSourceList.getDataSourcesStringList()
		// + getString(R.string.search_active_2));
		// searchNotificationTxt.setBackgroundColor(Color.DKGRAY);
		// searchNotificationTxt.setTextColor(Color.WHITE);
		// searchNotificationTxt.setOnTouchListener(this);
		// addContentView(searchNotificationTxt, new LayoutParams(
		// LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		// }
	}

	/**
	 * Closes MapView Activity and returns to MixView with or without the
	 * request to refresh the screen.
	 * 
	 * @param boolean True to refresh screen false not to
	 */
	private void closeMapViewActivity(boolean doRefreshScreen) {
		Intent closeMapView = new Intent();
		closeMapView.putExtra("RefreshScreen", doRefreshScreen);
		setResult(RESULT_OK, closeMapView);
		finish();
	}

	/* ********* Operators ********** */
	/**
	 * Gets the own position in maps Array
	 * @return The index in the maps array
	 */
	private int getOwnListPosition() {
		for (int i = 0; i < maps.length; i++) {
			if(maps[i].equals(getString(R.string.map_menu_map_google))) {
				return i;
			}
		}
		
		return 0;
	}
	
	private void setCenter(double latitude, double longitude) {
		GeoPoint centerPoint = new GeoPoint((int) (latitude * 1E6),
				(int) (longitude * 1E6));
		getMapView().getController().setCenter(centerPoint);
	}

	private void setCenterZoom(double lat, double lng, int zoom) {
		getMapView().getController().setZoom(zoom);
		setCenter(lat, lng);
	}

	private void setZoomLevelBasedOnRadius() {
		float mapZoomLevel = (getDataView().getRadius() / 2f);
		mapZoomLevel = MixUtils
				.earthEquatorToZoomLevel((mapZoomLevel < 2f) ? 2f
						: mapZoomLevel);
		getMapView().getController().setZoom((int) mapZoomLevel);
	}

	private void setOwnLocationToCenter() {
		Location location = getOwnLocation();
		setCenter(location.getLatitude(), location.getLongitude());
	}

	private void createOverlay() {
		setMapOverlays(getMapView().getOverlays());
		setDrawable(this.getResources().getDrawable(R.drawable.icon_map_link));
		MixOverlay mixOverlay = new MixOverlay(this, getDrawable());

		Marker marker;
		int limit = dataView.getDataHandler().getMarkerCount();
		for (int i = 0; i < limit; i++) {
			marker = dataView.getDataHandler().getMarker(i);
			if (searchKeyword != null) {
				if (!searchKeyword.isEmpty()) {
					if (marker.getTitle().toLowerCase()
							.indexOf(searchKeyword.toLowerCase().trim()) == -1) {
						marker = null;
						continue;
					}
				}
			}
			final GeoPoint point = new GeoPoint(
					(int) (marker.getLatitude() * 1E6),
					(int) (marker.getLongitude() * 1E6));
			final OverlayItem item = new OverlayItem(point, marker.getTitle(),
					marker.getURL());
			if (marker.getURL() == null || marker.getURL().isEmpty()) {
				Drawable dw = this.getResources().getDrawable(
						R.drawable.icon_map_nolink);
				dw.setBounds(-dw.getIntrinsicWidth() / 2,
						-dw.getIntrinsicHeight(), dw.getIntrinsicWidth() / 2, 0);
				item.setMarker(dw);
			}
			mixOverlay.addOverlay(item);
		}

		// Solved issue 39: only one overlay with all marker instead of one
		// overlay for each marker
		getMapOverlays().add(mixOverlay);

		setDrawable(this.getResources().getDrawable(R.drawable.loc_icon));
		final MixOverlay myOverlay = new MixOverlay(this, getDrawable());

		Location location = getOwnLocation();
		GeoPoint startPoint = new GeoPoint(
				(int) (location.getLatitude() * 1E6),
				(int) (location.getLongitude() * 1E6));
		final OverlayItem item = new OverlayItem(startPoint, "Your Position",
				"");
		myOverlay.addOverlay(item);
		getMapOverlays().add(myOverlay);
	}

	private void createWalkingPath() {
		if (isPathVisible()) {
			mapOverlays = getMapView().getOverlays();
			final Overlay item = new MixPath(walkingPath);
			mapOverlays.add(item);// TODO user specified paths
		}
	}

	private void createListView() {
		if (dataView.getDataHandler().getMarkerCount() > 0) {
			Intent intent1 = new Intent(this, MixListView.class);
			intent1.setAction(Intent.ACTION_VIEW);
			startActivityForResult(intent1, 42);// TODO receive result if any!
		}
		/* if the list is empty */
		else {
			getDataView().getContext().getNotificationManager()
					.addNotification(getString(R.string.empty_list));
		}
	}

	private void togglePath() {
		final String property = "pathVisible";
		final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		boolean result = settings.getBoolean(property, true);
		editor.putBoolean(property, !result);
		editor.commit();
	}

	/* ********* Operator - Menu ***** */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/* define the first */
		int base = Menu.FIRST;

		// TODO: Get Strings out of values
		menu.add(MENU_CENTER_POSITION_ID, MENU_CENTER_POSITION_ID, Menu.NONE,
				"Center").setIcon(android.R.drawable.ic_menu_mylocation)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		SubMenu subMenu1 = menu.addSubMenu("More");

		if (getMapView().isSatellite()) {
			subMenu1.add(MENU_CHANGE_MODE_ID, MENU_CHANGE_MODE_ID, Menu.NONE,
					"Change to Map");
		} else {
			subMenu1.add(MENU_CHANGE_MODE_ID, MENU_CHANGE_MODE_ID, Menu.NONE,
					"Change to Satellite");
		}

		subMenu1.add(MENU_LIST_VIEW, MENU_LIST_VIEW, Menu.NONE, "ListView");

		subMenu1.add(MENU_CAMERA_VIEW, MENU_CAMERA_VIEW, Menu.NONE,
				getString(R.string.map_menu_cam_mode));

		if (isPathVisible()) {
			subMenu1.add(MENU_SHOW_PATH, MENU_SHOW_PATH, Menu.NONE,
					getString(R.string.map_toggle_path_off));
		} else {
			subMenu1.add(base, base + 4, base + 4,
					getString(R.string.map_toggle_path_on));
		}

		MenuItem subMenu1Item = subMenu1.getItem();
		subMenu1Item.setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_dark);
		subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Actionbar icon pressed
		case android.R.id.home:
			finish();
			break;
		/* MapMode */
		case MENU_CHANGE_MODE_ID:
			if (getMapView().isSatellite()) {
				getMapView().setSatellite(false);
			} else {
				getMapView().setSatellite(true);
			}
			getSherlock().dispatchInvalidateOptionsMenu();
			break;
		/* go to users location */
		case MENU_CENTER_POSITION_ID:
			setOwnLocationToCenter();
			break;
		/* List View */
		case MENU_LIST_VIEW:
			createListView();
			// finish(); don't close map if list view created
			break;
		/* back to Camera View */
		case MENU_SHOW_PATH:
			togglePath();
			// refresh:
			startActivity(getIntent()); // what Activity are we launching?
			closeMapViewActivity(false);
			break;
		case MENU_CAMERA_VIEW:
			closeMapViewActivity(false);
			break;
		default:
			break;// do nothing

		}

		return true;
	}

	/* ************ Handlers ************ */

	/**
	 * Gets fired when the selected item of the ListNavigation changes. This
	 * method changes to the specified map. (Google Map/OSM)
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (maps[itemPosition].equals(getString(R.string.map_menu_map_osm))) {
			MixMap.changeMap(MixMap.MAPS.OSM);
			Intent intent = new Intent(this, OsmMap.class);
			startActivity(intent);
			finish();
		}
		return true;
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// String query = intent.getStringExtra(SearchManager.QUERY);
			// doMixSearch(query);
			intent.setClass(this, MixListView.class);
			startActivity(intent);
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	/*
	 * TODO Fix onTouch function MixMap (non-Javadoc)
	 * 
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
	 * android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		dataView.setFrozen(false);
		// dataView.getDataHandler().setMarkerList(originalMarkerList);

		searchNotificationTxt.setVisibility(View.INVISIBLE);
		searchNotificationTxt = null;
		finish();
		Intent intent1 = new Intent(this, GoogleMap.class);
		startActivityForResult(intent1, 42);

		return false;
	}

	/* ******* Getter and Setters ********** */

	/**
	 * @return the Current Location of the user
	 */
	private Location getOwnLocation() {
		return getDataView().getContext().getLocationFinder()
				.getCurrentLocation();
	}

	/**
	 * @return the mapOverlays
	 */
	private static List<Overlay> getMapOverlays() {
		return mapOverlays;
	}

	/**
	 * @param mapOverlays
	 *            the mapOverlays to set
	 */
	private static void setMapOverlays(List<Overlay> mapOverlays) {
		GoogleMap.mapOverlays = mapOverlays;
	}

	/**
	 * @return the Drawable
	 */
	private Drawable getDrawable() {
		return drawable;
	}

	/**
	 * @param drawable
	 *            the Drawable to set
	 */
	private void setDrawable(Drawable drawable) {
		this.drawable = drawable;
	}

	/**
	 * @return MapView the mapView
	 */
	private MapView getMapView() {
		return mapView;
	}

	/**
	 * @param mapView
	 *            the mapView to set
	 */
	private void setMapView(MapView mapView) {
		this.mapView = mapView;
	}

	/**
	 * Returns current DataView
	 * 
	 * @return DataView current DataView
	 */
	public DataView getDataView() {
		return dataView;
	}

	public List<Overlay> getMapOverlayList() {
		return getMapOverlays();
	}

	public void setMapContext(Context context) {
		thisContext = context;
	}

	public Context getMapContext() {
		return thisContext;
	}

	/**
	 * Adds a position to the walking route.(This route will be drawn on the
	 * map)
	 */
	public static void addWalkingPathPosition(GeoPoint geoPoint) {
		walkingPath.add(geoPoint);
	}

	/**
	 * Checks stored user preference
	 * 
	 * @return boolean false if specified, true otherwise (default)
	 */
	private boolean isPathVisible() {
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
	private GoogleMap mixMap;

	public MixOverlay(GoogleMap mixMap, Drawable marker) {
		super(boundCenterBottom(marker));
		// need to call populate here. See
		// http://code.google.com/p/android/issues/detail?id=2035
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
	protected boolean onTap(int index) {
		String url = overlayItems.get(index).getSnippet();

		try {
			if (url != null && url.startsWith("webpage")) {
				String newUrl = MixUtils.parseAction(url);
				// Log.d("test", "open: " + newUrl);
				mixMap.getDataView().getContext().getWebContentManager()
						.loadWebPage(newUrl, mixMap.getMapContext());
			} else {
				OverlayItem item = overlayItems.get(index);
				AlertDialog.Builder dialog = new AlertDialog.Builder(mixMap);
				dialog.setTitle(item.getTitle());
				dialog.setMessage(item.getSnippet());
				dialog.show();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
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
class MixPath extends Overlay {

	private List<GeoPoint> geoPoints;

	public MixPath(List<GeoPoint> geoPoints) {
		Log.i("MapActivity", geoPoints.toString());
		this.geoPoints = geoPoints;
	}

	public void draw(Canvas canvas, MapView mapv, boolean shadow) {
		super.draw(canvas, mapv, shadow);

		if (geoPoints.size() <= 0) {
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

		final Path usrPath = new Path();

		Point start = new Point();
		projection.toPixels(geoPoints.get(0), start);
		usrPath.moveTo(start.x, start.y);

		for (GeoPoint gp : geoPoints) {
			Point p = new Point();
			projection.toPixels(gp, p);
			usrPath.lineTo(p.x, p.y);
		}

		canvas.drawPath(usrPath, mPaint);
	}
}