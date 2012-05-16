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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * Searches, loads and executes available plugins that are installed on the device.
 * @author A.Egal
 */
public class PluginLoader {
	
	private static PluginLoader instance;
	
	private Activity activity;
	
	private Map<String, PluginConnection> pluginMap = new HashMap<String, PluginConnection>();
	
	private int pendingActivitiesOnResult = 0;
	
	public static PluginLoader getInstance() {
		if(instance == null){
			instance = new PluginLoader();
		}
		return instance;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
	
	/**
	 * loads all plugins from a plugin type.
	 */
	public void loadPlugin(PluginType pluginType) {
		PackageManager packageManager = activity.getPackageManager();
		Intent baseIntent = new Intent(pluginType.getActionName());
		baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
		List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent,
				PackageManager.GET_RESOLVED_FILTER);
		
		initService(list, activity, pluginType);
	}
	
	/**
	 * Initializes the services from the loaded plugins and stores them in the pluginmap
	 */
	private void initService(List<ResolveInfo> list, Activity activity, PluginType pluginType){
		for (int i = 0; i < list.size(); ++i) {
			ResolveInfo info = list.get(i);
			ServiceInfo sinfo = info.serviceInfo;
			if (sinfo != null) {
				Intent serviceIntent = new Intent();
				serviceIntent.setClassName(sinfo.packageName, sinfo.name);
				activity.startService(serviceIntent);
				activity.bindService(serviceIntent, (ServiceConnection)pluginType.getPluginConnection(),
						Context.BIND_AUTO_CREATE);
				checkForPendingActivity(pluginType);
			}
		}
	}
	
	/**
	 * Unbinds all plugins from the activity
	 */
	public void unBindServices(){
		for(PluginConnection pluginConnection : pluginMap.values()){
			if(pluginConnection instanceof ServiceConnection){
				activity.unbindService((ServiceConnection)pluginConnection);
			}
		}
	}

	/**
	 * Starts an activity plugin
	 */
	public void startPlugin(PluginType pluginType, String pluginName){
		if(pluginType.getLoader() == Loader.Activity){
			ActivityConnection activityConnection = (ActivityConnection)pluginMap.get(pluginName);
			activityConnection.startActivityForResult(activity);
		}
		else{
			throw new PluginNotFoundException("Cannot directly start a non-activity plugin," +
					" you must call a instance for it");
		}	
	}
	
	protected void addFoundPluginToMap(String pluginName, PluginConnection pluginConnection){
		pluginMap.put(pluginName, pluginConnection);
	}
	
	public Marker getMarkerInstance(String markername, int id, String title,
			double latitude, double longitude, double altitude, String link,
			int type, int color) throws PluginNotFoundException, RemoteException {
		
		MarkerServiceConnection msc = (MarkerServiceConnection)pluginMap.get(PluginType.MARKER.toString());
		IMarkerService iMarkerService = msc.getMarkerServices().get(markername);		
		
		if (iMarkerService == null) {
			throw new PluginNotFoundException();
		}
		RemoteMarker rm = new RemoteMarker(iMarkerService);
		rm.buildMarker(id, title, latitude, longitude, altitude, link, type, color);
		return rm; 
	}
	
	public PluginConnection getPluginConnection(String name){
		return pluginMap.get(name);
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

}