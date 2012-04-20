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
