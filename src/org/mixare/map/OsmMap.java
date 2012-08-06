package org.mixare.map;

import java.util.ArrayList;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.mapgenerator.tiledownloader.MapnikTileDownloader;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.GeoPoint;
import org.mixare.MixListView;
import org.mixare.MixView;
import org.mixare.R;
import org.mixare.lib.MixUtils;
import org.mixare.lib.marker.Marker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

/**
 * This class creates a OSM View and handles an overlay of POI's
 * 
 * @author KlemensE
 * 
 */
public class OsmMap extends SherlockOsmMapActivity implements
		OnNavigationListener {

	private MapView mapView;

	// Array which holds the available maps
	private String[] maps;

	// the search keyword
	protected String searchKeyword = "";

	/* Menu ID's */
	// Center my Position
	private static final int MENU_CENTER_POSITION_ID = Menu.FIRST;
	// Go to MixListView
	private static final int MENU_LIST_VIEW = Menu.FIRST + 1;
	// Go to AugmentedView
	private static final int MENU_CAMERA_VIEW = Menu.FIRST + 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Create mapView
		setMapView(new MapView(this));
		getMapView().setClickable(true);
		getMapView().setBuiltInZoomControls(false);
		getMapView().setMapGenerator(new MapnikTileDownloader());

		// Add mapView to View
		setContentView(getMapView());

		// Retrieve the search query
		Intent intent = this.getIntent();
		searchKeyword = intent.getStringExtra("search");

		createOverlay();

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
		getSupportActionBar().setListNavigationCallbacks(list, this);
		getSupportActionBar().setSelectedNavigationItem(getOwnListPosition());
	}

	/* Operators */

	/**
	 * Gets the own position in maps Array
	 * @return The index in the maps array
	 */
	private int getOwnListPosition() {
		for (int i = 0; i < maps.length; i++) {
			if(maps[i].equals(getString(R.string.map_menu_map_osm))) {
				return i;
			}
		}
		
		return 0;
	}
	
	/**
	 * Receives the Location and sets the MapCenter to your position
	 */
	private void setOwnLocationToCenter() {
		Location location = getOwnLocation();
		setCenter(location.getLatitude(), location.getLongitude());
	}

	/**
	 * Closes MapView Activity and returns that request to NOT refresh screen by
	 * default.
	 * 
	 * @param boolean do refresh? true or false
	 */
	private void closeMapViewActivity(boolean doRefreshScreen) {
		Intent closeMapView = new Intent();
		closeMapView.putExtra("RefreshScreen", doRefreshScreen);
		setResult(RESULT_OK, closeMapView);
		finish();
	}

	/**
	 * Sets the center of the map to the specified point
	 * 
	 * @param lat
	 *            The latitude of the point
	 * @param lng
	 *            The longitude of the point
	 */
	private void setCenter(double lat, double lng) {
		getMapView().getController().setCenter(new GeoPoint(lat, lng));
	}

	/**
	 * Sets the center of the map to the specified point with the specified zoom
	 * level
	 * 
	 * @param lat
	 *            The latitude of the point
	 * @param lng
	 *            The longitude of the point
	 * @param zoom
	 *            The zoom level
	 */
	private void setCenterZoom(double lat, double lng, int zoom) {
		getMapView().getController().setZoom(zoom);
		setCenter(lat, lng);
	}

	/**
	 * Sets the Zoomlevel of the Map based on the Radius using
	 * 
	 * @see MixUtils.earthEquatorToZoomLevel(float)
	 */
	private void setZoomLevelBasedOnRadius() {
		float mapZoomLevel = (MixView.getDataView().getRadius() / 2f);
		mapZoomLevel = MixUtils
				.earthEquatorToZoomLevel((mapZoomLevel < 2f) ? 2f
						: mapZoomLevel);
		getMapView().getController().setZoom((int) mapZoomLevel);
	}

	/**
	 * Creates the Overlay and adds the markers
	 */
	private void createOverlay() {
		// create a default marker for the overlay
		Drawable markerLink = getResources().getDrawable(
				R.drawable.icon_map_link);
		markerLink.setBounds(-markerLink.getIntrinsicWidth() / 2,
				-markerLink.getIntrinsicHeight(),
				markerLink.getIntrinsicWidth() / 2, 0);

		// Create marker if no link is specified
		Drawable markerNoLink = this.getResources().getDrawable(
				R.drawable.icon_map_nolink);
		markerNoLink.setBounds(-markerNoLink.getIntrinsicWidth() / 2,
				-markerNoLink.getIntrinsicHeight(),
				markerNoLink.getIntrinsicWidth() / 2, 0);
		
		// create an ItemizedOverlay with the default marker
		OsmOverlay itemizedOverlay = new OsmOverlay(markerLink, this);

		Marker marker;
		int limit = MixView.getDataView().getDataHandler().getMarkerCount();

		for (int i = 0; i < limit; i++) {
			marker = MixView.getDataView().getDataHandler().getMarker(i);
			// if a searchKeyword is specified
			if (searchKeyword != null) {
				// the Keyword is not Empty
				if (!searchKeyword.isEmpty()) {
					// the title of the Marker contains the searchKeyword
					if (marker.getTitle().toLowerCase()
							.indexOf(searchKeyword.toLowerCase().trim()) == -1) {
						marker = null;
						continue;
					}
				}
			}
			// reaches this part of code if no keyword is specified, the keyword
			// is empty or does match

			// Creates a new GeoPoint of the markers Location
			final GeoPoint point = new GeoPoint(marker.getLatitude(),
					marker.getLongitude());
			// Creates a new OverlayItem with the markers Location, the Title
			// and the Url
			final OverlayItem item = new OverlayItem(point, marker.getTitle(),
					marker.getURL());
			// If no URL is specified change the icon
			if (marker.getURL() == null || marker.getURL().isEmpty()) {
				item.setMarker(markerNoLink);
			}
			// Add the item to the overlay
			itemizedOverlay.addItem(item);
		}
		// Adds the overlay to the map
		getMapView().getOverlays().add(itemizedOverlay);

		// Create a overlay for own location
		final ArrayItemizedOverlay myOverlay = new ArrayItemizedOverlay(this
				.getResources().getDrawable(R.drawable.loc_icon));

		Location location = getOwnLocation();
		GeoPoint startPoint = new GeoPoint(location.getLatitude(),
				location.getLongitude());
		final OverlayItem item = new OverlayItem(startPoint, "Your Position",
				"");
		myOverlay.addItem(item);
		getMapView().getOverlays().add(myOverlay);
	}

	/**
	 * Gets fired when the selected item of the ListNavigation changes. This
	 * method changes to the specified map. (Google Map/OSM)
	 */
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (maps[itemPosition].equals(getString(R.string.map_menu_map_google))) {
			MixMap.changeMap(MixMap.MAPS.GOOGLE);
			Intent intent = new Intent(this, GoogleMap.class);
			startActivity(intent);
			finish();
		}
		return true;
	}

	/* ********* Operator - Menu ***** */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MENU_CENTER_POSITION_ID, MENU_CENTER_POSITION_ID, Menu.NONE,
				"Center").setIcon(android.R.drawable.ic_menu_mylocation)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		SubMenu subMenu1 = menu.addSubMenu("More");

		subMenu1.add(MENU_LIST_VIEW, MENU_LIST_VIEW, Menu.NONE,
				getString(R.string.menu_item_3))
				.setIcon(android.R.drawable.ic_menu_view);

		subMenu1.add(MENU_CAMERA_VIEW, MENU_CAMERA_VIEW, Menu.NONE,
				getString(R.string.map_menu_cam_mode))
				.setIcon(android.R.drawable.ic_menu_camera);

		MenuItem subMenu1Item = subMenu1.getItem();
		subMenu1Item.setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_dark);
		subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/* Actionbar icon pressed */
		case android.R.id.home:
			finish();
			break;
		/* go to users location */
		case MENU_CENTER_POSITION_ID:
			setOwnLocationToCenter();
			break;
		/* List View */
		case MENU_LIST_VIEW:
			if (MixView.getDataView().getDataHandler().getMarkerCount() > 0) {
				Intent intent1 = new Intent(this, MixListView.class);
				intent1.setAction(Intent.ACTION_VIEW);
				startActivityForResult(intent1, 42);
			}
			/* if the list is empty */
			else {
				MixView.getDataView().getContext().getNotificationManager()
						.addNotification(getString(R.string.empty_list));
			}
			break;
		/* back to Camera View */
		case MENU_CAMERA_VIEW:
			closeMapViewActivity(false);
			break;
		default:
			break;// do nothing
		}

		return true;
	}

	/* Getter and Setter */

	/**
	 * Returns the Point of the current Own Location
	 * 
	 * @return My current Location
	 */
	private Location getOwnLocation() {
		return MixView.getDataView().getContext().getLocationFinder()
				.getCurrentLocation();
	}

	/**
	 * The mapView which is shown
	 * 
	 * @return
	 */
	public MapView getMapView() {
		return mapView;
	}

	/**
	 * Sets the mapView to show
	 * 
	 * @param mapView
	 *            The mapView to show
	 */
	public void setMapView(MapView mapView) {
		this.mapView = mapView;
	}
}

/**
 * Manages the Overlays
 * 
 * @author KlemensE
 * 
 */
class OsmOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
	private OsmMap osmMap;

	public OsmOverlay(Drawable defaultMarker, OsmMap osmMap) {
		super(boundCenterBottom(defaultMarker));
		this.osmMap = osmMap;
	}

	public void addItem(OverlayItem item) {
		this.overlayItems.add(item);
	}

	@Override
	protected OverlayItem createItem(int index) {
		return overlayItems.get(index);
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
				MixView.getDataView().getContext().getWebContentManager()
						.loadWebPage(newUrl, osmMap);
			} else {
				OverlayItem item = overlayItems.get(index);
				AlertDialog.Builder dialog = new AlertDialog.Builder(osmMap);
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
}
