package org.mixare.mgr.datasource;

import java.util.ArrayList;

import org.mixare.data.DataSource;
import org.mixare.data.DataSourceStorage;

import android.content.Context;

public class DataSourceMgrImpl implements DataSourceManager {
	
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
