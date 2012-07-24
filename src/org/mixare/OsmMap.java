package org.mixare;

import java.util.ArrayList;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.mapgenerator.tiledownloader.MapnikTileDownloader;
import org.mapsforge.android.maps.overlay.ArrayItemizedOverlay;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.model.GeoPoint;
import org.mixare.lib.MixUtils;
import org.mixare.lib.marker.Marker;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * This class creates a OSM View and handles an overlay of POI's
 * 
 * @author KlemensE
 * 
 */
public class OsmMap extends MapActivity {

	private MapView mapView;

	// the search keyword
	protected String searchKeyword = "";

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
	}

	/* Operators */

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
		Drawable defaultMarker = getResources().getDrawable(
				R.drawable.icon_map_link);
		defaultMarker.setBounds(-defaultMarker.getIntrinsicWidth() / 2,
				-defaultMarker.getIntrinsicHeight(),
				defaultMarker.getIntrinsicWidth() / 2, 0);

		// create an ItemizedOverlay with the default marker
		OsmOverlay itemizedOverlay = new OsmOverlay(defaultMarker, this);

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
				Drawable dw = this.getResources().getDrawable(
						R.drawable.icon_map_nolink);
				dw.setBounds(-dw.getIntrinsicWidth() / 2,
						-dw.getIntrinsicHeight(), dw.getIntrinsicWidth() / 2, 0);
				item.setMarker(dw);
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

	/* ********* Operator - Menu ***** */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		/* define the first */
		int base = Menu.FIRST;

		/* Google Maps */
		final MenuItem item1 = menu.add(base, base, base,
				getString(R.string.map_menu_map_google));
		/* My Location */
		final MenuItem item2 = menu.add(base, base + 1, base + 1,
				getString(R.string.map_my_location));
		/* List View */
		final MenuItem item3 = menu.add(base, base + 2, base + 2,
				getString(R.string.menu_item_3));
		/* Camera */
		final MenuItem item4 = menu.add(base, base + 3, base + 3,
				getString(R.string.map_menu_cam_mode));

		/* assign icons to the menu items */
		item1.setIcon(android.R.drawable.ic_menu_mapmode);
		item2.setIcon(android.R.drawable.ic_menu_mylocation);
		item3.setIcon(android.R.drawable.ic_menu_view);
		item4.setIcon(android.R.drawable.ic_menu_camera);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/* Google Maps */
		case 1:
			MixMap.changeMap(MixMap.MAPS.GOOGLE);
			Intent intent = new Intent(this, GoogleMap.class);
			startActivity(intent);
			finish();
			break;
		/* go to users location */
		case 2:
			setOwnLocationToCenter();
			break;
		/* List View */
		case 4:
			if (MixView.getDataView().getDataHandler().getMarkerCount() > 0) {
				Intent intent1 = new Intent(this, MarkerListView.class);
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
		case 6:
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
