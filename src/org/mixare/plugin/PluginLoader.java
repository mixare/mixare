/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
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
package org.mixare.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mixare.MainActivity;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.service.IMarkerService;
import org.mixare.plugin.connection.ActivityConnection;
import org.mixare.plugin.connection.MarkerServiceConnection;
import org.mixare.plugin.remoteobjects.RemoteMarker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;
import android.util.Log;

/**
 * loads and executes available plugins that are installed on the device.
 * @author A.Egal
 */
public class PluginLoader {
	
	private static PluginLoader instance;
	
	private Activity activity;
	

	
//	private Map<String, PluginConnection> pluginMap = new HashMap<String, PluginConnection>();
	
	private List<PluginType> loadedPlugins = new ArrayList<PluginType>();
	
	private int pendingActivitiesOnResult = 0;
	
	public static PluginLoader getInstance() {
		if(instance == null){
			instance = new PluginLoader();
		}
		return instance;
	}

	public static void newInstance() {
		instance = new PluginLoader();
	}
	
	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	/**
	 * loads all plugins from a plugin type.
	 */
	public void loadPlugin(PluginType pluginType) {
		List<Plugin> plugins = new ArrayList<Plugin>();
		for (Plugin plugin : MainActivity.getPlugins()) {
			if (plugin.getPluginType().equals(pluginType) && plugin.getPluginStatus().equals(PluginStatus.Activated)) {
				plugins.add(plugin);
			}
		}
		
		initService(plugins);
		loadedPlugins.add(pluginType);
	}
	
	/**
	 * Initializes the services from the loaded plugins and stores them in the pluginmap
	 */
	private void initService(List<Plugin> list){
		for (int i = 0; i < list.size(); ++i) {
			ServiceInfo sinfo = list.get(i).getServiceInfo();
			if (sinfo != null) {
				Intent serviceIntent = new Intent();
				serviceIntent.setClassName(sinfo.packageName, sinfo.name);
				activity.startService(serviceIntent);
				activity.bindService(serviceIntent, (ServiceConnection)list.get(i).getPluginConnection(),
						Context.BIND_AUTO_CREATE);
				checkForPendingActivity(list.get(i).getPluginType());
			}
		}
	}
	
	/**
	 * Unbinds all plugins from the activity
	 */
	public void unBindServices() {
		try {
			for (Plugin plugin : MainActivity.getPlugins()) {
				if (plugin.getPluginConnection() instanceof ServiceConnection) {
					try {
						activity.unbindService((ServiceConnection) plugin.getPluginConnection());
						plugin.setPluginConnection(null);
					} catch (IllegalArgumentException iae) {
	//					Log.e("PluginLoader", "Service: " + plugin.getLable()
	//							+ " is not registered");
					}
				}
			}
		} catch (Exception e) {
			try {
				Log.e("test", String.valueOf(MainActivity.getPlugins().size()));
			} catch (Exception e2) {
				Log.e("test", "sadf");
			}
		}
	}

	/**
	 * Starts an activity plugin
	 */
	public void startPlugin(PluginType pluginType, String pluginName){
		if(pluginType.getLoader() == Loader.Activity){
			ActivityConnection activityConnection = null;
			for (Plugin plugin : MainActivity.getPlugins()) {
				if (plugin.getServiceInfo().name.equals(pluginName)) {
					activityConnection = (ActivityConnection) plugin.getPluginConnection();
					break;
				}
			}
			if (activityConnection != null) {
				activityConnection.startActivityForResult(activity);
			}
		}
		else{
			throw new PluginNotFoundException("Cannot directly start a non-activity plugin," +
					" you must call a instance for it");
		}	
	}
	
	protected void addFoundPluginToMap(String pluginName, PluginConnection pluginConnection){
		for (Plugin plugin : MainActivity.getPlugins()) {
			if (plugin.getServiceInfo().name.equals(pluginName)) {
				plugin.setPluginConnection(pluginConnection);
				break;
			}
		}
	}
	
	public Marker getMarkerInstance(String markername, int id, String title,
			double latitude, double longitude, double altitude, String link,
			int type, int color) throws PluginNotFoundException,
			RemoteException {
		
		try {
			MarkerServiceConnection msc = null;
			for (Plugin plugin : MainActivity.getPlugins()) {
				if (plugin.getPluginType().equals(PluginType.MARKER)) {
					msc = (MarkerServiceConnection) plugin.getPluginConnection();
					break;
				}
			}
			IMarkerService iMarkerService = msc.getMarkerServices().get(
					markername);

			if (iMarkerService == null) {
				throw new PluginNotFoundException();
			}
			RemoteMarker rm = new RemoteMarker(iMarkerService);
			rm.buildMarker(id, title, latitude, longitude, altitude, link,
					type, color);
			return rm;
		} catch (NullPointerException ne) {
			System.exit(0);
			return null;
		}
	}
	
	public int getPendingActivitiesOnResult(){
		return pendingActivitiesOnResult;
	}
	
	public void increasePendingActivitiesOnResult(){
		pendingActivitiesOnResult++;
	}
	
	public void decreasePendingActivitiesOnResult(){
		pendingActivitiesOnResult--;
	}
	
	private void checkForPendingActivity(PluginType pluginType){
		if(pluginType.getLoader() == Loader.Activity){
			increasePendingActivitiesOnResult();
		}
	}
	
	public boolean isPluginTypeLoaded(PluginType pluginType){
		return loadedPlugins.contains(pluginType);
	}
}