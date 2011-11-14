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

/**
 * This class is the main application which uses the other classes for different
 * functionalities.
 * It sets up the camera screen and the augmented screen which is in front of the
 * camera screen.
 * It also handles the main sensor events, touch events and location events.
 */

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.mixare.R.drawable;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSourceList;
import org.mixare.gui.PaintScreen;
import org.mixare.render.Matrix;
import org.mixare.reality.LowPassFilter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MixView extends Activity implements SensorEventListener, OnTouchListener{

	private CameraSurface camScreen;
	private AugmentedView augScreen;

	private boolean isInited;
	private MixContext mixContext;
	static PaintScreen dWindow;
	static DataView dataView;
	private Thread downloadThread;

	private float RTmp[] = new float[9];
	private float Rot[] = new float[9];
	private float I[] = new float[9];
	private float grav[] = new float[3];
	private float mag[] = new float[3];

	private SensorManager sensorMgr;
	private List<Sensor> sensors;
	private Sensor sensorGrav, sensorMag;

	private Matrix tempR = new Matrix();
	private Matrix finalR = new Matrix();
	private Matrix histR[] = new Matrix[60];
	private Matrix m1 = new Matrix();
	private Matrix m2 = new Matrix();
	private Matrix m3 = new Matrix();
	private Matrix m4 = new Matrix();

	private SeekBar myZoomBar;
	private WakeLock mWakeLock;

	private boolean fError;

	private int compassErrorDisplayed = 0;

	private String zoomLevel;
	private int zoomProgress;

	private TextView searchNotificationTxt;
  /* low pass filter object for accelerometer */
  LowPassFilter lpf_acc;
  /* low pass filter object for magnet */
  LowPassFilter lpf_mgnt;

	//TAG for logging
	public static final String TAG = "Mixare";

	/*string to name & access the preference file in the internal storage*/
	public static final String PREFS_NAME = "MyPrefsFileForMenuItems";

	public boolean isZoombarVisible() {
		return myZoomBar != null && myZoomBar.getVisibility() == View.VISIBLE;
	}

	public String getZoomLevel() {
		return zoomLevel;
	}

	public int getZoomProgress() {
		return zoomProgress;
	}

	public void doError(Exception ex1) {
		if (!fError) {
			fError = true;

			setErrorDialog();

			ex1.printStackTrace();
			try {
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}

		try {
			augScreen.invalidate();
		} catch (Exception ignore) {
		}
	}

	public void killOnError() throws Exception {
		if (fError)
			throw new Exception();
	}

	public void repaint() {
		dataView = new DataView(mixContext);
		dWindow = new PaintScreen();
		setZoomLevel();
	}

	public void setErrorDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(DataView.CONNECTION_ERROR_DIALOG_TEXT));
		builder.setCancelable(false);

		/*Retry*/
		builder.setPositiveButton(DataView.CONNECTION_ERROR_DIALOG_BUTTON1, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				fError=false;
				//TODO improve
				try {
					repaint();	       		
				}
				catch(Exception ex){
					//Don't call doError, it will be a recursive call.
					//doError(ex);
				}
			}
		});
		/*Open settings*/
		builder.setNeutralButton(DataView.CONNECTION_ERROR_DIALOG_BUTTON2, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Intent intent1 = new Intent(Settings.ACTION_WIRELESS_SETTINGS); 
				startActivityForResult(intent1, 42);
			}
		});
		/*Close application*/
		builder.setNegativeButton(DataView.CONNECTION_ERROR_DIALOG_BUTTON3, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				System.exit(0);
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {


      /* initializing the low pass filter objects */
      lpf_acc = new LowPassFilter();
      lpf_mgnt = new LowPassFilter();

			handleIntent(getIntent());

			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");

			killOnError();
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			/*Get the preference file PREFS_NAME stored in the internal memory of the phone*/
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();

			SharedPreferences DataSourceSettings = getSharedPreferences(DataSourceList.SHARED_PREFS, 0);
			
			myZoomBar = new SeekBar(this);
			myZoomBar.setVisibility(View.INVISIBLE);
			myZoomBar.setMax(100);
			myZoomBar.setProgress(settings.getInt("zoomLevel", 65));
			myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
			myZoomBar.setVisibility(View.INVISIBLE);			

			FrameLayout frameLayout = new FrameLayout(this);

			frameLayout.setMinimumWidth(3000);
			frameLayout.addView(myZoomBar);
			frameLayout.setPadding(10, 0, 10, 10);

			camScreen = new CameraSurface(this);
			augScreen = new AugmentedView(this);
			setContentView(camScreen);

			addContentView(augScreen, new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			addContentView(frameLayout, new FrameLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM));

			if (!isInited) {
				mixContext = new MixContext(this);
				mixContext.downloadManager = new DownloadManager(mixContext);
				dWindow = new PaintScreen();
				dataView = new DataView(mixContext);

				/*set the radius in data view to the last selected by the user*/
				setZoomLevel(); 
				isInited = true;		
			}

			/*check if the application is launched for the first time*/
			if(settings.getBoolean("firstAccess",false)==false){
				AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
				builder1.setMessage(getString(DataView.LICENSE_TEXT));
				builder1.setNegativeButton(getString(DataView.CLOSE_BUTTON), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
				AlertDialog alert1 = builder1.create();
				alert1.setTitle(getString(DataView.LICENSE_TITLE));
				alert1.show();
				editor.putBoolean("firstAccess", true);

				//value for maximum POI for each selected OSM URL to be active by default is 5
				editor.putInt("osmMaxObject",5);
				editor.commit();

				//add the default datasources to the preferences file
				SharedPreferences.Editor dataSourceEditor = DataSourceSettings.edit();
				dataSourceEditor.putString("DataSource0", "Wikipedia|http://ws.geonames.org/findNearbyWikipediaJSON|0|0|true");
				dataSourceEditor.putString("DataSource1", "Twitter|http://search.twitter.com/search.json|2|0|true");
				dataSourceEditor.putString("DataSource2", "Buzz|https://www.googleapis.com/buzz/v1/activities/search?alt=json&max-results=20|1|0|true");
				dataSourceEditor.putString("DataSource3", "OpenStreetmap|http://open.mapquestapi.com/xapi/api/0.6/node[railway=station]|3|1|true");
				dataSourceEditor.putString("DataSource4", "Own URL|http://mixare.org/geotest.php|4|0|false");
				dataSourceEditor.commit();

			} 

		} catch (Exception ex) {
			doError(ex);
		}
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMixSearch(query);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void doMixSearch(String query) {
		DataHandler jLayer = dataView.getDataHandler();
		if(!dataView.isFrozen()){
			MixListView.originalMarkerList = jLayer.getMarkerList();
			MixMap.originalMarkerList = jLayer.getMarkerList();
		}

		ArrayList<Marker> searchResults =new ArrayList<Marker>();
		Log.d("SEARCH-------------------0", ""+query);
		if (jLayer.getMarkerCount() > 0) {
			for(int i = 0; i < jLayer.getMarkerCount(); i++) {
				Marker ma = jLayer.getMarker(i);
				if(ma.getTitle().toLowerCase().indexOf(query.toLowerCase()) != -1){
					searchResults.add(ma);
					/*the website for the corresponding title*/
				}
			}
		}
		if (searchResults.size() > 0){
			dataView.setFrozen(true);
			jLayer.setMarkerList(searchResults);
		}
		else
			Toast.makeText( this, getString(DataView.SEARCH_FAILED_NOTIFICATION), Toast.LENGTH_LONG ).show();
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			this.mWakeLock.release();

      unregisterListners();

			if (fError) {
				finish();
			}
		} catch (Exception ex) {
			doError(ex);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			this.mWakeLock.acquire();

			killOnError();
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			mixContext.mixView = this;
			dataView.doStart();
			dataView.clearEvents();


			mixContext.refreshDataSources();
			
			double angleX, angleY;

			int marker_orientation = -90;

			int rotation = Compatibility.getRotation(this);
			
			//display text from left to right and keep it horizontal
			angleX = Math.toRadians(marker_orientation);
			m1.set(	1f,	0f, 						0f, 
					0f,	(float) Math.cos(angleX),	(float) -Math.sin(angleX),
					0f,	(float) Math.sin(angleX),	(float) Math.cos(angleX)
			);
			angleX = Math.toRadians(marker_orientation);
			angleY = Math.toRadians(marker_orientation);
			if (rotation == 1) {
				m2.set(	1f,	0f,							0f,
						0f,	(float) Math.cos(angleX),	(float) -Math.sin(angleX),
						0f,	(float) Math.sin(angleX),	(float) Math.cos(angleX));
				m3.set(	(float) Math.cos(angleY),	0f,	(float) Math.sin(angleY),
						0f,							1f,	0f,
						(float) -Math.sin(angleY),	0f,	(float) Math.cos(angleY));
			} else {
				m2.set(	(float) Math.cos(angleX),	0f,	(float) Math.sin(angleX),
						0f,							1f,	0f,
						(float) -Math.sin(angleX),	0f, (float) Math.cos(angleX));
				m3.set(	1f,	0f,							0f, 
						0f,	(float) Math.cos(angleY),	(float) -Math.sin(angleY),
						0f,	(float) Math.sin(angleY),	(float) Math.cos(angleY));
				
			}
			
			m4.toIdentity();

			for (int i = 0; i < histR.length; i++) {
				histR[i] = new Matrix();
			}

			sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

			sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (sensors.size() > 0) {
				sensorGrav = sensors.get(0);
			}

			sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
			if (sensors.size() > 0) {
				sensorMag = sensors.get(0);
			}

			sensorMgr.registerListener(this, sensorGrav, SENSOR_DELAY_GAME);
			sensorMgr.registerListener(this, sensorMag, SENSOR_DELAY_GAME);

			try {

				GeomagneticField gmf = new GeomagneticField((float) mixContext.curLoc
						.getLatitude(), (float) mixContext.curLoc.getLongitude(),
						(float) mixContext.curLoc.getAltitude(), System
						.currentTimeMillis());

				angleY = Math.toRadians(-gmf.getDeclination());
				m4.set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math
						.sin(angleY), 0f, (float) Math.cos(angleY));
				mixContext.declination = gmf.getDeclination();
			} catch (Exception ex) {
				Log.d("mixare", "GPS Initialize Error", ex);
			}
			downloadThread = new Thread(mixContext.downloadManager);
			downloadThread.start();
		} catch (Exception ex) {
			doError(ex);
      unregisterListners();
		}

		if (dataView.isFrozen() && searchNotificationTxt == null){
			searchNotificationTxt = new TextView(this);
			searchNotificationTxt.setWidth(dWindow.getWidth());
			searchNotificationTxt.setPadding(10, 2, 0, 0);			
			searchNotificationTxt.setText(getString(DataView.SEARCH_ACTIVE_1)+" "+ DataSourceList.getDataSourcesStringList()+ getString(DataView.SEARCH_ACTIVE_2));;
			searchNotificationTxt.setBackgroundColor(Color.DKGRAY);
			searchNotificationTxt.setTextColor(Color.WHITE);

			searchNotificationTxt.setOnTouchListener(this);
			addContentView(searchNotificationTxt, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		}
		else if(!dataView.isFrozen() && searchNotificationTxt != null){
			searchNotificationTxt.setVisibility(View.GONE);
			searchNotificationTxt = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int base = Menu.FIRST;
		/*define the first*/
		MenuItem item1 =menu.add(base, base, base, getString(DataView.MENU_ITEM_1)); 
		MenuItem item2 =menu.add(base, base+1, base+1,  getString(DataView.MENU_ITEM_2)); 
		MenuItem item3 =menu.add(base, base+2, base+2,  getString(DataView.MENU_ITEM_3));
		MenuItem item4 =menu.add(base, base+3, base+3,  getString(DataView.MENU_ITEM_4));
		MenuItem item5 =menu.add(base, base+4, base+4,  getString(DataView.MENU_ITEM_5));
		MenuItem item6 =menu.add(base, base+5, base+5,  getString(DataView.MENU_ITEM_6));
		MenuItem item7 =menu.add(base, base+6, base+6,  getString(DataView.MENU_ITEM_7));

		/*assign icons to the menu items*/
		item1.setIcon(drawable.icon_datasource);
		item2.setIcon(android.R.drawable.ic_menu_view);
		item3.setIcon(android.R.drawable.ic_menu_mapmode);
		item4.setIcon(android.R.drawable.ic_menu_zoom);
		item5.setIcon(android.R.drawable.ic_menu_search);
		item6.setIcon(android.R.drawable.ic_menu_info_details);
		item7.setIcon(android.R.drawable.ic_menu_share);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		/*Data sources*/
		case 1:		
			if(!dataView.isLauncherStarted()){
				MixListView.setList(1);
				Intent intent = new Intent(MixView.this, DataSourceList.class); 
				startActivityForResult(intent, 40);
			}
			else{
				Toast.makeText( this, getString(DataView.OPTION_NOT_AVAILABLE_STRING_ID), Toast.LENGTH_LONG ).show();		
			}
			break;
			/*List view*/
		case 2:

			MixListView.setList(2);
			/*if the list of titles to show in alternative list view is not empty*/
			if (dataView.getDataHandler().getMarkerCount() > 0) {
				Intent intent1 = new Intent(MixView.this, MixListView.class); 
				startActivityForResult(intent1, 42);
			}
			/*if the list is empty*/
			else{
				Toast.makeText( this, DataView.EMPTY_LIST_STRING_ID, Toast.LENGTH_LONG ).show();			
			}
			break;
			/*Map View*/
		case 3:
			Intent intent2 = new Intent(MixView.this, MixMap.class); 
			startActivityForResult(intent2, 20);
			break;
			/*zoom level*/
		case 4:
			myZoomBar.setVisibility(View.VISIBLE);
			zoomProgress = myZoomBar.getProgress();
			break;
			/*Search*/
		case 5:
			onSearchRequested();
			break;
			/*GPS Information*/
		case 6:
			Location currentGPSInfo = mixContext.getCurrentLocation();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(DataView.GENERAL_INFO_TEXT)+ "\n\n" +
					getString(DataView.GPS_LONGITUDE) + currentGPSInfo.getLongitude() + "\n" +
					getString(DataView.GPS_LATITUDE) + currentGPSInfo.getLatitude() + "\n" +
					getString(DataView.GPS_ALTITUDE)+ currentGPSInfo.getAltitude() + "m\n" +
					getString(DataView.GPS_SPEED) + currentGPSInfo.getSpeed() + "km/h\n" +
					getString(DataView.GPS_ACCURACY) + currentGPSInfo.getAccuracy() + "m\n" +
					getString(DataView.GPS_LAST_FIX) + new Date(currentGPSInfo.getTime()).toString() + "\n");
			builder.setNegativeButton(getString(DataView.CLOSE_BUTTON), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			AlertDialog alert = builder.create();
			alert.setTitle(getString(DataView.GENERAL_INFO_TITLE));
			alert.show();
			break;
			/*Case 6: license agreements*/
		case 7:
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setMessage(getString(DataView.LICENSE_TEXT));	
			/*Retry*/
			builder1.setNegativeButton(getString(DataView.CLOSE_BUTTON), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			AlertDialog alert1 = builder1.create();
			alert1.setTitle(getString(DataView.LICENSE_TITLE));
			alert1.show();
			break;

		}
		return true;
	}

	public float calcZoomLevel(){

		int myZoomLevel = myZoomBar.getProgress();
		float myout = 5;

		if (myZoomLevel <= 26) {
			myout = myZoomLevel / 25f;
		} else if (25 < myZoomLevel && myZoomLevel < 50) {
			myout = (1 + (myZoomLevel - 25)) * 0.38f;
		} 
		else if (25== myZoomLevel) {
			myout = 1;
		} 
		else if (50== myZoomLevel) {
			myout = 10;
		} 
		else if (50 < myZoomLevel && myZoomLevel < 75) {
			myout = (10 + (myZoomLevel - 50)) * 0.83f;
		} else {
			myout = (30 + (myZoomLevel - 75) * 2f);
		}


		return myout;
	}

	private void setZoomLevel() {
		float myout = calcZoomLevel();

		dataView.setRadius(myout);

		myZoomBar.setVisibility(View.INVISIBLE);
		zoomLevel = String.valueOf(myout);

		dataView.doStart();
		dataView.clearEvents();
		downloadThread = new Thread(mixContext.downloadManager);
		downloadThread.start();

	};

	private SeekBar.OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		Toast t;

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			float myout = calcZoomLevel();

			zoomLevel = String.valueOf(myout);
			zoomProgress = myZoomBar.getProgress();

			t.setText("Radius: " + String.valueOf(myout));
			t.show();
		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			Context ctx = seekBar.getContext();
			t = Toast.makeText(ctx, "Radius: ", Toast.LENGTH_LONG);
			//			zoomChanging= true;
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			/*store the zoom range of the zoom bar selected by the user*/
			editor.putInt("zoomLevel", myZoomBar.getProgress());
			editor.commit();
			myZoomBar.setVisibility(View.INVISIBLE);
			//			zoomChanging= false;

			myZoomBar.getProgress();

			t.cancel();
			setZoomLevel();
		}
	};

	public void onSensorChanged(SensorEvent evt) {
		try {
			if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
        /* filtering the accelerometer values */
				grav = lpf_acc.filter(evt.values, grav);
				augScreen.postInvalidate();

			} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
        /* filtering the magnetic field values */
				mag = lpf_mgnt.filter(evt.values, mag);
				augScreen.postInvalidate();
			}

			SensorManager.getRotationMatrix(RTmp, I, grav, mag);
			Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

			if (display.getRotation() == 1)
				SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, Rot);
			else
				SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, Rot);

			tempR.set(Rot[0], Rot[1], Rot[2], Rot[3], Rot[4], Rot[5], Rot[6], Rot[7], Rot[8]);

			finalR.toIdentity();
			finalR.prod(m4);
			finalR.prod(m1);
			finalR.prod(tempR);
			finalR.prod(m3);
			finalR.prod(m2);
			finalR.invert(); 

			synchronized (mixContext.rotationM) {
				mixContext.rotationM.set(finalR);
			}

		} catch (Exception e) {
			Log.e(TAG, e.getStackTrace().toString());
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		try {
			killOnError();

			float xPress = me.getX();
			float yPress = me.getY();
			if (me.getAction() == MotionEvent.ACTION_UP) {
				dataView.clickEvent(xPress, yPress);
			}

			return true;
		} catch (Exception ex) {
			//doError(ex);
			ex.printStackTrace();
			return super.onTouchEvent(me);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			killOnError();

			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (dataView.isDetailsView()) {
					dataView.keyEvent(keyCode);
					dataView.setDetailsView(false);
					return true;
				} else {
					return super.onKeyDown(keyCode, event);
				}
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				return super.onKeyDown(keyCode, event);
			}
			else {
				dataView.keyEvent(keyCode);
				return false;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			return super.onKeyDown(keyCode, event);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy==SensorManager.SENSOR_STATUS_UNRELIABLE && compassErrorDisplayed == 0) {
			for(int i = 0; i <2; i++) {
				Toast.makeText(mixContext, "Compass data unreliable. Please recalibrate compass.", Toast.LENGTH_LONG).show();
			}
			compassErrorDisplayed++;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		dataView.setFrozen(false);
		if (searchNotificationTxt != null) {
			searchNotificationTxt.setVisibility(View.GONE);
			searchNotificationTxt = null;
		}
		return false;
	}

  /**
  * Function intended to unregister listners in case of pausing
  * the application (avoids battery drain).
  */
  private void unregisterListners() {
    try {
      /* disabling sensor manager */
      if(sensorMgr != null) {
        sensorMgr.unregisterListener(this, sensorGrav);
        sensorMgr.unregisterListener(this, sensorMag);
        sensorMgr = null;
      }

      /* disabling mix context */
      if(mixContext != null) {
        mixContext.unregisterLocationManager();

        if(mixContext.downloadManager != null)
          mixContext.downloadManager.stop();
      }
    } catch (Exception ignore) {
      /* Exception igonred */
		}
  }
}

