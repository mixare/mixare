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
package org.mixare;

import java.util.Vector;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MixListView extends ListActivity{
	
	private static int list=0;
	
	private static Vector<String> data = null;
	private static Vector<String> selectedItemURL= null;
	private static MixContext context = null;
	private static DataView dataView = null;
	private static String info= null;
	private static String selectedDataSource="Wikipedia";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		switch(list){
		case 1:
			Vector<String> dataSourceMenu = new Vector();
			dataSourceMenu.add("Wikipedia");
			dataSourceMenu.add("Twitter");
			dataSourceMenu.add("Buzz");
//			dataSourceMenu.add("own URL");
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataSourceMenu));
			getListView().setTextFilterEnabled(true);
			break;
		
		case 2:
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,data));
			getListView().setTextFilterEnabled(true);
			break;
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch(list){
		/*Data Sources*/
		case 1:
			clickOnDataSource(position);		
			break;
		
		/*List View*/
		case 2:
			/*if no website is available for this item*/
			if(selectedItemURL.get(position)==""){				
				Toast.makeText( this, info, Toast.LENGTH_LONG ).show();			
			}
			else{
				String url = selectedItemURL.get(position);
				try {
					if (url != null && url.startsWith("webpage")) {
						String newUrl = MixUtils.parseAction(url);
						dataView.ctx.loadWebPage(newUrl, this);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;

		}
		
	}
	public void clickOnDataSource(int position){
		switch(position){
			/*WIKIPEDIA*/
			case 0:
				setDataSource("Wikipedia");
				Toast.makeText( this , "Changed to Wikipedia as data source", Toast.LENGTH_LONG ).show();
				break;
			
			/*TWITTER*/
			case 1:		
				setDataSource("Twitter");
				Toast.makeText( this ,"Changed to Twitter as data source", Toast.LENGTH_LONG ).show();	
				break;
			/*BUZZ*/
			case 2:
				setDataSource("Buzz");
				Toast.makeText( this ,"Changed to Google Buzz as data source", Toast.LENGTH_LONG).show();
			/*Own URL*/
//			case 2:
//				setDataSource("OwnURL");
//				Toast.makeText( this ,"sdfwer3rh", Toast.LENGTH_LONG ).show();	
//				break;
			
		}
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
			int base = Menu.FIRST;
			/*define the first*/
			MenuItem item1 =menu.add(base, base, base, getString(dataView.MENU_ITEM_3)); 
			MenuItem item2 =menu.add(base, base+1, base+1, getString(dataView.MENU_CAM_MODE));
//			MenuItem item3 =menu.add(base, base+2, base+2, getString(dataView.MAP_MY_LOCATION)); 
//			MenuItem item4 =menu.add(base, base+3, base+3, getString(dataView.MENU_ITEM_2)); 
//			MenuItem item5 =menu.add(base, base+4, base+4, getString(dataView.MAP_MENU_CAM_MODE)); 

			/*assign icons to the menu items*/
			item1.setIcon(android.R.drawable.ic_menu_mapmode);
			item2.setIcon(android.R.drawable.ic_menu_camera);
//			item3.setIcon(android.R.drawable.ic_menu_mylocation);
//			item4.setIcon(android.R.drawable.ic_menu_view);
//			item5.setIcon(android.R.drawable.ic_menu_camera);

			return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			/*Map View*/
			case 1:
				createMixMap();
				finish();
				break;
			/*back to Camera View*/
			case 2:
				finish();
				break;
		}
		return true;
	}
	public void createMixMap(){
		MixMap.setMarkerList(dataView.jLayer.markers);
		MixMap.setDataView(dataView);
		MixMap.setMixContext(dataView.ctx);
		Intent intent2 = new Intent(MixListView.this, MixMap.class); 
		startActivityForResult(intent2, 20);
	}
	
	
	public static void setDataSource(String source){
		selectedDataSource = source;
	}
	public static String getDataSource(){
		return selectedDataSource;
	}
	public static void setInfoText(String i){
		info = i;
	}
	public static void setDataView(DataView v){
		dataView = v;
	}
	public static void setMixContext(MixContext mc){
		context = mc;
	}
	public static void setTitleVector(Vector<String> t){
		data = t;
	}
	public static void setURLVector(Vector<String> url){
		selectedItemURL = url;
	}
	public static void setList(int l){
		list = l;
	}
}




