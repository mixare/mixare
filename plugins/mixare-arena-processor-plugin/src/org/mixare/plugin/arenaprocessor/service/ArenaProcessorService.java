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
