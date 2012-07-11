package org.mixare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mixare.plugin.Plugin;
import org.mixare.plugin.PluginListActivity;
import org.mixare.plugin.PluginStatus;
import org.mixare.plugin.PluginType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;

/**
 * This is the main activity of mixare, that will be opened if mixare is
 * launched through the android.intent.action.MAIN the main tasks of this
 * activity is to search plugins and show a prompt dialog where the user can decide to launch the
 * plugins, or not to launch the plugins.
 * 
 * @author A.Egal
 */
public class MainActivity extends Activity {

	private Context ctx;
	private final String usedPluginsPrefs = "usedPlugins";
	private static List<Plugin> plugins;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			startActivity(new Intent(ctx, PluginLoaderActivity.class));
			finish();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		plugins = new ArrayList<Plugin>();
		ctx = this;
		
		if (havePluginsChanged() || arePluginsDeinstalled()) {
			SharedPreferences.Editor prefEditor= getSharedPreferences(usedPluginsPrefs, MODE_PRIVATE).edit();
			prefEditor.clear();
			prefEditor.commit();
			showDialog();
		} else {
			startActivity(new Intent(ctx, PluginLoaderActivity.class));
			finish();
		}
	}

	/**
	 * Checks the preferences for Plugins that got deinstalled
	 * @return True if a plugin got deinstalled
	 */
	public boolean arePluginsDeinstalled() {
		SharedPreferences prefs = getSharedPreferences(usedPluginsPrefs, MODE_PRIVATE);
		for (Entry<String, ?> entry : prefs.getAll().entrySet()) {
			String[] array = entry.getKey().split(":");
			if (array.length == 2) {
				boolean found = false;
				for (Plugin plugin : MainActivity.getPlugins()) {
					String pluginType = plugin.getPluginType().name();
					String pluginServiceName = plugin.getServiceInfo().name;
					if (pluginType.equalsIgnoreCase(array[0])) {
						if (pluginServiceName.equalsIgnoreCase(array[1])) {
							found = true;
						}
					}
				}

				if (!found) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Shows a dialog
	 */
	public void showDialog() {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

		dialog.setTitle(R.string.launch_plugins);
		dialog.setMessage(R.string.plugin_message);
		dialog.setCancelable(false);

		// Allways activate new plugins

		// final CheckBox checkBox = new CheckBox(ctx);
		// checkBox.setText(R.string.remember_this_decision);
		// dialog.setView(checkBox);

		dialog.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int whichButton) {
						startActivityForResult(new Intent(ctx,
								PluginListActivity.class), 1);
						d.dismiss();
					}
				});

		dialog.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int whichButton) {
						disableNewFoundPlugins();
						savePluginState();
						startActivity(new Intent(ctx,
								PluginLoaderActivity.class));
						d.dismiss();
						finish();
					}
				});

		dialog.show();
	}

	/**
	 * Diables all new Found Plugins
	 */
	private void disableNewFoundPlugins() {
		for (Plugin plugin : MainActivity.getPlugins()) {
			if (plugin.getPluginStatus().equals(PluginStatus.New)) {
				plugin.setPluginStatus(PluginStatus.Deactivated);
			}
		}
	}

	/**
	 * Saves the Plugin State to SharedPreferences to check next time whether
	 * they are enabled or not.
	 */
	protected void savePluginState() {
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
	 * Calls method to fill Plugin List, compares with Shared Preferences and
	 * checks if there are still Plugins with PluginState.New
	 * 
	 * @return Whether new Plugins are available or not
	 */
	private boolean havePluginsChanged() {
		getInstalledPlugins();
		checkPluginPreferences();
		
//		SharedPreferences.Editor sharedEditor = getSharedPreferences(
//				usedPluginsPrefs, MODE_PRIVATE).edit();
//		sharedEditor.clear();
//		sharedEditor.commit();
		savePluginState();

		for (Plugin plugin : MainActivity.getPlugins()) {
			if (plugin.getPluginStatus().equals(PluginStatus.New)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Check if installed Plugins are allready known and set them to their
	 * previous Status
	 */
	private void checkPluginPreferences() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				usedPluginsPrefs, MODE_PRIVATE);
		for (int i = 0; i < MainActivity.getPlugins().size(); i++) {
			String name = MainActivity.getPlugins().get(i).getPluginType()
					.name()
					+ ":"
					+ MainActivity.getPlugins().get(i).getServiceInfo().name;

			if (sharedPreferences.contains(name)) {
				if (sharedPreferences.getBoolean(name, true)) {
					MainActivity.getPlugins().get(i)
							.setPluginStatus(PluginStatus.Activated);
				} else {
					MainActivity.getPlugins().get(i)
							.setPluginStatus(PluginStatus.Deactivated);
				}
			}
		}
	}

	/**
	 * Fills a list with installed Plugins
	 */
	private void getInstalledPlugins() {
		PluginType[] allPluginTypes = PluginType.values();
		for (PluginType pluginType : allPluginTypes) {
			PackageManager packageManager = getPackageManager();
			Intent baseIntent = new Intent(pluginType.getActionName());
			List<ResolveInfo> list = packageManager.queryIntentServices(
					baseIntent, PackageManager.GET_RESOLVED_FILTER);

			for (ResolveInfo resolveInfo : list) {
				Plugin plugin = new Plugin(PluginStatus.New,
						resolveInfo.serviceInfo,
						(String) resolveInfo.loadLabel(packageManager),
						resolveInfo.loadIcon(packageManager), pluginType);
				this.addPlugin(plugin);
			}
		}
	}

	public void addPlugin(Plugin plugin) {
		MainActivity.plugins.add(plugin);
	}

	public static List<Plugin> getPlugins() {
		if (plugins == null) {
			plugins = new ArrayList<Plugin>();
		}
		return plugins;
	}
}