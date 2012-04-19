/*
 * Copyright (C) 2010- Peer internet solutions
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

import java.util.ArrayList;
import java.util.List;

import org.mixare.R;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DataSourceList extends ListActivity {
	
	public static final String SHARED_PREFS = "DataSourcesPrefs";
	private static DataSourceAdapter dataSourceAdapter;

	private static final int MENU_CREATE_ID = Menu.FIRST;
	private static final int MENU_EDIT_ID = Menu.FIRST + 1;
	private static final int MENU_DELETE_ID = Menu.FIRST + 2;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onResume() {
		super.onResume();

		DataSourceStorage.getInstance().fillDefaultDataSources();

		int size = DataSourceStorage.getInstance().getSize();
		
		// copy the value from shared preference to adapter
		dataSourceAdapter = new DataSourceAdapter();
		for (int i = 0; i < size; i++) {
			String fields[] = DataSourceStorage.getInstance().getFields(i);
			dataSourceAdapter.addItem(new DataSource(fields[0], fields[1], fields[2], fields[3], fields[4]));
		}
		setListAdapter(dataSourceAdapter);
		ListView lv = getListView();
		registerForContextMenu(lv);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		DataSourceStorage.getInstance().clear();
		//every URL in Adapter 
		//put the URL link and status inside the Shared Preference
		for (int k = 0; k < dataSourceAdapter.getCount(); k++) {
			DataSourceStorage.getInstance().add("DataSource" + k, dataSourceAdapter.serialize(k));
		}
	}

	//TODO: check if it's really needed
	public static String getDataSourcesStringList() {
		String ret="";
		boolean first=true;

		for(int i = 0; i < dataSourceAdapter.getCount(); i++) {
			if(dataSourceAdapter.getItemEnabled(i)) {
				if(!first) {
					ret+=", ";
				}	
				ret+=dataSourceAdapter.getItemName(i);
				first=false;
			}
		}

		return ret;
	}
	private class DataSourceAdapter extends BaseAdapter implements
	OnCheckedChangeListener {

		private List<DataSource> mDataSource = new ArrayList<DataSource>();
		private LayoutInflater mInflater;

		public DataSourceAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public boolean getItemEnabled(int k) {
			return mDataSource.get(k).getEnabled();
		}

		public String getItemName(int k) {
			return mDataSource.get(k).getName();

		}

		public String serialize(int k) {
			return mDataSource.get(k).serialize();
		}
		public void addItem(final DataSource item) {
			mDataSource.add(item);
			notifyDataSetChanged();
		}

		public void deleteItem(final int id) {
			if(mDataSource.get(id).getEnabled()) {
				mDataSource.get(id).setEnabled(false);
				notifyDataSetChanged();
			}
			mDataSource.remove(id);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mDataSource.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (convertView==null) {
				convertView = mInflater.inflate(R.layout.datasourcelist, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.list_text);
				holder.description = (TextView) convertView.findViewById(R.id.description_text);
				holder.checkbox = (CheckBox) convertView.findViewById(R.id.list_checkbox);
				holder.checkbox.setTag(position);
				holder.checkbox.setOnCheckedChangeListener(this);
				holder.datasource_icon = (ImageView) convertView.findViewById(R.id.datasource_icon);

				convertView.setTag(holder);
			}
			else{
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(mDataSource.get(position).getName());
			holder.description.setText(mDataSource.get(position).getUrl());

			holder.datasource_icon.setImageResource(mDataSource.get(position).getDataSourceIcon());
			holder.checkbox.setChecked(mDataSource.get(position).getEnabled());

			return convertView;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int position = (Integer) buttonView.getTag();
			if (isChecked) {
				buttonView.setChecked(true);
			} else {
				buttonView.setChecked(false);
			}
			mDataSource.get(position).setEnabled(isChecked);
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		private class ViewHolder {
			TextView text;
			TextView description;
			CheckBox checkbox;
			ImageView datasource_icon;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(MENU_CREATE_ID, MENU_CREATE_ID, MENU_CREATE_ID, R.string.data_source_add);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item){
		switch(item.getItemId()){
		case MENU_CREATE_ID:
			Intent addDataSource = new Intent(this, DataSource.class);
			startActivity(addDataSource);
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		menu.add(MENU_EDIT_ID, MENU_EDIT_ID, MENU_EDIT_ID, R.string.data_source_edit); 
		menu.add(MENU_DELETE_ID, MENU_DELETE_ID, MENU_DELETE_ID, R.string.data_source_delete);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}
		final long idOfMenu = getListAdapter().getItemId(info.position);
		switch (item.getItemId()) {
		case MENU_EDIT_ID:
			if (idOfMenu <= 3) {
				Toast.makeText(this, getString(R.string.data_source_edit_err), Toast.LENGTH_SHORT).show();
			} else {
				Intent editDataSource = new Intent(this, DataSource.class);
				editDataSource.putExtra("DataSourceId", (int) idOfMenu);
				startActivity(editDataSource);
			}
			break;
		case MENU_DELETE_ID:
			if (idOfMenu <= 3) {
				Toast.makeText(this, getString(R.string.data_source_delete_err), Toast.LENGTH_SHORT).show();
			} else {
				dataSourceAdapter.deleteItem((int) idOfMenu);
			}
			break;
		}
		return super.onContextItemSelected(item);
	}

}
