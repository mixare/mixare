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

import org.mixare.utils.ErrorUtility;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.net.ConnectivityManager;
import android.provider.Settings;

import org.mixare.gui.ButtonTypes;
import org.mixare.gui.DialogButton;
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
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is the main application which uses the other classes for different
 * functionalities.
 * It sets up the camera screen and the augmented screen which is in front of the
 * camera screen.
 * It also handles the main sensor events, touch events and location events.
 */
public class MixView extends Activity
                     implements SensorEventListener, OnTouchListener {

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
	private Matrix m1 = new Matrix();
	private Matrix m2 = new Matrix();
	private Matrix m3 = new Matrix();
	private Matrix m4 = new Matrix();

	private SeekBar myZoomBar;
	private WakeLock mWakeLock;

	private int compassErrorDisplayed = 0;

	private String zoomLevel;
	private int zoomProgress;

	private TextView searchNotificationTxt;
  /* low pass filter object for accelerometer */
  LowPassFilter lpf_acc;
  /* low pass filter object for magnet */
  LowPassFilter lpf_mgnt;

	/* TAG for logging */
	public static final String TAG = "MixView";

	/* string to name & access the preference file in the internal storage */
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

	public void repaint() {
		dataView = new DataView(mixContext);
		dWindow = new PaintScreen();
		setZoomLevel();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


    /*********************** DEBUG *************************/
    System.out.println("------------------- debug start     -------------------");
    /* define buttons */
    ArrayList<DialogButton> btns = new ArrayList<DialogButton>();
    btns.add(new DialogButton(ButtonTypes.POSITIVE,
      getString(DataView.CONNECTION_ERROR_DIALOG_BUTTON1),
      new RetryClick()));

    btns.add(new DialogButton(ButtonTypes.NEUTRAL,
      getString(DataView.CONNECTION_ERROR_DIALOG_BUTTON2),
      new OpenSettingsClick()));

    btns.add(new DialogButton(ButtonTypes.NEGATIVE,
      getString(DataView.CONNECTION_ERROR_DIALOG_BUTTON3),
      new CloseClick()));

    /* generate the error (log and visualization) */
    ErrorUtility.handleError(TAG,
                             getString(DataView.CONNECTION_ERROR_DIALOG_TEXT),
                             "No connectivity found",
                             this,
                             btns);
    System.out.println("------------------- debug stop      -------------------");
    /******************** END DEBUG ************************/

		try {
      /* initializing the low pass filter objects. The parameters have been
         defined empirically */
      lpf_acc  = new LowPassFilter(0.5f, 1.0f);
      lpf_mgnt = new LowPassFilter(2.0f, 5.0f);

			handleIntent(getIntent());

			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");

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
      boolean firstAccess = settings.getBoolean("firstAccess",false);
			if(firstAccess == false){
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

		} catch (Exception e) {
			ErrorUtility.handleError(TAG, e);
      //augScreen.invalidate();
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

//			if (fError) {
//				finish();
//			}
		} catch (Exception e) {
			ErrorUtility.handleError(TAG, e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		try {
			this.mWakeLock.acquire();

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
		} catch (Exception e) {
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
			
			int rotation = Compatibility.getRotation(this);
			
			if (rotation == 1) {
				SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, Rot);
			} else {
				SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, Rot);
      }

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
			ErrorUtility.handleError(TAG, e);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		try {

			float xPress = me.getX();
			float yPress = me.getY();

			if (me.getAction() == MotionEvent.ACTION_UP)
				dataView.clickEvent(xPress, yPress);

			return true;

		} catch (Exception e) {
			ErrorUtility.handleError(TAG, e);
			return super.onTouchEvent(me);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
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

		} catch (Exception e) {
      ErrorUtility.handleError(TAG, e);
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

    } catch (Exception e) {
      /* Exception ignored, but logged */
      ErrorUtility.handleError(TAG, e);
		}
  }

  /**
  * This Class represents the retry button action that is shown
  * on a dialog box when an error is detected.
  *
  * @author Armando Miraglia &lt;arma&#64;lamortenera.it&gt;
  */
  class RetryClick implements DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int id) {
      /* TODO: improve */
      repaint();	       		
    }
  }

  /**
  * This Class represents the open settings button action that is shown
  * on a dialog box when an error is detected.
  *
  * @author Armando Miraglia &lt;arma&#64;lamortenera.it&gt;
  */
  class OpenSettingsClick implements DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int id) {
      Intent intent1 = new Intent(Settings.ACTION_WIRELESS_SETTINGS); 
      startActivityForResult(intent1, 42);
    }
  }

  /**
  * This Class represents the close button action that is shown
  * on a dialog box when an error is detected.
  *
  * @author Armando Miraglia &lt;arma&#64;lamortenera.it&gt;
  */
  class CloseClick implements DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int id) {
      System.exit(0);
    }
  }
}
