/*
 * Copyleft 2012 - Peer internet solutions & Alessandro Staniscia
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
package org.mixare.mgr.datasource;

import org.mixare.data.DataSource;

/**
 * This class is responsible for Data Source Managing
 * 
 */
public interface DataSourceManager {

	/**
	 * Is At Least One Datasource Selected
	 * 
	 * @return boolean
	 */
	boolean isAtLeastOneDatasourceSelected();

	/**
	 * Sync DataSouceManager with DataSourceStorage.
	 */
	void refreshDataSources();

	/**
	 * Clean all old datasource and insert only source.
	 * 
	 * @param source
	 */
	void setAllDataSourcesforLauncher(DataSource source);

	/**
	 * send command to download data information from datasource
	 * 
	 * @param lat
	 * @param lon
	 * @param alt
	 * @param radius
	 */
	void requestDataFromAllActiveDataSource(double lat, double lon, double alt,
			float radius);

}
