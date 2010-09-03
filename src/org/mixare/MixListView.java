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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MixListView extends ListActivity{
	
	private static int list=0;
	
	private static Vector<String> listViewMenu = null;
	private static Vector<String> selectedItemURL= null;
	public static Vector<String> dataSourceMenu= null;
	public static Vector<String> dataSourceDescription= null;
	private static MixContext mixCtx = null;
	private static DataView dataView = null;
	private static String selectedDataSource="Wikipedia";
	/*to check which data source is active*/
	private int clickedDataSourceItem = 0;
	private ListItemAdapter adapter=null;
	public static String customizedURL="http://mixare.org/geotest.php";
	private static Context ctx;
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mixCtx = MixView.ctx;
		dataView = MixView.view;	
		ctx = this;

		switch(list){
		case 1:
			dataSourceMenu = new Vector();
			dataSourceMenu.add("Wikipedia");
			dataSourceMenu.add("Twitter");
			dataSourceMenu.add("Buzz");
			dataSourceMenu.add("OpenStreetMap");
			dataSourceMenu.add("own URL");
			
			dataSourceDescription = new Vector();
			dataSourceDescription.add("no change...");
			dataSourceDescription.add("no change...");
			dataSourceDescription.add("no change...");
			dataSourceDescription.add("no change...");
			dataSourceDescription.add("example: http://mixare.org/geotest.php");

			adapter = new ListItemAdapter(this);
			adapter.colorSource(getDataSource());
			getListView().setTextFilterEnabled(true);
			
			setListAdapter(adapter);

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
	
	public static void createContextMenu(ImageView icon){
		
		
		 icon.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {				
				@Override
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					 int index=0;
					 switch(ListItemAdapter.itemPosition){
					 	case 0:
							 menu.setHeaderTitle("Wiki Menu");
							 menu.add(index, index, index, "We are working on it...");			
							 break;
					 	case 1:
					 		 menu.setHeaderTitle("Twitter Menu");
							 menu.add(index, index, index, "We are working on it...");
							 break;
					 	case 2:
					 		 menu.setHeaderTitle("Buzz Menu");
							 menu.add(index, index, index, "We are working on it...");
							 break;
					 	case 3:
					 		 menu.setHeaderTitle("OpenStreetMap Menu");
							 menu.add(index, index, index, "We are working on it...");
							 break;
					 	case 4:
							AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
							alert.setTitle("insert your own URL:");
							
							final EditText input = new EditText(ctx); 
							input.setText(customizedURL);
							alert.setView(input);
							
							alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					        	public void onClick(DialogInterface dialog, int id) {       		
					        		Editable value = input.getText();
					        		customizedURL = ""+value;
					            }
					        });
							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					        	public void onClick(DialogInterface dialog, int id) {       		
					        		dialog.dismiss();
					            }
					        });
						    alert.show();
					 		break;
					 }
				}
	        });

	}

	
	public void clickOnDataSource(int position){
		switch(position){
			/*WIKIPEDIA*/
			case 0:
				setDataSource("Wikipedia");
				adapter.colorSource(getDataSource());
				setListAdapter(adapter);
				break;
			
			/*TWITTER*/
			case 1:		
				setDataSource("Twitter");
				adapter.colorSource(getDataSource());
				setListAdapter(adapter);
				break;

			/*BUZZ*/
			case 2:
				setDataSource("Buzz");
				adapter.colorSource(getDataSource());
				setListAdapter(adapter);
				break;
				
			/*OSM*/
			case 3:
				setDataSource("OpenStreetMap");
				adapter.colorSource(getDataSource());
				setListAdapter(adapter);
				break;
				
			/*Own URL*/
			case 4:
				setDataSource("OwnURL");
				adapter.colorSource(getDataSource());
				setListAdapter(adapter);
				break;
		}
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
				break;
			case 2: 
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
	static ViewHolder holder;
	private int[] bgcolors = new int[] {0,0,0,0,0};
	private int[] textcolors = new int[] {Color.WHITE,Color.WHITE,Color.WHITE,Color.WHITE,Color.WHITE};
	private int[] descriptioncolors = new int[] {Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY,Color.GRAY};

	public static boolean icon_clicked = false;

	public static int itemPosition =0;

	public ListItemAdapter(Context context) {
		myInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		itemPosition =position;
        if(convertView==null){
	        convertView = myInflater.inflate(R.layout.main, null);
	
	        holder = new ViewHolder();
	        holder.text = (TextView) convertView.findViewById(R.id.list_text);
	        holder.description = (TextView) convertView.findViewById(R.id.description_text);

	        holder.icon = (ImageView) convertView.findViewById(R.id.icon);
	        convertView.setTag(holder);
        }
        else{
        	holder = (ViewHolder) convertView.getTag();
        }
       
        holder.icon.setPadding(20, 8, 20, 8);
        holder.icon.setClickable(true);        
        
        holder.icon.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				icon_clicked = true;
				itemPosition = position;
		
				return false;
			}
		});
        
		MixListView.createContextMenu(holder.icon);

        holder.text.setPadding(20, 8, 0, 0);
        holder.description.setPadding(20, 40, 0, 0);

        holder.text.setText(MixListView.dataSourceMenu.get(position));
        holder.description.setText(MixListView.dataSourceDescription.get(position));
        
        int colorPos = position % bgcolors.length;
     	convertView.setBackgroundColor(bgcolors[colorPos]);
     	holder.text.setTextColor(textcolors[colorPos]);
     	holder.description.setTextColor(descriptioncolors[colorPos]);
    	    	
        return convertView;
    }

	public void changeColor(int index, int bgcolor, int textcolor){
		if(index<bgcolors.length){
				bgcolors[index]=bgcolor;
				textcolors[index]= textcolor;
		}
		else {
				Log.d("Color Error", "too large index");
		}
	}
	
	public void colorSource(String source){
		for (int i = 0; i < bgcolors.length; i++) {
			bgcolors[i]=0;
			textcolors[i]=Color.WHITE;
		}
		if(source.equals("Wikipedia"))
			changeColor(0, Color.WHITE, Color.DKGRAY);
		if(source.equals("Twitter"))
			changeColor(1, Color.WHITE, Color.DKGRAY);
		if(source.equals("Buzz"))
			changeColor(2, Color.WHITE, Color.DKGRAY);
		if(source.equals("OpenStreetMap"))
			changeColor(3, Color.WHITE, Color.DKGRAY);
		if(source.equals("OwnURL"))
			changeColor(4, Color.WHITE, Color.DKGRAY);
	}
	
	@Override
	public int getCount() {
		return MixListView.dataSourceMenu.size();
	}

	@Override
	public Object getItem(int position) {
		return this;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
    private class ViewHolder {
		TextView text;
		TextView description;
        ImageView icon;
    }
}