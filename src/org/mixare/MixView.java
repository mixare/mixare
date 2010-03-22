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

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.mixare.gui.PaintScreen;
import org.mixare.render.Matrix;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

public class MixView extends Activity implements SensorEventListener,
LocationListener {


	CameraSurface camScreen;
	AugmentedView augScreen;

	static boolean isInited = false;
	static MixContext ctx;
	static PaintScreen dWindow;
	static DataView view;
	Thread downloadThread;

	float RTmp[] = new float[9];
	float R[] = new float[9];
	float I[] = new float[9];
	float grav[] = new float[3];
	float mag[] = new float[3];

	private SensorManager sensorMgr;
	private List<Sensor> sensors;
	private Sensor sensorGrav, sensorMag;
	private LocationManager locationMgr;
	boolean isGpsEnabled = false;

	int rHistIdx = 0;
	Matrix tempR = new Matrix();
	Matrix finalR = new Matrix();
	Matrix smoothR = new Matrix();
	Matrix histR[] = new Matrix[60];
	Matrix m1 = new Matrix();
	Matrix m2 = new Matrix();
	Matrix m3 = new Matrix();
	Matrix m4 = new Matrix();

	private SeekBar myZoomBar;
	private WakeLock mWakeLock;

	boolean fError = false;
	String fErrorTxt;
	Exception fExeption;


	public void doError(Exception ex1) {
		if (!fError) {
			fError = true;
			fErrorTxt = ex1.toString();
			fExeption = ex1;

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


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			this.mWakeLock = pm.newWakeLock(
					PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
			killOnError();
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			myZoomBar = new SeekBar(this);
			myZoomBar.setVisibility(View.INVISIBLE);
			myZoomBar.setMax(100);
			myZoomBar.setProgress(65);
			myZoomBar
			.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
			myZoomBar.setVisibility(View.INVISIBLE);

			FrameLayout FL = new FrameLayout(this);

			FL.setMinimumWidth(3000);
			FL.addView(myZoomBar);
			FL.setPadding(10, 0, 10, 10);

			camScreen = new CameraSurface(this);
			augScreen = new AugmentedView(this);
			setContentView(camScreen);

			addContentView(augScreen, new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

			addContentView(FL, new FrameLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT,
					Gravity.BOTTOM));

			if (!isInited) {

				ctx = new MixContext(this);
				ctx.downloadManager = new DownloadManager(ctx);
				dWindow = new PaintScreen();
				view = new DataView(ctx);
				isInited = true;
			}
		} catch (Exception ex) {
			doError(ex);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		try {
			this.mWakeLock.release();

			try {
				sensorMgr.unregisterListener(this, sensorGrav);
			} catch (Exception ignore) {
			}
			try {
				sensorMgr.unregisterListener(this, sensorMag);
			} catch (Exception ignore) {
			}
			sensorMgr = null;

			try {
				locationMgr.removeUpdates(this);
			} catch (Exception ignore) {
			}
			try {
				locationMgr = null;
			} catch (Exception ignore) {
			}

			try {
				ctx.downloadManager.stop();
			} catch (Exception ignore) {
			}

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
			ctx.mixView = this;
			view.doStart();
			view.clearEvents();

			double angleX, angleY;

			angleX = Math.toRadians(-90);
			m1.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math
					.sin(angleX), 0f, (float) Math.sin(angleX), (float) Math
					.cos(angleX));

			angleX = Math.toRadians(-90);
			angleY = Math.toRadians(-90);
			m2.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math
					.sin(angleX), 0f, (float) Math.sin(angleX), (float) Math
					.cos(angleX));
			m3.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY),
					0f, 1f, 0f, (float) -Math.sin(angleY), 0f, (float) Math
					.cos(angleY));

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
				Criteria c = new Criteria();

				c.setAccuracy(Criteria.ACCURACY_FINE);
				//c.setBearingRequired(true);

				locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				String bestP = locationMgr.getBestProvider(c, true);
				isGpsEnabled = locationMgr.isProviderEnabled(bestP);

				if (isGpsEnabled) {
					locationMgr.requestLocationUpdates(bestP, 100, 1, this);
				}

				Location lastFix = new Location("reverseGeocoded");

				lastFix.setLatitude(46.47122383117541);
				lastFix.setLongitude(11.260278224944742);
				lastFix.setAltitude(300);

				//Log.d("mixare", lastFix.toString());
				try {
					//lastFix = locationMgr.getLastKnownLocation(bestP);
					//Log.d("mixare", "resume: " + lastFix.toString());
					ctx.curLoc = new Location(locationMgr.getLastKnownLocation(bestP));
				} catch (Exception ex2) {
					ctx.curLoc = new Location(lastFix);
					//ex2.printStackTrace();
				}

				GeomagneticField gmf = new GeomagneticField((float) ctx.curLoc
						.getLatitude(), (float) ctx.curLoc.getLongitude(),
						(float) ctx.curLoc.getAltitude(), System
						.currentTimeMillis());

				angleY = Math.toRadians(-gmf.getDeclination());
				m4.set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math
						.sin(angleY), 0f, (float) Math.cos(angleY));
				ctx.declination = gmf.getDeclination();
			} catch (Exception ex) {
				Log.d("mixare", "GPS Initialize Error", ex);
			}

			downloadThread = new Thread(ctx.downloadManager);
			downloadThread.start();


		} catch (Exception ex) {
			doError(ex);

			try {
				if (sensorMgr != null) {
					sensorMgr.unregisterListener(this, sensorGrav);
					sensorMgr.unregisterListener(this, sensorMag);
					sensorMgr = null;
				}

				if (locationMgr != null) {
					locationMgr.removeUpdates(this);
					locationMgr = null;
				}

				if (ctx != null) {
					if (ctx.downloadManager != null)
						ctx.downloadManager.stop();

				}
			} catch (Exception ignore) {

			}
		}
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		try {

			myZoomBar.setVisibility(View.VISIBLE);

			return true;
		} catch (Exception ex) {
			doError(ex);
			return super.onPrepareOptionsMenu(menu);
		}
	}

	private void SetZoomLevel() {
		int myZoomLevel = myZoomBar.getProgress();
		float myout = 5;
		if (myZoomLevel < 25) {
			myout = myZoomLevel / 25f;
		} else if (25 < myZoomLevel && myZoomLevel < 50) {
			myout = (1 + (myZoomLevel - 25)) * 0.38f;
		} else if (50 < myZoomLevel && myZoomLevel < 75) {
			myout = (10 + (myZoomLevel - 50)) * 0.83f;
		} else {
			myout = (30 + (myZoomLevel - 75) * 2f);
		}

		view.state.radius = String.valueOf(myout);

		myZoomBar.setVisibility(View.INVISIBLE);
		view.doStart();
		view.clearEvents();
		downloadThread = new Thread(ctx.downloadManager);
		downloadThread.start();

	};

	private SeekBar.OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

		Toast t;

		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			int myZoomLevel = myZoomBar.getProgress();
			float myout = 5;
			if (myZoomLevel < 25) {
				myout = myZoomLevel / 25f;
			} else if (25 <= myZoomLevel && myZoomLevel < 50) {
				myout = (1 + (myZoomLevel - 25)) * 0.38f;
			} else if (50 <= myZoomLevel && myZoomLevel < 75) {
				myout = (10 + (myZoomLevel - 50)) * 0.83f;
			} else {
				myout = (30 + (myZoomLevel - 75) * 2f);
			}
			t.setText("Radius: " + String.valueOf(myout));
			t.show();

		}

		public void onStartTrackingTouch(SeekBar seekBar) {
			Context ctx = seekBar.getContext();
			t = Toast.makeText(ctx, "Radius: ", Toast.LENGTH_LONG);
		}

		public void onStopTrackingTouch(SeekBar seekBar) {
			t.cancel();
			SetZoomLevel();
		}

	};

	public void onAccuracyChanged(Sensor s, int accuracy) {
		//TODO: do something here
	}

	public void onSensorChanged(SensorEvent evt) {
		try {
			killOnError();

			if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				grav[0] = evt.values[0];
				grav[1] = evt.values[1];
				grav[2] = evt.values[2];

				augScreen.postInvalidate();
			} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				mag[0] = evt.values[0];
				mag[1] = evt.values[1];
				mag[2] = evt.values[2];

				augScreen.postInvalidate();
			}

			SensorManager.getRotationMatrix(RTmp, I, grav, mag);
			SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, R);

			tempR.set(R[0], R[1], R[2], R[3], R[4], R[5], R[6], R[7],
					R[8]);

			finalR.toIdentity();
			finalR.prod(m4);
			finalR.prod(m1);
			finalR.prod(tempR);
			finalR.prod(m3);
			finalR.prod(m2);
			finalR.invert(); // TODO: use transpose() instead

			histR[rHistIdx].set(finalR);
			rHistIdx++;
			if (rHistIdx >= histR.length)
				rHistIdx = 0;

			smoothR.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
			for (int i = 0; i < histR.length; i++) {
				smoothR.add(histR[i]);
			}
			smoothR.mult(1 / (float) histR.length);

			synchronized (ctx.rotationM) {
				ctx.rotationM.set(smoothR);
			}
		} catch (Exception ex) {
			doError(ex);
		}
	}

	public boolean onTouchEvent(MotionEvent me) {
		try {
			killOnError();

			float xPress = me.getX();
			float yPress = me.getY();
			if (me.getAction() == me.ACTION_UP) {
				view.clickEvent(xPress, yPress);
			}

			return true;
		} catch (Exception ex) {
			doError(ex);

			return super.onTouchEvent(me);
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			killOnError();

			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (view.state.detailsView) {
					view.keyEvent(keyCode);
					view.state.detailsView = false;
					return true;
				} else {
					return super.onKeyDown(keyCode, event);
				}
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				return super.onKeyDown(keyCode, event);
			}
			else {
				view.keyEvent(keyCode);
				return false;
			}

		} catch (Exception ex) {
			doError(ex);

			return super.onKeyDown(keyCode, event);
		}
	}

	public void onProviderDisabled(String provider) {
		isGpsEnabled = locationMgr
		.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public void onProviderEnabled(String provider) {
		isGpsEnabled = locationMgr
		.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public void onLocationChanged(Location location) {
		try {
			killOnError();

			if (LocationManager.GPS_PROVIDER.equals(location.getProvider())) {
				synchronized (ctx.curLoc) {
					ctx.curLoc = location;
				}
				isGpsEnabled = true;
			}
		} catch (Exception ex) {
			doError(ex);
		}
	}
}

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
				supportedSizes = Compatibility.getSupportedPreviewSizes(parameters);

				Iterator<Camera.Size> itr = supportedSizes.iterator(); 
				while(itr.hasNext()) {
					Camera.Size element = itr.next(); 
					element.width -= w;
					element.height -= h;
					Log.d("mixare", "W:" + element.width + " H:" + element.height);
				} 
				Collections.sort(supportedSizes, new ResolutionsOrder());
				parameters.setPreviewSize(w + supportedSizes.get(supportedSizes.size()-1).width, h + supportedSizes.get(supportedSizes.size()-1).height);
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

class ResolutionsOrder implements java.util.Comparator<Camera.Size> {
	public int compare(Camera.Size left, Camera.Size right) {

		return Float.compare(left.width + left.height, right.width + right.height);
	}
}

class AugmentedView extends View {
	MixView app;

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
			if (app.fError) {
				Paint errPaint = new Paint();
				errPaint.setColor(Color.RED);
				errPaint.setTextSize(16);

				canvas.drawText("ERROR: ", 10, 20, errPaint);
				canvas.drawText("" + app.fErrorTxt, 10, 40, errPaint);

				return;
			}

			app.killOnError();

			MixView.dWindow.setWidth(canvas.getWidth());
			MixView.dWindow.setHeight(canvas.getHeight());
			MixView.dWindow.setCanvas(canvas);

			if (!MixView.view.isInited()) {
				MixView.view.init(MixView.dWindow.getWidth(),
						MixView.dWindow.getHeight());
			}

			MixView.view.draw(MixView.dWindow);
		} catch (Exception ex) {
			app.doError(ex);
		}
	}
}


