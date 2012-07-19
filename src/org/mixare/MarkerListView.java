/*
 * Copyright (C) 2012- Peer internet solutions
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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class MarkerListView extends Activity {
	
	private DataView dataView;
	private Context ctx;
	private StandardArrayAdapter arrayAdapter;
	private SectionListAdapter sectionAdapter;
	private SectionListView listView;
	private String search = "";
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
			search = query;
		} else {
			list = createList();
			search = "";
		}

		setContentView(R.layout.list);
		arrayAdapter = new StandardArrayAdapter(this, R.id.list_text, list);
		sectionAdapter = new SectionListAdapter(getLayoutInflater(),
				arrayAdapter);
		listView = (SectionListView) findViewById(R.id.section_list_view);
		listView.setAdapter(sectionAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int base = Menu.FIRST;

		/* define menu items */
		MenuItem item1 = menu.add(base, base, base,
				getString(R.string.menu_item_4));
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
			intent2.putExtra("search", search);
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
					section = "< " + MixUtils.formatDist(sections[i]);
					break;
				} else if (distance > sections[i - 1]) {
					section = MixUtils.formatDist(sections[i - 1]) + " - "
							+ MixUtils.formatDist(sections[i]);
					break;
				}
			} else {
				section = "> " + MixUtils.formatDist(sections[i]);
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
		Marker ma;
		MarkerInfo markerInfo;
		SectionedListItem item;
		List<SectionedListItem> list = new ArrayList<SectionedListItem>();
		for(int i = 0; i < jLayer.getMarkerCount();i++){
			ma = jLayer.getMarker(i);

			if (ma.getTitle().toLowerCase().indexOf(query.toLowerCase().trim()) != -1) {
				markerInfo = new MarkerInfo(ma.getTitle(), ma.getURL(), ma.getDistance(), ma.getLatitude(), ma.getLongitude());
				item = new SectionedListItem(markerInfo, getSection(ma.getDistance()));
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
		Marker ma;
		MarkerInfo markerInfo;
		for (int i = 0; i < dataHandler.getMarkerCount(); i++) {
			ma = dataHandler.getMarker(i);
			markerInfo = new MarkerInfo(ma.getTitle(), ma.getURL(), ma.getDistance(), ma.getLatitude(), ma.getLongitude());
			SectionedListItem item = new SectionedListItem(markerInfo,
					getSection(markerInfo.getDist()));
			if (!list.contains(item)) {
				list.add(item);
			}
		}

		return list;
	}

	/**
	 * Save some memory. We are only interested in these 3 things.
	 * @author KlemensE
	 *
	 */
	private class MarkerInfo {
		private String title;
		private String url;
		private Double dist;
		private Double latitude;
		private Double longitude;
		/**
		 * Constructor
		 * @param title The title of the marker
		 * @param url The URL where the marker points to
		 * @param dist The distance to my position
		 */
		public MarkerInfo(String title, String url, Double dist, Double latitude, Double longitude) {
			setTitle(title);
			setUrl(url);
			setDist(dist);
			setLatitude(latitude);
			setLongitude(longitude);
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public Double getDist() {
			return dist;
		}
		public void setDist(Double dist) {
			this.dist = dist;
		}
		public Double getLatitude() {
			return latitude;
		}
		public void setLatitude(Double latitude) {
			this.latitude = latitude;
		}
		public Double getLongitude() {
			return longitude;
		}
		public void setLongitude(Double longitude) {
			this.longitude = longitude;
		}
	}
		
	private class StandardArrayAdapter extends ArrayAdapter<SectionedListItem>{
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
				holder.centerMap = (ImageButton) convertView.findViewById(R.id.centerMap);
				holder.showWebView = (ImageButton) convertView.findViewById(R.id.showWebView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			SpannableString spannableString;
			MarkerInfo markerInfo = (MarkerInfo) items.get(position).getItem();

			if (markerInfo.getUrl() != null) {
				spannableString = new SpannableString(markerInfo.getTitle());
				spannableString.setSpan(new UnderlineSpan(), 0,
						spannableString.length(), 0);
				holder.showWebView.setTag(position);
				holder.showWebView.setOnClickListener(onClickListenerWebView);
				holder.showWebView.setVisibility(View.VISIBLE);
			} else {
				spannableString = new SpannableString(markerInfo.getTitle());
				holder.showWebView.setTag(position);
				holder.showWebView.setOnClickListener(null);
				holder.showWebView.setVisibility(View.GONE);
			}
			holder.centerMap.setTag(position);
			holder.centerMap.setOnClickListener(onClickListenerCenterMap);
			holder.title.setText(spannableString);
			holder.dist.setText(Math.round(markerInfo.getDist()) + "m");

			
			return convertView;
		}

		private class ViewHolder {
			TextView title;
			TextView dist;
			ImageButton centerMap;
			ImageButton showWebView;
		}

		OnClickListener onClickListenerCenterMap = new OnClickListener() {
			@Override
			public void onClick(View v) {
				MarkerInfo markerInfo = (MarkerInfo) items.get((Integer) v.getTag()).getItem();
				
				Intent startMap = new Intent(MarkerListView.this, MixMap.class);
				startMap.putExtra("center", true);
				startMap.putExtra("latitude", markerInfo.getLatitude());
				startMap.putExtra("longitude", markerInfo.getLongitude());
				startActivityForResult(startMap, 76);
			}
		};
		
		OnClickListener onClickListenerWebView = new OnClickListener() {
			@Override
			public void onClick(View v) {
				MarkerInfo markerInfo = (MarkerInfo) items.get((Integer) v.getTag()).getItem();
				
				String selectedURL = markerInfo.getUrl();
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
	}
}