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

import org.mixare.lib.service.IBootStrap;
import org.mixare.plugin.PluginConnection;
import org.mixare.plugin.PluginLoader;
import org.mixare.plugin.PluginNotFoundException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * A plugin loader + binder for bootstrap plugins (plugins that are loaded on start)
 * @author A. Egal
 */
public class BootStrapActivityConnection extends PluginConnection implements ActivityConnection{

	private IBootStrap iBootStrap;
	private Intent activityIntent;
	private int bootstrapRequestCode = -1;
	
	@Override
	public void startActivityForResult(Activity activity) {
		PluginLoader.getInstance().decreasePendingActivitiesOnResult();
		if(activityIntent == null){
			throw new PluginNotFoundException();
		}
		try{
			bootstrapRequestCode = iBootStrap.getActivityRequestCode();
			activity.startActivityForResult(activityIntent, bootstrapRequestCode);
		}catch(RemoteException e){
			throw new PluginNotFoundException(e);
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		iBootStrap = IBootStrap.Stub.asInterface(service);
		try {
			buildIntent();
			String pluginName = iBootStrap.getPluginName();
			storeFoundPlugin(pluginName);
			PluginLoader.getInstance().increasePendingActivitiesOnResult();
			PluginLoader.getInstance().startPlugin(getPluginType(), pluginName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}		
	}
	
	private void buildIntent() throws RemoteException{
		activityIntent = new Intent();
		String packageName = iBootStrap.getActivityPackage();
		String className = iBootStrap.getActivityName();
		activityIntent.setClassName(packageName, className);
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		iBootStrap = null;
		activityIntent = null;		
	}
	
}