/**
 * @author daniele
 *
 */
class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {
	MixView app;
	SurfaceHolder holder;
	Camera camera;

	CameraSurface(Context context) {
		super(context);

		try {
			app = (MixView) context;

			holder = getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		} catch (Exception ex) {

		}
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			if (camera != null) {
				try {
					camera.stopPreview();
				} catch (Exception ignore) {
				}
				try {
					camera.release();
				} catch (Exception ignore) {
				}
				camera = null;
			}

			camera = Camera.open();
			camera.setPreviewDisplay(holder);
		} catch (Exception ex) {
			try {
				if (camera != null) {
					try {
						camera.stopPreview();
					} catch (Exception ignore) {
					}
					try {
						camera.release();
					} catch (Exception ignore) {
					}
					camera = null;
				}
			} catch (Exception ignore) {

			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			if (camera != null) {
				try {
					camera.stopPreview();
				} catch (Exception ignore) {
				}
				try {
					camera.release();
				} catch (Exception ignore) {
				}
				camera = null;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		try {
			Camera.Parameters parameters = camera.getParameters();
			try {
				List<Camera.Size> supportedSizes = null;
				//On older devices (<1.6) the following will fail
				//the camera will work nevertheless
				supportedSizes = Compatibility.getSupportedPreviewSizes(parameters);

				//preview form factor
				float ff = (float)w/h;
				Log.d("Mixare", "Screen res: w:"+ w + " h:" + h + " aspect ratio:" + ff);

				//holder for the best form factor and size
				float bff = 0;
				int bestw = 0;
				int besth = 0;
				Iterator<Camera.Size> itr = supportedSizes.iterator();

				//we look for the best preview size, it has to be the closest to the
				//screen form factor, and be less wide than the screen itself
				//the latter requirement is because the HTC Hero with update 2.1 will
				//report camera preview sizes larger than the screen, and it will fail
				//to initialize the camera
				//other devices could work with previews larger than the screen though
				while(itr.hasNext()) {
					Camera.Size element = itr.next();
					//current form factor
					float cff = (float)element.width/element.height;
					//check if the current element is a candidate to replace the best match so far
					//current form factor should be closer to the bff
					//preview width should be less than screen width
					//preview width should be more than current bestw
					//this combination will ensure that the highest resolution will win
					Log.d("Mixare", "Candidate camera element: w:"+ element.width + " h:" + element.height + " aspect ratio:" + cff);
					if ((ff-cff <= ff-bff) && (element.width <= w) && (element.width >= bestw)) {
						bff=cff;
						bestw = element.width;
						besth = element.height;
					}
				} 
				Log.d("Mixare", "Chosen camera element: w:"+ bestw + " h:" + besth + " aspect ratio:" + bff);
				//Some Samsung phones will end up with bestw and besth = 0 because their minimum preview size is bigger then the screen size.
				//In this case, we use the default values: 480x320
				if ((bestw == 0) || (besth == 0)){
					Log.d("Mixare", "Using default camera parameters!");
					bestw = 480;
					besth = 320;
				}
				parameters.setPreviewSize(bestw, besth);
			} catch (Exception ex) {
				parameters.setPreviewSize(480 , 320);
			}

			camera.setParameters(parameters);
			camera.startPreview();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

class AugmentedView extends View {
	MixView app;
	int xSearch=200;
	int ySearch = 10;
	int searchObjWidth = 0;
	int searchObjHeight=0;

	public AugmentedView(Context context) {
		super(context);

		try {
			app = (MixView) context;

			app.killOnError();
		} catch (Exception ex) {
			app.doError(ex);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			//			if (app.fError) {
			//
			//				Paint errPaint = new Paint();
			//				errPaint.setColor(Color.RED);
			//				errPaint.setTextSize(16);
			//				
			//				/*Draws the Error code*/
			//				canvas.drawText("ERROR: ", 10, 20, errPaint);
			//				canvas.drawText("" + app.fErrorTxt, 10, 40, errPaint);
			//
			//				return;
			//			}

			app.killOnError();

			MixView.dWindow.setWidth(canvas.getWidth());
			MixView.dWindow.setHeight(canvas.getHeight());

			MixView.dWindow.setCanvas(canvas);

			if (!MixView.dataView.isInited()) {
				MixView.dataView.init(MixView.dWindow.getWidth(), MixView.dWindow.getHeight());
			}
			if (app.isZoombarVisible()){
				Paint zoomPaint = new Paint();
				zoomPaint.setColor(Color.WHITE);
				zoomPaint.setTextSize(14);
				String startKM, endKM;
				endKM = "80km";
				startKM = "0km";
				/*if(MixListView.getDataSource().equals("Twitter")){
					startKM = "1km";
				}*/
				canvas.drawText(startKM, canvas.getWidth()/100*4, canvas.getHeight()/100*85, zoomPaint);
				canvas.drawText(endKM, canvas.getWidth()/100*99+25, canvas.getHeight()/100*85, zoomPaint);

				int height= canvas.getHeight()/100*85;
				int zoomProgress = app.getZoomProgress();
				if (zoomProgress > 92 || zoomProgress < 6) {
					height = canvas.getHeight()/100*80;
				}
				canvas.drawText(app.getZoomLevel(),  (canvas.getWidth())/100*zoomProgress+20, height, zoomPaint);
			}

			MixView.dataView.draw(MixView.dWindow);
		} catch (Exception ex) {
			app.doError(ex);
		}
	}
}
