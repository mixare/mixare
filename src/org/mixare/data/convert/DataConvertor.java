package org.mixare.data.convert;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.mixare.MixView;
import org.mixare.data.DataSource;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.reality.PhysicalPlace;

import android.widget.Toast;

/**
 * This class is responsible for converting raw data to marker data
 * The class will first check which processor is needed before it handles the data
 * After that it will convert the data to the format the processor wants. I.E. JSON / XML
 * @author A. Egal
 */
public class DataConvertor {
	
	private List<DataProcessor> dataProcessors = new ArrayList<DataProcessor>();
	
	private static DataConvertor instance;
	
	public static DataConvertor getInstance(){
		if(instance == null){
			instance = new DataConvertor();
			instance.addDefaultDataProcessors();
		}
		return instance;
	}
	
	public void clearDataProcessors() {
		dataProcessors.clear();
		addDefaultDataProcessors();
	}
	
	public void addDataProcessor(DataProcessor dataProcessor){
		dataProcessors.add(dataProcessor);
	}
	
	public void removeDataProcessor(DataProcessor dataProcessor){
		dataProcessors.remove(dataProcessor);
	}
	
	public List<Marker> load(String url, String rawResult, DataSource ds){
		DataProcessor dataProcessor = searchForMatchingDataProcessors(url, rawResult);
		if(dataProcessor == null){
			dataProcessor = new MixareDataProcessor(); //using this as default if nothing is found.
		}
		try {
			return dataProcessor.load(rawResult, ds.getTaskId(), ds.getColor());
		} catch (JSONException e) {
			MixView.CONTEXT.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					Toast.makeText(MixView.CONTEXT, "Could not process the url data", Toast.LENGTH_LONG).show();
				}
			});
		}
		return null;
	}
	
	private DataProcessor searchForMatchingDataProcessors(String url, String rawResult){
		for(DataProcessor dp : dataProcessors){
			//checking if url matches any dataprocessor identifiers
			for(String urlIdentifier : dp.getUrlMatch()){
				if(url.contains(urlIdentifier)){
					return dp;
				}
			}
			//checking if data matches any dataprocessor identifiers
			for(String dataIdentifier : dp.getDataMatch()){
				if(rawResult.contains(dataIdentifier)){
					return dp;
				}
			}
		}
		return null;
	}
	
	private void addDefaultDataProcessors(){
		dataProcessors.add(new WikiDataProcessor());
		dataProcessors.add(new TwitterDataProcessor());
		dataProcessors.add(new OsmDataProcessor());
	}
	
	public static String getOSMBoundingBox(double lat, double lon, double radius) {
		String bbox = "[bbox=";
		PhysicalPlace lb = new PhysicalPlace(); // left bottom
		PhysicalPlace rt = new PhysicalPlace(); // right top
		PhysicalPlace.calcDestination(lat, lon, 225, radius*1414, lb); // 1414: sqrt(2)*1000
		PhysicalPlace.calcDestination(lat, lon, 45, radius*1414, rt);
		bbox+=lb.getLongitude()+","+lb.getLatitude()+","+rt.getLongitude()+","+rt.getLatitude()+"]";
		return bbox;

		//return "[bbox=16.365,48.193,16.374,48.199]";
	}
	
}
