package org.mixare.mgr.datasource;

import java.util.ArrayList;

import org.mixare.data.DataSource;

public interface DataSourceManager {
	
	boolean isAtLeastOneDatasourceSelected();

	void refreshDataSources();

	void setAllDataSourcesforLauncher(DataSource source);

	ArrayList<DataSource> getAllDataSources();

}
