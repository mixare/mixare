package org.mixare.lib.service;

import org.mixare.lib.marker.InitialMarkerData;
/**
 * Android Interface Definition Language for contact between services in different threads,
 * In this case: The IDataHandlerService connects the mixare core with the datahandlers of the plugins.
 */
interface IDataHandlerService {
    /** Request the process ID of this service. */
    int getPid();
    
 	String build();
 	
 	String getPluginName();
 	
 	String[] getUrlMatch(String processorName);
 	
 	String[] getDataMatch(String processorName);
 	
 	List<InitialMarkerData> load(String processorName, String rawData, int taskId, int colour);
}