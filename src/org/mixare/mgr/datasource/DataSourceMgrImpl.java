/*
 * Copyleft 2012 - Peer internet solutions 
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

import java.util.ArrayList;

import org.mixare.data.DataSource;
import org.mixare.data.DataSourceStorage;

import android.content.Context;

class DataSourceMgrImpl implements DataSourceManager {
	
	private ArrayList<DataSource> allDataSources = new ArrayList<DataSource>();
	
	private Context ctx;
	
	public DataSourceMgrImpl(Context ctx){
		this.ctx=ctx;
	}
	
	@Override
	public boolean isAtLeastOneDatasourceSelected(){
		boolean atLeastOneDatasourceSelected = false;
		for (DataSource ds : this.allDataSources) {
			if (ds.getEnabled())
				atLeastOneDatasourceSelected = true;
		}
		return atLeastOneDatasourceSelected;
	}
	
	public ArrayList<DataSource> getAllDataSources() {
		return this.allDataSources;
	}

	public void setAllDataSourcesforLauncher(DataSource datasource) {
		this.allDataSources.clear();
		this.allDataSources.add(datasource);
	}
	
	public void refreshDataSources() {
		this.allDataSources.clear();

		DataSourceStorage.getInstance(ctx).fillDefaultDataSources();

		int size = DataSourceStorage.getInstance().getSize();

		// copy the value from shared preference to adapter
		for (int i = 0; i < size; i++) {
			String fields[] = DataSourceStorage.getInstance().getFields(i);
			this.allDataSources.add(new DataSource(fields[0], fields[1],
					fields[2], fields[3], fields[4]));
		}
	}

}
