package org.mixare.data;

import org.mixare.R;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton slass that manages the storage of datasources
 * You can add, edit or delete a datasource through this class.
 */
public class DataSourceStorage {

	private SharedPreferences settings;
	
	private Context ctx;
	
	public static DataSourceStorage instance;
		
	public DataSourceStorage(Context ctx){
		this.ctx = ctx;
		settings = ctx.getSharedPreferences(DataSourceList.SHARED_PREFS, 0);
	}
	
	public static void init(Context ctx){
		instance = new DataSourceStorage(ctx);
	}
	
	public static DataSourceStorage getInstance() {
		return instance;
	}
	
	public void add(String name, String url, String type, String display, boolean visible) {
		SharedPreferences.Editor dataSourceEditor = settings.edit();
		dataSourceEditor.putString("DataSource"+getSize(), 
				name +"|"+ url +"|"+ type +"|"+ display +"|"+ String.valueOf(visible));
		dataSourceEditor.commit();
	}
	
	public void add(String id, String serialized){
		SharedPreferences.Editor dataSourceEditor = settings.edit();
		dataSourceEditor.putString(id, serialized);
		dataSourceEditor.commit();
	}
	
	public void clear(){
		SharedPreferences.Editor dataSourceEditor = settings.edit();
		dataSourceEditor.clear();
		dataSourceEditor.commit();
	}
	
	public void editVisibility(int i, boolean visible){
		String[] fields = getFields(i);
		if(fields.length == 5){
			fields[4] = String.valueOf(visible);
		
			SharedPreferences.Editor dataSourceEditor = settings.edit();
			dataSourceEditor.putString("DataSource"+i, 
					fields[0] +"|"+ fields[1] +"|"+ fields[2] +"|"+ fields[3] +"|"+ fields[4]);
			dataSourceEditor.commit();
		}
	}
	
	public void fillDefaultDataSources(){
		String[] datasources = ctx.getResources().getStringArray(R.array.defaultdatasources);
		for(int i = 0; i < datasources.length; i++){
			add("DataSource"+i, datasources[i]);
		}
	}
	
	public String[] getFields(int i){
		return settings.getString("DataSource"+i, "").split("\\|", -1);
	}
	
	public int getSize(){
		return settings.getAll().size();
	}
	
}
