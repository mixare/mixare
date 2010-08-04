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
import android.os.Bundle;
import android.util.Log;
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
				dataView.state.handleEvent(context, selectedItemURL.get(position));
			}
			finish();
			break;

		}
		finish();
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




