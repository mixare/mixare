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
	
	private boolean customDataSourceSelected = false;
		
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
	
	public static DataSourceStorage getInstance(Context ctx) {
		if(instance == null){
			instance = new DataSourceStorage(ctx);
		}
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
		if(datasources.length > getSize()){
			for(int i = 0; i < datasources.length; i++){
				int id = getSize();
				add("DataSource"+ id, datasources[i]);
				onCustomDataSourceSelected(id);
			}
		}
	}
	
	public String[] getFields(int i){
		return settings.getString("DataSource"+i, "").split("\\|", -1);
	}
	
	public int getSize(){
		return settings.getAll().size();
	}
	
	public void setCustomDataSourceSelected(boolean customDataSourceSelected){
		this.customDataSourceSelected = customDataSourceSelected;
	}
		
	private void onCustomDataSourceSelected(int id) {
		// if a custom data source is selected, then hide the datasources
		editVisibility(id, !customDataSourceSelected);
	}
}
