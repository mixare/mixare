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

import android.R.color;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MixListView extends ListActivity{
	
	private static int list=0;
	
	private static Vector<String> listViewMenu = null;
	private static Vector<String> selectedItemURL= null;
	public static Vector<String> dataSourceMenu= null;
	private static MixContext context = null;
	private static DataView dataView = null;
	private static String selectedDataSource="Wikipedia";
	/*to check which data source is active*/
	private int clickedDataSourceItem = 0;
	private ListItemAdapter adapter=null;



	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		context = MixView.ctx;
		dataView = MixView.view;		

		switch(list){
		case 1:
			dataSourceMenu = new Vector();
			dataSourceMenu.add("Wikipedia");
			dataSourceMenu.add("Twitter");
			dataSourceMenu.add("Buzz");
			dataSourceMenu.add("OpenStreetMap");
//			dataSourceMenu.add("own URL");
			
			//getListView().setBackgroundResource()(Color.WHITE);
//			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice,dataSourceMenu));
//			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//			getListView().setTextFilterEnabled(true);
//			getListView().setBackgroundColor(Color.WHITE);
//
			/*check which Data Source is active and set radio button*/
			
//			if(getDataSource().equals("Wikipedia"))
//				adapter.changeColor(0, Color.DKGRAY);
//			if(getDataSource().equals("Twitter"))
//				adapter.changeColor(1, Color.DKGRAY);
//			if(getDataSource().equals("Buzz"))
//				adapter.changeColor(2, Color.DKGRAY);
//			if(getDataSource().equals("OpenStreetMap"))
//				adapter.changeColor(3, Color.DKGRAY);
//			
//			getListView().setItemChecked(clickedDataSourceItem, true);
			adapter = new ListItemAdapter(this);
			adapter.colorSource(getDataSource());
			setListAdapter(adapter);
			getListView().setTextFilterEnabled(true);
			
			getListView().setOnCreateContextMenuListener(new OnCreateContextMenuListener() {							
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					 int index=0;
					 menu.setHeaderTitle("Twitter Menu");
					 MenuItem item1 = menu.add(index, index, index, "All infos");
					 MenuItem item2 = menu.add(index, index+1, index+1, "Followers");
					 MenuItem item3 = menu.add(index, index+2, index+2, "Groups");
					 MenuItem item4 = menu.add(index, index+3, index+3, "Log IN");
					
				}});
			break;
		
		case 2:
			selectedItemURL = new Vector();
			listViewMenu = new Vector();
			/*add all marker items to a title and a URL Vector*/
			for(int i = 0; i<dataView.jLayer.markers.size();i++){
				Marker ma = new Marker();
				ma = dataView.jLayer.markers.get(i);
				listViewMenu.add(ma.getText());
					/*the website for the corresponding title*/
					if(ma.getURL()!=null)
						selectedItemURL.add(ma.getURL());
					/*if no website is available for a specific title*/
					else
						selectedItemURL.add("");
			}
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listViewMenu));
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
				clickOnListView(position);
				break;
		}
		
	}
	public void clickOnListView(int position){
		/*if no website is available for this item*/
		if(selectedItemURL.get(position)==""){				
			Toast.makeText( this, getString(dataView.NO_WEBINFO_AVAILABLE), Toast.LENGTH_LONG ).show();			
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
	}
	
	public void clickOnDataSource(int position){
		switch(position){
			/*WIKIPEDIA*/
			case 0:
				setDataSource("Wikipedia");
				adapter.colorSource(getDataSource());
				setListAdapter(adapter);
				//Toast.makeText( this , getString(dataView.DATA_SOURCE_CHANGE_WIKIPEDIA), Toast.LENGTH_LONG ).show();
				break;
			
			/*TWITTER*/
			case 1:		
				setDataSource("Twitter");
				adapter.colorSource(getDataSource());
				setListAdapter(adapter);
				//Toast.makeText( this ,getString(dataView.DATA_SOURCE_CHANGE_TWITTER), Toast.LENGTH_LONG ).show();	
				break;

			/*BUZZ*/
			case 2:
				setDataSource("Buzz");
				adapter.colorSource(getDataSource());
				setListAdapter(adapter);
				//Toast.makeText( this ,getString(dataView.DATA_SOURCE_CHANGE_BUZZ), Toast.LENGTH_LONG).show();
				break;
				
			/*OSM*/
			case 3:
				setDataSource("OpenStreetMap");
				adapter.colorSource(getDataSource());
				setListAdapter(adapter);
				//Toast.makeText( this ,getString(dataView.DATA_SOURCE_CHANGE_OSM), Toast.LENGTH_LONG).show();
				break;
				
			/*Own URL*/
//			case 3:
//				setDataSource("OwnURL");
//				Toast.makeText( this ,"sdfwer3rh", Toast.LENGTH_LONG ).show();	
//				break;
			
		}
	//	finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
			int base = Menu.FIRST;
			
			/*define menu items*/
			MenuItem item1 = menu.add(base, base, base, getString(dataView.MENU_ITEM_3)); 
			MenuItem item2 = menu.add(base, base+1, base+1, getString(dataView.MENU_CAM_MODE));

			/*assign icons to the menu items*/
			item1.setIcon(android.R.drawable.ic_menu_mapmode);
			item2.setIcon(android.R.drawable.ic_menu_camera);
			
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
	@Override
    public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case 1: 
				finish();
				break;
			case 2: 
				finish();
				break;
		}
		return false;
	}
		
	
	public void createMixMap(){
		Intent intent2 = new Intent(MixListView.this, MixMap.class); 
		startActivityForResult(intent2, 20);
	}
	
	public static void setDataSource(String source){
		selectedDataSource = source;
	}
	
	public static String getDataSource(){
		return selectedDataSource;
	}
	
	public static void setList(int l){
		list = l;
	}
}

