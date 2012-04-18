package org.mixare.data.convert;

import java.util.List;

import org.json.JSONException;
import org.mixare.lib.marker.Marker;

/**
 * A data processor interface, Classes that are implemented by this interface 
 * are responsible for converting raw data into markers.
 * This class also contains 2 abstract methods, those methods can be implemented by
 * the interface and they describe the conditions that are needed for the processor to be activated
 * @author A. Egal
 */
public interface DataProcessor {

	String[] getUrlMatch();
	
	String[] getDataMatch();
	
	List<Marker> load(String rawData, int taskId, int colour) throws JSONException;
}
