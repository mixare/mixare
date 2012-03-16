package org.mixare.plugin;

import java.util.HashMap;
import java.util.List;

import org.mixare.lib.marker.MarkerInterface;
import org.mixare.lib.service.IMarkerService;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;

public class PluginLoader {

	public static final String MARKER_PLUGIN = "org.mixare.plugin.marker";
	public static HashMap<String, IMarkerService> markerServices = new HashMap<String, IMarkerService>();
	private static ServiceConnection serviceConnection = new MarkerServiceConnection();
	
	public static void loadMarkerPlugins(Context ctx) {
		PackageManager packageManager = ctx.getPackageManager();
		Intent baseIntent = new Intent(MARKER_PLUGIN);
		baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
		List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent,
				PackageManager.GET_RESOLVED_FILTER);
		startPlugin(list, ctx);
	}
	
	private static void startPlugin(List<ResolveInfo> list, Context ctx){
		for (int i = 0; i < list.size(); ++i) {
			ResolveInfo info = list.get(i);
			ServiceInfo sinfo = info.serviceInfo;
			if (sinfo != null) {
				Intent serviceIntent = new Intent();
				serviceIntent.setClassName(sinfo.packageName, sinfo.name);
				ctx.bindService(serviceIntent, serviceConnection,
						Context.BIND_AUTO_CREATE);
			}
		}
	}

	public static MarkerInterface getMarkerInstance(String markername, String title,
			double latitude, double longitude, double altitude, String link,
			int type, int color) throws PluginNotFoundException, RemoteException {
		IMarkerService iMarkerService = markerServices.get(markername);
		if (iMarkerService == null) {
			throw new PluginNotFoundException();
		}
		RemoteMarker rm = new RemoteMarker(markername, iMarkerService);
		rm.buildMarker(title, latitude, longitude, altitude, link, type, color);
		return rm; 
	}

}