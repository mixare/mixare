package org.mixare.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.mixare.data.DataHandler;
import org.mixare.data.convert.DataProcessor;
import org.mixare.lib.marker.InitialMarkerData;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.service.IDataHandlerService;

import android.os.RemoteException;

public class RemoteDataHandler extends DataHandler implements DataProcessor{

	private String dataHandlerName;
	private IDataHandlerService iDataHandlerService;
	
	public String getDataHandlerName() {
		return dataHandlerName;
	}
	
	public RemoteDataHandler(IDataHandlerService iDataHandlerService) {
		this.iDataHandlerService = iDataHandlerService;
	}
	
	public void buildDataHandler(){
		try {
			this.dataHandlerName = iDataHandlerService.build();
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}
	
	public String[] getUrlMatch() {
		try {
			return iDataHandlerService.getUrlMatch(dataHandlerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}
	
	public String[] getDataMatch() {
		try {
			return iDataHandlerService.getDataMatch(dataHandlerName);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}
	
	public List<Marker> load(String rawData, int taskId, int colour) throws JSONException {
		try {
			List<InitialMarkerData> initialMarkerData = iDataHandlerService.load(dataHandlerName, rawData, taskId, colour);
			return initializeMarkerData(initialMarkerData);
		} catch (RemoteException e) {
			throw new PluginNotFoundException(e);
		}
	}
	
	private List<Marker> initializeMarkerData(List<InitialMarkerData> initialMarkerData) throws PluginNotFoundException, RemoteException{
		List<Marker> markers = new ArrayList<Marker>();
		for(InitialMarkerData i : initialMarkerData){
			Marker marker = PluginLoader.getInstance().getMarkerInstance(i.getMarkerName(),
					(String)i.getConstr()[0], (Double)i.getConstr()[1], (Double)i.getConstr()[2], 
					(Double)i.getConstr()[3], (String)i.getConstr()[4], (Integer)i.getConstr()[5], (Integer)i.getConstr()[6]);
			fillExtraMarkerParcelableProperties(marker, i.getExtraParcelables());
			fillExtraMarkerPrimitiveProperties(marker, i.getExtraPrimitives());
			
			markers.add(marker);
		}
		return markers;
	}
	
	private Marker fillExtraMarkerParcelableProperties(Marker marker, Map<String, ParcelableProperty> properties){
		Iterator<Entry<String, ParcelableProperty>> it = properties.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, ParcelableProperty> pairs = (Entry<String, ParcelableProperty>)it.next();
			marker.setExtras(pairs.getKey(), pairs.getValue());
		}
		return marker;
	}
	
	private Marker fillExtraMarkerPrimitiveProperties(Marker marker, Map<String, PrimitiveProperty> properties){
		Iterator<Entry<String, PrimitiveProperty>> it = properties.entrySet().iterator();
		while(it.hasNext()){
			Entry<String, PrimitiveProperty> pairs = (Entry<String, PrimitiveProperty>)it.next();
			marker.setExtras(pairs.getKey(), pairs.getValue());
		}
		return marker;
	}
	
	
}
