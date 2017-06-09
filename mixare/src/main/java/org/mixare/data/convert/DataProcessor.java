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
	
	boolean matchesRequiredType(String type);
	
	List<Marker> load(String rawData, int taskId, int colour) throws JSONException;
}
