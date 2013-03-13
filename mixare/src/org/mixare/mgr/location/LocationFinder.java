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
package org.mixare.mgr.location;

import org.mixare.mgr.downloader.DownloadManager;

import android.hardware.GeomagneticField;
import android.location.Location;

/**
 * This class is repsonsible for finding the location, and sending it back to
 * the mixcontext.
 */
public interface LocationFinder {

	/**
	 * Possible status of LocationFinder
	 */
	public enum LocationFinderState {
		Active, // Providing Location Information
		Inactive, // No-Active
		Confused // Same problem in internal state
	}

	/**
	 * Finds the location through the providers  
	 * @param ctx
	 * @return
	 */
	void findLocation();

	/**
	 * A working location provider has been found: check if 
	 * the found location has the best accuracy.
	 */
	void locationCallback(String provider);
	
	/**
	 * Returns the current location.
	 */
	Location getCurrentLocation();

	/**
	 * Gets the location that was used in the last download for
	 * datasources.
	 * @return
	 */
	Location getLocationAtLastDownload();

	/**
	 * Sets the property to the location with the last successfull download.
	 */
	void setLocationAtLastDownload(Location locationAtLastDownload);

	/**
	 * Set the DownloadManager manager at this service
	 * 
	 * @param downloadManager
	 */
	void setDownloadManager(DownloadManager downloadManager);

	/**
	 * Request to active the service
	 */
	void switchOn();

	/**
	 * Request to deactive the service
	 */
	void switchOff();

	/**
	 * Status of service
	 * 
	 * @return
	 */
	LocationFinderState getStatus();

	/**
	 * 
	 * @return GeomagneticField
	 */
	GeomagneticField getGeomagneticField();

}