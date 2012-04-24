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
