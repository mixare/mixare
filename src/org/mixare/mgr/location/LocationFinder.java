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

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Location;

public interface LocationFinder {

	enum LocationFinderState {
		Active, Inactive, Confused
	}

	Location findLocation(Context ctx);

	/**
	 * Returns the current location.
	 */
	Location getCurrentLocation();

	Location getLocationAtLastDownload();

	/**
	 * Sets the property to the location with the last successfull download.
	 */
	void setLocationAtLastDownload(Location locationAtLastDownload);

	void setDownloadManager(DownloadManager downloadManager);

	void switchOn();

	void switchOff();

	LocationFinderState getStatus();

	GeomagneticField getGeomagneticField();

}