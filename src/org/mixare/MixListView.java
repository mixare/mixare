package org.mixare;

import java.util.ArrayList;
import java.util.List;

import org.mixare.data.DataHandler;
import org.mixare.lib.MixUtils;
import org.mixare.lib.marker.Marker;
import org.mixare.map.MixMap;
import org.mixare.sectionedlist.Item;
import org.mixare.sectionedlist.SectionItem;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MixListView extends SherlockActivity {

	private Context ctx;
	private DataView dataView;
	private SectionAdapter sectionAdapter;
	private ListView listView;
	private static final int MENU_MAPVIEW_ID = 0;
	private static final int MENU_SEARCH_ID = 1;
	private EditText editText;
	private MenuItem search;

	/* The sections for the list in meter */
	private int[] sections = { 250, 500, 1000, 1500, 3500, 5000, 10000, 20000,
			50000 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.ctx = this;
		this.dataView = MixView.getDataView();

		editText = new EditText(this);

		List<Item> list;
		if (Intent.ACTION_SEARCH.equals(this.getIntent().getAction())) {
			String query = this.getIntent().getStringExtra(SearchManager.QUERY);
			list = createList(query);
			editText.setText(query);
		} else {
			list = createList();
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.list);
		sectionAdapter = new SectionAdapter(this, 0, list);
		listView = (ListView) findViewById(R.id.section_list_view);
		listView.setAdapter(sectionAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MENU_MAPVIEW_ID, MENU_MAPVIEW_ID, MENU_MAPVIEW_ID, "MapView")
				.setIcon(android.R.drawable.ic_menu_mapmode)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_IF_ROOM
								| MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		editText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		editText.setHint(getString(R.string.list_view_search_hint));
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (hasFocus) {
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

				} else {
					imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
				}
			}
		});
		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable edit) {
				String query = edit.toString();
				sectionAdapter.changeList(createList(query));
			}
		});

		search = menu.add(MENU_SEARCH_ID, MENU_SEARCH_ID, MENU_SEARCH_ID,
				getString(R.string.list_view_search_hint));
		search.setIcon(android.R.drawable.ic_menu_search);
		search.setActionView(editText);
		search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
				| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case MENU_MAPVIEW_ID:
			Intent map = new Intent(MixListView.this, MixMap.class);
			startActivity(map);
			break;
		case MENU_SEARCH_ID:
			editText.requestFocus();
		}
		return true;
	}

	@Override
	public boolean onSearchRequested() {
		search.expandActionView();
		editText.requestFocus();
		return false;
	}

	/**
	 * Creates the list to display without filtering. Same as createList(null).
	 * 
	 * @return The list containing Item's to display
	 */
	private List<Item> createList() {
		return createList(null);
	}

	/**
	 * Creates the list to display
	 * 
	 * @param query
	 *            The query to look for or null not to filter
	 * @return The list containing Item's to display
	 */
	private List<Item> createList(String query) {
		List<Item> list = new ArrayList<Item>();
		DataHandler dataHandler = dataView.getDataHandler();
		String lastSection = "";
		int lastSectionId = -1;
		int markerCount = 0;
		int sectionCount = 0;

		for (int i = 0; i < dataHandler.getMarkerCount(); i++) {
			Marker marker = dataHandler.getMarker(i);

			// Check the query
			if (query != null) {
				if (marker.getTitle().toLowerCase()
						.indexOf(query.toLowerCase().trim()) < 0) {
					continue;
				}
			}

			// Create MarkerInfo and the section string
			MarkerInfo markerInfo = new MarkerInfo(marker.getTitle(),
					marker.getURL(), marker.getDistance(),
					marker.getLatitude(), marker.getLongitude(),
					marker.getColor());
			String markerSection = getSection(marker.getDistance());

			// If the lastSection is not equal to this Section create a new
			// Section before creating the new Entry
			if (!markerSection.equals(lastSection)) {
				if (lastSectionId != -1) {
					((SectionItem) list.get(lastSectionId))
							.setMarkerCount(markerCount);
					markerCount = 0;
				}
				SectionItem section = new SectionItem(markerSection);
				list.add(section);
				lastSectionId = list.size() - 1;
				lastSection = markerSection;
				sectionCount++;
			}

			// Add the EntryItem to the list
			EntryItem entry = new EntryItem(markerInfo);
			list.add(entry);
			markerCount++;
		}

		if (lastSectionId != -1) {
			((SectionItem) list.get(lastSectionId)).setMarkerCount(markerCount);
		}
		
		if (list.size() == 0) {
			SectionItem noResultFound = new SectionItem(getString(R.string.list_view_search_no_result));
			list.add(noResultFound);
			sectionCount++;
		}

		getSupportActionBar().setSubtitle(getString(R.string.list_view_total_markers) + (list.size() - sectionCount));
		
		return list;
	}

	/**
	 * Create the string that represents a section
	 * 
	 * Example: distance = 600 Method returns 500m - 1km (if these are the
	 * nearest sections) distance = 200 Method returns < 250 (if 250 is the
	 * smallest section) distance = 60000 Method returns > 50km (if 50000 is the
	 * biggest section)
	 * 
	 * @param distance
	 *            the distance from the marker to your location
	 * @return A string that indicates how far you are away from a point
	 * 
	 */
	private String getSection(double distance) {
		// TODO: Optimize
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

	/* private classes */

	/**
	 * Save some memory. We are only interested in these 6 things.
	 * 
	 * @author KlemensE
	 * 
	 */
	private class MarkerInfo {
		private String title;
		private String url;
		private Double dist;
		private Double latitude;
		private Double longitude;
		private int color;

		/**
		 * Constructor
		 * 
		 * @param title
		 *            The title of the marker
		 * @param url
		 *            The URL where the marker points to
		 * @param dist
		 *            The distance to my position
		 * @param latitude
		 *            The latitude of the marker
		 * @param longitude
		 *            The longitude of the marker
		 */
		public MarkerInfo(String title, String url, Double dist,
				Double latitude, Double longitude, int color) {
			this.title = title;
			this.url = url;
			this.dist = dist;
			this.latitude = latitude;
			this.longitude = longitude;
			this.color = color;
		}

		public String getTitle() {
			return title;
		}

		public String getUrl() {
			return url;
		}

		public Double getDist() {
			return dist;
		}

		public Double getLatitude() {
			return latitude;
		}

		public Double getLongitude() {
			return longitude;
		}

		public int getColor() {
			return this.color;
		}
	}

	/**
	 * This class is an implementation of the Item class which tells us whether
	 * to draw a Section or an Entry
	 * 
	 * @author KlemensE
	 */
	private class EntryItem implements Item {
		MarkerInfo markerInfo;

		private EntryItem(MarkerInfo info) {
			this.markerInfo = info;
		}

		public MarkerInfo getMarkerInfo() {
			return markerInfo;
		}

		@Override
		public boolean isSection() {
			return false;
		}
	}

	/**
	 * This class extends the ArrayAdapter to be able to create our own View and
	 * OnClickListeners
	 * 
	 * @author KlemensE
	 */
	private class SectionAdapter extends ArrayAdapter<Item> {

		private List<Item> items;

		public SectionAdapter(Context context, int textViewResourceId,
				List<Item> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects;
		}

		public void changeList(List<Item> items) {
			this.items = items;
			notifyDataSetChanged();
		}

		@Override
		public Item getItem(int position) {
			if (position > items.size()) {
				return null;
			}
			return items.get(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			Item i = getItem(position);
			if (i != null) {
				if (i.isSection()) {
					SectionViewHolder sectionViewHolder;
					Object tag = null;
					try {
						tag = convertView.getTag(R.string.list_view_section);
					} catch (Exception e) {
					}

					if (tag == null) {
						convertView = getLayoutInflater().inflate(
								R.layout.list_item_section, null);

						sectionViewHolder = new SectionViewHolder();
						sectionViewHolder.title = (TextView) convertView
								.findViewById(R.id.section_title);
						sectionViewHolder.markerCount = (TextView) convertView
								.findViewById(R.id.section_marker_count);

						convertView.setTag(R.string.list_view_section,
								sectionViewHolder);
					} else {
						sectionViewHolder = (SectionViewHolder) tag;
					}

					convertView.setOnClickListener(null);
					convertView.setOnLongClickListener(null);
					convertView.setLongClickable(false);

					sectionViewHolder.title.setText(((SectionItem) i)
							.getTitle());
					sectionViewHolder.markerCount.setText(getString(R.string.list_view_marker_in_section) + ((SectionItem) i)
							.getMarkerCount());
				} else {
					ViewHolder holder;
					Object tag = null;
					try {
						tag = convertView.getTag(R.string.list_view_entry);
					} catch (Exception e) {
					}

					if (tag == null) {
						convertView = getLayoutInflater().inflate(
								R.layout.marker_list, null);

						holder = new ViewHolder();

						holder.sideBar = (View) convertView
								.findViewById(R.id.side_bar);
						holder.title = (TextView) convertView
								.findViewById(R.id.marker_list_title);
						holder.desc = (TextView) convertView
								.findViewById(R.id.marker_list_summary);
						holder.centerMap = (ImageButton) convertView
								.findViewById(R.id.marker_list_mapbutton);

						convertView.setTag(R.string.list_view_entry, holder);
					} else {
						holder = (ViewHolder) tag;
					}

					EntryItem item = (EntryItem) i;

					MarkerInfo markerInfo = item.getMarkerInfo();
					SpannableString spannableString = new SpannableString(
							markerInfo.getTitle());

					if (markerInfo.getUrl() != null) {
						spannableString.setSpan(new UnderlineSpan(), 0,
								spannableString.length(), 0);
						convertView.setTag(R.string.list_view_webview_id,
								position);
						convertView.setOnClickListener(onClickListenerWebView);
					} else {
						convertView.setOnClickListener(null);
					}

					holder.sideBar.setBackgroundColor(markerInfo.getColor());
					holder.centerMap.setTag(position);
					holder.centerMap
							.setOnClickListener(onClickListenerCenterMap);
					holder.title.setText(spannableString);
					holder.desc.setText(Math.round(markerInfo.getDist()) + "m");
				}
			}

			return convertView;
		}

		public int getCount() {
			return items.size();
		};

		/**
		 * Handles the click event of the centerMap Button, to center the marker
		 * on the Map.
		 */
		OnClickListener onClickListenerCenterMap = new OnClickListener() {
			@Override
			public void onClick(View v) {
				MarkerInfo markerInfo = ((EntryItem) getItem((Integer) v
						.getTag())).getMarkerInfo();

				Intent startMap = new Intent(MixListView.this, MixMap.class);
				startMap.putExtra("center", true);
				startMap.putExtra("latitude", markerInfo.getLatitude());
				startMap.putExtra("longitude", markerInfo.getLongitude());
				startActivityForResult(startMap, 76);
			}
		};

		/**
		 * Handles the click on the list row to open the WebView
		 */
		OnClickListener onClickListenerWebView = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int tag = (Integer) v.getTag(R.string.list_view_webview_id);
				MarkerInfo markerInfo = ((EntryItem) getItem(tag))
						.getMarkerInfo();

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

		private class SectionViewHolder {
			TextView title;
			TextView markerCount;
		}

		private class ViewHolder {
			View sideBar;
			TextView title;
			TextView desc;
			ImageButton centerMap;
		}
	}
}
