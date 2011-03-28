package org.mixare;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Editable;
import android.util.Log;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/*
 * List user OpenStreetMap URL, add, delete the URL*/

public class OSMDataSource extends ListActivity {

	public static final String SHARED_PREFS = "MyPrefsFileForOSMUrl";
	private MyOSMAdapter osmAdapter;
	public final static int ADD_MENU = 0;
	public final static int SET_MAX_OBJECT = 4;
	private MenuItem m1;
	private String editStr = "";

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		
		
		SharedPreferences settings = getSharedPreferences(
				OSMDataSource.SHARED_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();

		
		
		/*
		 * editor.clear(); editor.commit();
		 */
		//if nothing inside the Shared preference
		/*if (settings.getAll().isEmpty()) {
			editor.putString("URLStr0",
					"http://geometa.hsr.ch/xapi/api/0.6/node[indoor=yes]");
			editor.putBoolean("URLBool0", true);
			editor.commit();
		}*/
		int size = settings.getAll().size();
		//copy the value from shared preference to adapter
		osmAdapter = new MyOSMAdapter();
		for (int i = 0; i < (size / 2); i++) {
			String s = settings.getString("URLStr" + i, "");
			Boolean b = settings.getBoolean("URLBool" + i, false);
			osmAdapter.addItem(new osmURL(s, b));// to hold the string
		}
		setListAdapter(osmAdapter);
		ListView lv = getListView();
		registerForContextMenu(lv);
	}

	private class MyOSMAdapter extends BaseAdapter implements
			OnCheckedChangeListener {

		private List<osmURL> mData = new ArrayList<osmURL>();
		private LayoutInflater mInflater;

		public MyOSMAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void addItem(final osmURL item) {
			mData.add(item);
			notifyDataSetChanged();
		}

		public void deleteItem(final int id) {
			mData.remove(id);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		public String getItemName(int position) {
			return mData.get(position).getName();
		}

		public Boolean getItemStatus(int position) {
			return mData.get(position).getStatus();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void updateItemName(int position, String s) {
			mData.get(position).setName(s);
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.osm, null);
				holder = new ViewHolder();
				holder.textView = (TextView) convertView
						.findViewById(R.id.text);
				holder.checkbox = (CheckBox) convertView.findViewById(R.id.cb);
				holder.checkbox.setTag(position); // setting listener for
													// checkbox in a row
				//  set listeners for any object we get here
				holder.checkbox.setOnCheckedChangeListener(this);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.textView.setText(mData.get(position).getName());
			holder.checkbox.setChecked(mData.get(position).getStatus());

			return convertView;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			int position = (Integer) buttonView.getTag();
			if (isChecked) {
				buttonView.setChecked(true);
			} else {
				buttonView.setChecked(false);
			}
			mData.get(position).setSatus(isChecked);
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	//class to hold URL and it's status
	public class osmURL {
		private String mName;
		private boolean mStatus;

		public osmURL(String name, boolean status) {
			mName = name;
			mStatus = status;
		}

		public String getName() {
			return mName;
		}

		public boolean getStatus() {
			return mStatus;
		}

		public void setName(String iName) {
			mName = iName;
		}

		public void setSatus(boolean iStatus) {
			mStatus = iStatus;
		}
	}

	public static class ViewHolder {
		public TextView textView;
		public CheckBox checkbox;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case ADD_MENU:
			AlertDialog.Builder osmAlert = new AlertDialog.Builder(this);
			osmAlert.setTitle(R.string.insert_osm_url);

			final EditText inputOSM = new EditText(this);
			inputOSM.setText("");
			osmAlert.setView(inputOSM);

			osmAlert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Editable value = inputOSM.getText();
							String inputStr = "" + value;
							addURL(inputStr);

						}
					});
			osmAlert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			osmAlert.show();

			break;
		case SET_MAX_OBJECT://id 1
			AlertDialog.Builder maxObjectAlert = new AlertDialog.Builder(this);
			maxObjectAlert.setTitle(R.string.set_osm_max_object);

			final EditText maxObject = new EditText(this);
			SharedPreferences mixViewSetting = getSharedPreferences(
					MixView.PREFS_NAME, 0);
			int no = mixViewSetting.getInt("osmMaxObject", 5);
			maxObject.setText(Integer.toString(no));
			maxObjectAlert.setView(maxObject);

			maxObjectAlert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Editable value = maxObject.getText();
							String inputStr = "" + value;
							editMaxObject(Integer.parseInt(inputStr));


						}
					});
			maxObjectAlert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			maxObjectAlert.show();
			break;
		default:
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		m1 = menu.add(0, ADD_MENU, 0, R.string.add_osm_menu);
		menu.add(1, SET_MAX_OBJECT, 1, R.string.osm_max_object_menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void addURL(String inputStr) {
		if (inputStr != null) {
			osmAdapter.addItem(new osmURL(inputStr, true));
			setListAdapter(osmAdapter);
		}
	}

	public void deleteURL(int id) {
		osmAdapter.deleteItem(id);// remove from baseadapter
		setListAdapter(osmAdapter);
	}

	public void editURL(int id, String finalUrl) {
		osmAdapter.updateItemName(id, finalUrl); //update from base adapter
		setListAdapter(osmAdapter);
	}

	public void editMaxObject(int newVal){
		SharedPreferences mixViewSetting = getSharedPreferences(
				MixView.PREFS_NAME, 0);
		SharedPreferences.Editor mixVieweditor = mixViewSetting.edit();
		mixVieweditor.putInt("osmMaxObject", newVal);
		mixVieweditor.commit();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e) {
			Log.e("OSMDataSource", "bad menuInfo", e);
			return;
		}
		long id = getListAdapter().getItemId(info.position);
		int base = Menu.FIRST;
		menu.add(base, base, base, "Edit"); //1
		menu.add(base, base + 1, base + 1, "Copy");//2
		menu.add(base, base + 2, base + 2, "Delete");//3
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		AdapterView.AdapterContextMenuInfo info;
		try {
			info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		} catch (ClassCastException e) {
			Log.e("OSMDataSource", "bad menuInfo", e);
			return false;
		}
		final long idOfMenu = getListAdapter().getItemId(info.position);
		switch (item.getItemId()) {
		case 1:

			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("edit your own URL:");

			final EditText input = new EditText(this);
			String s = osmAdapter.getItemName((int) idOfMenu);
			input.setText(s);
			alert.setView(input);

			alert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Editable value = input.getText();
							editStr = "" + value;
							editURL((int) idOfMenu, editStr);
						}
					});
			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
			alert.show();

			break;
		case 2: 
			//get the string value
			String strVal=osmAdapter.getItemName((int) idOfMenu);
			//get the system clipboard
			ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setText(strVal);
			break;
		case 3:
			deleteURL((int) idOfMenu);
			break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		SharedPreferences settings = getSharedPreferences(
				OSMDataSource.SHARED_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.clear();
		// COMMIT CHANGES IN THE SHARED PREFERENCE
		for (int k = 0; k < osmAdapter.getCount(); k++) {
			editor.putString("URLStr" + k, osmAdapter.getItemName(k));// the URL
			editor.putBoolean("URLBool" + k, osmAdapter.getItemStatus(k));// the
																			// Boolean
																			// status
		}
		editor.commit();
	}

	


}
