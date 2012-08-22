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

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DataSourceList extends SherlockListActivity {
	
	public static final String SHARED_PREFS = "DataSourcesPrefs";
	private static DataSourceAdapter dataSourceAdapter;

	private static final int MENU_CREATE_ID = Menu.FIRST;
	private static final int MENU_EDIT_ID = Menu.FIRST + 1;
	private static final int MENU_DELETE_ID = Menu.FIRST + 2;
	private static final int MENU_MORE_ID = Menu.FIRST + 3;
	private static final int MENU_RESTORE_ID = Menu.FIRST + 4;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();

		int size = DataSourceStorage.getInstance().getSize();
		
		// copy the value from shared preference to adapter
		dataSourceAdapter = new DataSourceAdapter();
		for (int i = 0; i < size; i++) {
			dataSourceAdapter.addItem(DataSourceStorage.getInstance().getDataSource(i));
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
		int size = dataSourceAdapter.getCount();
		for (int k = 0; k < size; k++) {
			DataSourceStorage.getInstance().add((DataSource) dataSourceAdapter.getItem(k));
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

		public void addItem(final DataSource item) {
			mDataSource.add(item);
			notifyDataSetChanged();
		}

		public void deleteItem(final int id) {
			if (mDataSource.get(id).getEnabled()) {
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

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.datasourcelist, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView
						.findViewById(R.id.list_text);
				holder.description = (TextView) convertView
						.findViewById(R.id.description_text);
				holder.checkbox = (CheckBox) convertView
						.findViewById(R.id.list_checkbox);
				holder.checkbox.setTag(position);
				holder.checkbox.setOnCheckedChangeListener(this);
				holder.datasource_icon = (ImageView) convertView
						.findViewById(R.id.datasource_icon);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			DataSource ds = mDataSource.get(position);
			if (ds != null) {
				holder.text.setText(ds.getName());
				holder.description.setText(ds.getUrl());

				holder.datasource_icon.setImageResource(ds.getDataSourceIcon());
				holder.checkbox.setChecked(ds.getEnabled());
			} else {
				Log.d("test", position + "");
			}
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

		private class ViewHolder {
			TextView text;
			TextView description;
			CheckBox checkbox;
			ImageView datasource_icon;
		}

		@Override
		public Object getItem(int index) {
			return mDataSource.get(index);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		menu.add(MENU_CREATE_ID, MENU_CREATE_ID, Menu.NONE, "Add")
				.setIcon(R.drawable.ic_compose)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_ALWAYS);

		SubMenu subMenu1 = menu.addSubMenu("More");
		subMenu1.add(MENU_RESTORE_ID, MENU_RESTORE_ID, Menu.NONE, "Restore Default");

		MenuItem subMenu1Item = subMenu1.getItem();
		subMenu1Item.setIcon(R.drawable.abs__ic_menu_moreoverflow_holo_dark);
		subMenu1Item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_MENU){
			
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case MENU_CREATE_ID:
			Intent addSource = new Intent(DataSourceList.this,
					AddDataSource.class);
			startActivity(addSource);
			break;
		case MENU_RESTORE_ID:
			// TODO: Restore Default Sources
			break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		menu.add(MENU_EDIT_ID, MENU_EDIT_ID, MENU_EDIT_ID,
				R.string.data_source_edit);
		menu.add(MENU_DELETE_ID, MENU_DELETE_ID, MENU_DELETE_ID,
				R.string.data_source_delete);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			return false;
		}
		final long idOfMenu = getListAdapter().getItemId(info.position);
		DataSource ds = (DataSource) dataSourceAdapter.getItem((int) idOfMenu);
		switch (item.getItemId()) {
		case MENU_EDIT_ID:
			if (ds.isEditable()) {
				Intent editDataSource = new Intent(this, AddDataSource.class);
				editDataSource.putExtra("DataSourceId", (int) idOfMenu);
				startActivity(editDataSource);
			} else {
				Toast.makeText(this, getString(R.string.data_source_edit_err), Toast.LENGTH_SHORT).show();
			}
			break;
		case MENU_DELETE_ID:
			if (ds.isEditable()) {
				dataSourceAdapter.deleteItem((int) idOfMenu);
			} else {
				Toast.makeText(this, getString(R.string.data_source_delete_err), Toast.LENGTH_SHORT).show();
			}
			break;
		}
		return super.onContextItemSelected(item);
	}

}
