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
package org.mixare.plugin.connection;

import java.util.HashMap;
import java.util.Map;

import org.mixare.lib.service.IMarkerService;
import org.mixare.plugin.PluginConnection;
import org.mixare.plugin.PluginNotFoundException;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * A service connector for the marker plugins, this class creates a IMarkerService instance
 * for every loaded plugin. And stores them in a hashmap.
 * @author A. Egal
 *
 */
public class MarkerServiceConnection extends PluginConnection implements ServiceConnection{

	private Map<String, IMarkerService> markerServices = new HashMap<String, IMarkerService>();

	@Override
	public void onServiceDisconnected(ComponentName name) {
		markerServices.clear();
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// get instance of the aidl binder
		IMarkerService iMarkerService = IMarkerService.Stub
				.asInterface(service);
		try {
			String markername = iMarkerService.getPluginName();
			markerServices.put(markername, iMarkerService);
			storeFoundPlugin();
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}
	
	public Map<String, IMarkerService> getMarkerServices() {
		return markerServices;
	}
}
