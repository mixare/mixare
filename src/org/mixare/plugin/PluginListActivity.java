package org.mixare.plugin;

import java.util.ArrayList;
import java.util.List;

import org.mixare.DataView;
import org.mixare.MainActivity;
import org.mixare.MixView;
import org.mixare.R;
import org.mixare.sectionedlist.SectionedListItem;
import org.mixare.sectionedlist.SectionListAdapter;
import org.mixare.sectionedlist.SectionListView;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class PluginListActivity extends Activity {
	private StandardArrayAdapter arrayAdapter;
	private SectionListAdapter sectionAdapter;
	private SectionListView listView;
	private static final int MENU_SELECT_PLUGIN_ID = Menu.FIRST;
	private static final int MENU_SELECT_ALL = Menu.FIRST + 1;
	private static final int MENU_DESELECT_ALL = Menu.FIRST + 2;
	private static final int MENU_RELOAD = Menu.FIRST + 3;
	private final String usedPluginsPrefs = "usedPlugins";

	private class StandardArrayAdapter extends ArrayAdapter<SectionedListItem>
			implements OnCheckedChangeListener {

		private SectionedListItem[] items;

		public SectionedListItem[] getItems() {
			return items;
		}

		public StandardArrayAdapter(final Context context,
				final int textViewResourceId, final SectionedListItem[] items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		public void selectAll() {
			for (SectionedListItem item : items) {
				Plugin plugin = (Plugin) item.getItem();
				plugin.setPluginStatus(PluginStatus.Activated);
				item.setItem(plugin);
			}
			this.notifyDataSetChanged();
		}

		public void deselectAll() {
			for (SectionedListItem item : items) {
				Plugin plugin = (Plugin) item.getItem();
				plugin.setPluginStatus(PluginStatus.Deactivated);
				item.setItem(plugin);
			}
			this.notifyDataSetChanged();
		}

		@Override
		public View getView(final int position, final View convertView,
				final ViewGroup parent) {
			ViewHolder holder = null;
			View view = convertView;
			if (view == null) {
				final LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = vi.inflate(R.layout.pluginlist, null);

				holder = new ViewHolder();
				holder.title = (TextView) view.findViewById(R.id.list_text);
				holder.desc = (TextView) view
						.findViewById(R.id.description_text);
				holder.checkBox = (CheckBox) view
						.findViewById(R.id.list_checkbox);
				holder.img = (ImageView) view
						.findViewById(R.id.datasource_icon);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			final Plugin currentItem = (Plugin) items[position].getItem();

			if (currentItem != null) {
				holder.title.setText(currentItem.getLable());
				holder.desc.setText(currentItem.getServiceInfo().name);
				holder.checkBox.setChecked(currentItem.getPluginStatus()
						.equals(PluginStatus.Activated));
				holder.checkBox.setTag(position);
				holder.checkBox.setOnCheckedChangeListener(this);
				holder.img.setImageDrawable(currentItem.getLogo());
			}

			return view;
		}

		/**
		 * Activates Plugins with same Label
		 */
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int position = (Integer) buttonView.getTag();
			String lable = MainActivity.getPlugins().get(position).getLable();

			for (SectionedListItem item : items) {
				Plugin plugin = (Plugin) item.getItem();
				if (plugin.getLable().equals(lable)) {
					if (isChecked) {
						plugin.setPluginStatus(PluginStatus.Activated);
					} else {
						plugin.setPluginStatus(PluginStatus.Deactivated);
					}
				}
				item.setItem(plugin);
			}

			arrayAdapter.notifyDataSetChanged();
		}
	
		private class ViewHolder {
			TextView title;
			TextView desc;
			CheckBox checkBox;
			ImageView img;
		}
	}

	/**
	 * Saves the Plugin State to SharedPreferences to check next time whether
	 * they are enabled or not.
	 */
	protected void savePluginState() {
		for (int i = 0; i < MainActivity.getPlugins().size(); i++) {
			MainActivity.getPlugins().set(i,
					(Plugin) arrayAdapter.getItem(i).getItem());
		}

		SharedPreferences sharedPreferences = getSharedPreferences(
				usedPluginsPrefs, MODE_PRIVATE);
		SharedPreferences.Editor shareEditor = sharedPreferences.edit();
		for (Plugin plugin : MainActivity.getPlugins()) {
			boolean activated = plugin.getPluginStatus().equals(
					PluginStatus.Activated) ? true : false;
			shareEditor.putBoolean(
					plugin.getPluginType().name() + ":"
							+ plugin.getServiceInfo().name, activated);
		}
		shareEditor.commit();
	}

	/**
	 * Checks whether the pluginStatus changed or not
	 * 
	 * @return The proper resultCode
	 */
	public int getResult() {
		for (int i = 0; i < arrayAdapter.getItems().length; i++) {
			SectionedListItem item = arrayAdapter.getItem(i);
			Plugin plugin = (Plugin) item.getItem();
			if (!plugin.equals(MainActivity.getPlugins().get(i))) {
				return 1;
			}
		}

		return 0;
	}

	@Override
	public void onBackPressed() {
		setResult(getResult());
		savePluginState();
		super.onBackPressed();
	}

	/**
	 * Create an array out of SectionedListItem's, which contain the plugin
	 * itself and as section the plugin Type
	 * 
	 * @return An array of Plugin
	 */
	private SectionedListItem[] createArray() {
		List<SectionedListItem> list = new ArrayList<SectionedListItem>();

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
			list.add(new SectionedListItem(plugin.clone(), pluginType));
		}

		return list.toArray(new SectionedListItem[] {});
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.list);
		arrayAdapter = new StandardArrayAdapter(this, R.id.list_text,
				createArray());
		sectionAdapter = new SectionListAdapter(getLayoutInflater(),
				arrayAdapter);
		listView = (SectionListView) findViewById(R.id.section_list_view);
		listView.setAdapter(sectionAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MENU_SELECT_PLUGIN_ID, MENU_SELECT_PLUGIN_ID,
				MENU_SELECT_PLUGIN_ID, R.string.select_plugin);
		menu.add(MENU_SELECT_ALL, MENU_SELECT_ALL, MENU_SELECT_ALL,
				R.string.select_all);
		menu.add(MENU_DESELECT_ALL, MENU_DESELECT_ALL, MENU_DESELECT_ALL,
				R.string.deselect_all);
		menu.add(MENU_RELOAD, MENU_RELOAD, MENU_RELOAD, R.string.reload_plugins);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
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
			return true;
			/* Select all plugins */
		case MENU_SELECT_ALL:
			arrayAdapter.selectAll();
			return true;
			/* Deselect all plugins */
		case MENU_DESELECT_ALL:
			arrayAdapter.deselectAll();
			return true;
		case MENU_RELOAD:
			
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
}