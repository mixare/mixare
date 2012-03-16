package org.mixare.plugin;

import org.mixare.lib.service.IMarkerService;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class MarkerServiceConnection implements ServiceConnection{

	@Override
	public void onServiceDisconnected(ComponentName name) {
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// get instance of the aidl binder
		IMarkerService iMarkerService = IMarkerService.Stub
				.asInterface(service);
		try {
			String markername = iMarkerService.getPluginName();
			PluginLoader.markerServices.put(markername, iMarkerService);
		} catch (RemoteException e) {
			Log.e("RemoteException", e.toString());
		}
	}
}
