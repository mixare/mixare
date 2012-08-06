package org.mixare;

import java.util.ArrayList;
import java.util.List;

import org.mixare.R;
import org.mixare.plugin.Plugin;
import org.mixare.plugin.PluginStatus;
import org.mixare.sectionedlist.Item;
import org.mixare.sectionedlist.SectionItem;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class PluginListActivity extends SherlockActivity {
	private SectionAdapter sectionAdapter;
	private ListView listView;
	private static final int MENU_SELECT_PLUGIN_ID = Menu.FIRST;
	private final String usedPluginsPrefs = "usedPlugins";

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		List<Item> list = createList();

		setContentView(R.layout.list);
		sectionAdapter = new SectionAdapter(this, 0, list);
		listView = (ListView) findViewById(R.id.section_list_view);
		listView.setAdapter(sectionAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MENU_SELECT_PLUGIN_ID, MENU_SELECT_PLUGIN_ID,
				MENU_SELECT_PLUGIN_ID, R.string.select_plugin)
				.setIcon(android.R.drawable.ic_input_add)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		/* Show available plugins from url in a webView */
		case MENU_SELECT_PLUGIN_ID:
			DataView dataView = MixView.getDataView();

			String url = "http://www.mixare.org/plugins/mixare-appview.php";
			try {
				dataView.getContext().getWebContentManager()
						.loadWebPage(url, this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		setResult(getResult());
		savePluginState();
		super.onBackPressed();
	}

	/**
	 * Saves the Plugin State to SharedPreferences to check next time whether
	 * they are enabled or not.
	 */
	protected void savePluginState() {
		// save the Plugins into the list in the MainActivity
		// calculate the count of sections before the Plugins and get the real
		// index of the Plugin
		int sectionsBefore = 0;
		for (int i = 0; i < MainActivity.getPlugins().size(); i++) {
			Item item = sectionAdapter.getItem(i);
			if (!item.isSection()) {
				MainActivity.getPlugins().set(i - sectionsBefore,
						((EntryItem) item).getPlugin());
			} else {
				sectionsBefore++;
			}
		}

		// save the Plugin to the SharedPreferences
		SharedPreferences.Editor shareEditor = getSharedPreferences(
				usedPluginsPrefs, MODE_PRIVATE).edit();

		for (Plugin plugin : MainActivity.getPlugins()) {
			// is the Plugin activated
			boolean activated = plugin.getPluginStatus().equals(
					PluginStatus.Activated) ? true : false;
			// create the name, example:
			// MARKER:org.mixare.plugin.weathermarker.service.WeatherMarkerService
			String name = plugin.getPluginType().name() + ":"
					+ plugin.getServiceInfo().name;
			shareEditor.putBoolean(name, activated);
		}

		shareEditor.commit();
	}

	/**
	 * Checks whether the pluginStatus changed or not
	 * 
	 * @return The proper resultCode
	 */
	public int getResult() {
		int sectionCount = 0;
		for (int i = 0; i < sectionAdapter.getCount(); i++) {
			if (!sectionAdapter.getItem(i).isSection()) {
				if (!((EntryItem) sectionAdapter.getItem(i))
						.getPlugin()
						.equals(MainActivity.getPlugins().get(i - sectionCount))) {
					return 1;
				}
			} else {
				sectionCount++;
			}
		}
		return 0;
	}

	/**
	 * Creates the list to display
	 * 
	 * @return The list containing Item's to display
	 */
	private List<Item> createList() {
		List<Item> list = new ArrayList<Item>();
		String lastSection = "";

		for (Plugin plugin : MainActivity.getPlugins()) {

			String pluginType = "";
			switch (plugin.getPluginType()) {
			case BOOTSTRAP_PHASE_1:
				pluginType = getString(R.string.bootstrap1);
				break;
			case BOOTSTRAP_PHASE_2:
				pluginType = getString(R.string.bootstrap2);
				break;
			case MARKER:
				pluginType = getString(R.string.marker);
				break;
			case DATAHANDLER:
				pluginType = getString(R.string.datahandler);
				break;
			case DATASELECTOR:
				pluginType = getString(R.string.dataselector);
				break;
			default:
				break;
			}

			// If the lastSection is not equal to this Section create a new
			// Section before creating the new Entry
			if (!pluginType.equals(lastSection)) {
				SectionItem section = new SectionItem(pluginType);
				list.add(section);
				lastSection = pluginType;
			}

			// Add the EntryItem to the list
			EntryItem entry = new EntryItem(plugin.clone());
			list.add(entry);
		}

		if (list.size() == 0) {
			SectionItem noResultFound = new SectionItem(
					getString(R.string.list_view_no_plugins_available));
			list.add(noResultFound);
		}

		return list;
	}

	private class EntryItem implements Item {
		private Plugin plugin;

		public EntryItem(Plugin plugin) {
			this.plugin = plugin;
		}

		public Plugin getPlugin() {
			return plugin;
		}

		public void setPlugin(Plugin plugin) {
			this.plugin = plugin;
		}

		@Override
		public boolean isSection() {
			return false;
		}
	}

	private class SectionAdapter extends ArrayAdapter<Item> {
		private List<Item> items;

		public SectionAdapter(Context context, int textViewResourceId,
				List<Item> objects) {
			super(context, textViewResourceId, objects);
			this.items = objects;
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
				} else {
					ViewHolder holder;
					Object tag = null;
					try {
						tag = convertView.getTag(R.string.list_view_entry);
					} catch (Exception e) {
					}

					if (tag == null) {
						convertView = getLayoutInflater().inflate(
								R.layout.plugin_list, null);

						holder = new ViewHolder();
						holder.title = (TextView) convertView
								.findViewById(R.id.list_text);
						holder.desc = (TextView) convertView
								.findViewById(R.id.description_text);
						holder.checkBox = (CheckBox) convertView
								.findViewById(R.id.list_checkbox);
						holder.img = (ImageView) convertView
								.findViewById(R.id.datasource_icon);

						convertView.setTag(R.string.list_view_entry, holder);
					} else {
						holder = (ViewHolder) tag;
					}

					EntryItem item = (EntryItem) i;

					final Plugin currentItem = ((EntryItem) item).getPlugin();

					if (currentItem != null) {
						holder.title.setText(currentItem.getLable());
						holder.desc.setText(currentItem.getServiceInfo().name);
						holder.checkBox.setChecked(currentItem
								.getPluginStatus().equals(
										PluginStatus.Activated));
						holder.checkBox.setTag(position);
						holder.checkBox
								.setOnCheckedChangeListener(checkedChangeListener);
						holder.img.setImageDrawable(currentItem.getLogo());
					}
				}
			}

			return convertView;
		}

		public int getCount() {
			return items.size();
		};

		OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
			/**
			 * Activates Plugins with same Label
			 */
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				int position = (Integer) buttonView.getTag();
				String lable = ((EntryItem) items.get(position)).getPlugin()
						.getLable();

				for (Item item : items) {
					if (!item.isSection()) {
						Plugin plugin = ((EntryItem) item).getPlugin();
						if (plugin.getLable().equals(lable)) {
							if (isChecked) {
								plugin.setPluginStatus(PluginStatus.Activated);
							} else {
								plugin.setPluginStatus(PluginStatus.Deactivated);
							}
						}
						((EntryItem) item).setPlugin(plugin);
					}
				}

				notifyDataSetChanged();
			}
		};

		private class SectionViewHolder {
			TextView title;
		}

		private class ViewHolder {
			TextView title;
			TextView desc;
			CheckBox checkBox;
			ImageView img;
		}
	}
}