class ListItemAdapter extends BaseAdapter {
	private LayoutInflater myInflater;
	private Bitmap edit_icon;
	private ViewHolder holder;
	private View row;
	//private int[] colors = new int[] { 0x30FF0000, 0x300000FF,0x3000FF00, 0x000FFFF0 };
	private int[] colors = new int[] {0,0,0,0};
	//private View view=null;
	public ListItemAdapter(Context context) {
		myInflater = LayoutInflater.from(context);
		//edit_icon = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_edit);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
        if(convertView==null){
	        convertView = myInflater.inflate(R.layout.main, null);
	
	        holder = new ViewHolder();
	        holder.text = (TextView) convertView.findViewById(R.id.list_text);
	        	        
	        holder.icon = (ImageView) convertView.findViewById(R.id.icon);
	        convertView.setTag(holder); 
        }
        else{
        	holder = (ViewHolder) convertView.getTag();
        }
       
        holder.icon.setPadding(20, 8, 20, 8);
        holder.icon.setClickable(true);
//        holder.icon.setOnClickListener(new View.OnClickListener() {
//        	public void onClick(View v) {
//        		holder.icon;
//        	}
//        });
        holder.text.setPadding(20, 18, 0, 0);
        holder.text.setText(MixListView.dataSourceMenu.get(position));
        int colorPos = position % colors.length;
     	convertView.setBackgroundColor(colors[colorPos]);
    	row = convertView;
    	
        return convertView;
    }
	public void changeColor(int index, int color){
		if(index<colors.length)
				colors[index]=color;
		else {
				colors[index]=0;
				Log.d("Color Error", "too large index");
		}
	}
	public void colorSource(String source){
		for (int i = 0; i < colors.length; i++) {
			colors[i]=0;
		}
		if(source.equals("Wikipedia"))
			changeColor(0, Color.DKGRAY);
		if(source.equals("Twitter"))
			changeColor(1, Color.DKGRAY);
		if(source.equals("Buzz"))
			changeColor(2, Color.DKGRAY);
		if(source.equals("OpenStreetMap"))
			changeColor(3, Color.DKGRAY);
	}
	
	public ImageView getIcon(){
		return holder.icon;
	}

//    public View getView(){
//    	return view;
//    }

	@Override
	public int getCount() {
		return MixListView.dataSourceMenu.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int id) {
		return id;
	}
	
    private class ViewHolder {
		TextView text;
        ImageView icon;
    }

}