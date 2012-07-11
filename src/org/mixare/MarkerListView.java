package org.mixare;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.mixare.data.DataHandler;
import org.mixare.lib.MixUtils;
import org.mixare.lib.marker.Marker;
import org.mixare.sectionedlist.SectionListAdapter;
import org.mixare.sectionedlist.SectionListView;
import org.mixare.sectionedlist.SectionedListItem;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MarkerListView extends Activity {
	
	private DataView dataView;
	private Context ctx;
	private StandardArrayAdapter arrayAdapter;
	private SectionListAdapter sectionAdapter;
	private SectionListView listView;
	/* The sections for the list in meter */
	private int[] sections = { 250, 500, 1000, 1500, 3500, 5000, 10000, 20000,
			50000 };
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ctx = this;
		dataView = MixView.getDataView();
		
		List<SectionedListItem> list;
		if (Intent.ACTION_SEARCH.equals(this.getIntent().getAction())) {
			String query = this.getIntent().getStringExtra(SearchManager.QUERY);
			list = search(query);
		} else {
			list = createList();
		}

		setContentView(R.layout.list);
		arrayAdapter = new StandardArrayAdapter(this, R.id.list_text, list);
		sectionAdapter = new SectionListAdapter(getLayoutInflater(),
				arrayAdapter);
		sectionAdapter.setOnItemClickListener(itemClickListener);
		listView = (SectionListView) findViewById(R.id.section_list_view);
		listView.setOnItemClickListener(sectionAdapter);
		listView.setAdapter(sectionAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int base = Menu.FIRST;

		/* define menu items */
		MenuItem item1 = menu.add(base, base, base,
				getString(R.string.menu_item_3));
		MenuItem item2 = menu.add(base, base + 1, base + 1,
				getString(R.string.map_menu_cam_mode));
		/* assign icons to the menu items */
		item1.setIcon(android.R.drawable.ic_menu_mapmode);
		item2.setIcon(android.R.drawable.ic_menu_camera);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/* Map View */
		case 1:
			Intent intent2 = new Intent(MarkerListView.this, MixMap.class);
			startActivityForResult(intent2, 20);
			finish();
			break;
		/* back to Camera View */
		case 2:
			finish();
			break;
		}
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			intent.setClass(this, MarkerListView.class);
			startActivity(intent);
			finish(); //TODO reoginize launching
		}
	}
	
	/**
	 * Formats the distance to m or km
	 * 
	 * Example: distance = 600
	 * 			Method returns 600m
	 * 			distance = 6000
	 * 			Method returns 6km
	 * 			distance = 6500
	 * 			Method returns 6.5km
	 * 
	 * @param dist
	 * 			The distance to a point
	 * @return The formated distance
	 */
	private static String getDistString(float dist) {
		String distance = "";
		if ((dist / 1000) >= 1) {
			Double val = (double) (dist / 1000);
			String valasdf = val.toString();
			if (Double.parseDouble(val.toString().substring(valasdf.indexOf('.'))) == 0.0) {
				distance = val.intValue() + "km";
			} else {
				distance = val + "km";
			}
		} else {
			distance =  (int)dist + "m";
		}
		return distance;
	}

	/**
	 * Create the string that represents a section
	 * 
	 * Example: distance = 600
	 * 			Method returns 500m - 1km
	 * 			(if these are the nearest sections)
	 * 			distance = 200
	 * 			Method returns < 250
	 * 			(if 250 is the smallest section)
	 * 			distance = 60000
	 * 			Method returns > 50km
	 * 			(if 50000 is the biggest section)
	 * 
	 * @param distance
	 * 			the distance from the marker to your location
	 * @return A string that indicates how far you are away from a point
	 * 
	 */
	private String getSection(double distance) {
		String section = "";
		for (int i = 0; i < sections.length; i++) {
			if (distance <= sections[i]) {
				if (i == 0) {
					section = "< " + getDistString(sections[i]);
					break;
				} else if (distance > sections[i - 1]) {
					section = getDistString(sections[i - 1]) + " - "
							+ getDistString(sections[i]);
					break;
				}
			} else {
				section = "> " + getDistString(sections[i]);
			}
		}
		return section;
	}

	/**
	 * Searches the list for markers that match the query
	 * @param query
	 * 			The string that should be looked for
	 * @return A list that contains only markers that match the query or if no match was found return the normal list
	 */
	private List<SectionedListItem> search(String query) {
		DataHandler jLayer = dataView.getDataHandler();
		List<SectionedListItem> list = new ArrayList<SectionedListItem>();
		for(int i = 0; i < jLayer.getMarkerCount();i++){
			Marker ma = jLayer.getMarker(i);

			if (ma.getTitle().toLowerCase().indexOf(query.toLowerCase().trim()) != -1) {
				SectionedListItem item = new SectionedListItem(ma, getSection(ma.getDistance()));
				list.add(item);
			}
		}
		if (list.size() == 0) {
			Log.d("test", "MarkerListView: Search nothing found");
			list = createList();
			dataView.getContext().getNotificationManager().
			addNotification(getString(R.string.search_failed_notification));
		}
		return list;
	}
	
	/**
	 * Creats the list of markers and adds them to the appropriate section
	 * @return A list of SectionedListItem
	 */
	private List<SectionedListItem> createList() {
		List<SectionedListItem> list = new ArrayList<SectionedListItem>();
		DataHandler dataHandler = dataView.getDataHandler();

		for (int i = 0; i < dataHandler.getMarkerCount(); i++) {
			Marker marker = dataHandler.getMarker(i);
			SectionedListItem item = new SectionedListItem(marker,
					getSection(marker.getDistance()));
			if (!list.contains(item)) {
				list.add(item);
			}
		}

		return list;
	}

	/**
	 * The clickListener that handles the click on a list item and if a URL is specified open a webView
	 */
	OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Marker marker = (Marker) arrayAdapter.getItem(position).getItem();

			String selectedURL = marker.getURL();
			if (selectedURL != null) {
				try {
					if (selectedURL.startsWith("webpage")) {
						String newUrl = MixUtils.parseAction(selectedURL);
						dataView.getContext().getWebContentManager()
								.loadWebPage(newUrl, ctx);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	private class StandardArrayAdapter extends ArrayAdapter<SectionedListItem> {
		List<SectionedListItem> items;

		public SectionedListItem getItem(int position) {
			return items.get(position);
		}

		public StandardArrayAdapter(Context context, int textViewResourceId,
				List<SectionedListItem> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				final LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.marker_list, null);

				holder = new ViewHolder();
				holder.title = (TextView) convertView
						.findViewById(R.id.markerTitle);
				holder.dist = (TextView) convertView
						.findViewById(R.id.markerDist);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			SpannableString spannableString;
			Marker marker = (Marker) items.get(position).getItem();

			if (marker.getURL() != null) {
				spannableString = new SpannableString(marker.getTitle());
				spannableString.setSpan(new UnderlineSpan(), 0,
						spannableString.length(), 0);
			} else {
				spannableString = new SpannableString(marker.getTitle());
			}
			holder.title.setText(spannableString);
			holder.dist.setText(Math.round(marker.getDistance()) + "m");

			return convertView;
		}

		private class ViewHolder {
			TextView title;
			TextView dist;
		}
	}
}