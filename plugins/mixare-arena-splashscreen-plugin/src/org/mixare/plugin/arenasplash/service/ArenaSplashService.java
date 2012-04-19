package org.mixare.plugin.arenasplash.service;

import org.mixare.lib.service.IBootStrap;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class ArenaSplashService extends Service{

	private final String ACTIVITY_PACKAGE = "org.mixare.plugin.arenasplash";	
	private final String ACTIVITY_NAME = "org.mixare.plugin.arenasplash.ArenaSplashActivity";
	private final String PLUGIN_NAME = "arena-splash";
	private final int Z_INDEX = 0;
	public static final int ACTIVITY_REQUEST_CODE = 2118; 
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	private final IBootStrap.Stub binder = new IBootStrap.Stub() {

		@Override
		public String getActivityName() throws RemoteException {
			return ACTIVITY_NAME;
		}

		@Override
		public String getActivityPackage() throws RemoteException {
			return ACTIVITY_PACKAGE;
		}
		
		@Override
		public int getZIndex() throws RemoteException {
			return Z_INDEX;
		}

		@Override
		public String getPluginName() throws RemoteException {
			return PLUGIN_NAME;
		}

		@Override
		public int getActivityRequestCode() throws RemoteException {
			return ACTIVITY_REQUEST_CODE;
		}

	};
	
}
