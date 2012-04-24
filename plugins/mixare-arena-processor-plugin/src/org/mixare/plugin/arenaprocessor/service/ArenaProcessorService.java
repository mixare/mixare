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
package org.mixare.plugin.arenaprocessor.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.mixare.lib.marker.InitialMarkerData;
import org.mixare.lib.service.IDataHandlerService;
import org.mixare.plugin.arenaprocessor.ArenaProcessor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class ArenaProcessorService extends Service{

	public final String pluginName = "arenaProcessor";
	private Map<String, ArenaProcessor> processor = new HashMap<String, ArenaProcessor>();	
	public static ArenaProcessorService instance;
	private Integer count = 0;
	
	@Override
	public IBinder onBind(Intent intent) {
		instance = this;
		return binder;
	}
	
	public final IDataHandlerService.Stub binder = new IDataHandlerService.Stub() {

		@Override
		public String build() throws RemoteException {
			ArenaProcessor arenaProcessor = new ArenaProcessor();
			String processorName = "arenaProcessor-"+count+"-"+arenaProcessor.hashCode();
			processor.put(processorName, arenaProcessor);
			return processorName;
		}

		@Override
		public String[] getDataMatch(String processorName) throws RemoteException {
			return processor.get(processorName).getDataMatch();
		}

		@Override
		public int getPid() throws RemoteException {
			return 0;
		}

		@Override
		public String getPluginName() throws RemoteException {
			return pluginName;
		}

		@Override
		public String[] getUrlMatch(String processorName) throws RemoteException {
			return processor.get(processorName).getUrlMatch();
		}

		@Override
		public List<InitialMarkerData> load(String processorName, String rawData, 
				int taskId, int colour) throws RemoteException {
			try {
				return processor.get(processorName).load(rawData, taskId, colour);
			} catch (JSONException e) {
				e.printStackTrace();
				return new ArrayList<InitialMarkerData>();
			}
		}
	
	};

	
}
