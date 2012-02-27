package org.mixare;

import org.mixare.barcode.IntentIntegrator;
import org.mixare.barcode.IntentResult;
import org.mixare.data.DataSourceList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * The user can scan for a url with the zxing barcode scanner.
 * 
 * @author A.Egal
 * 
 */
public class BarcodeMainActivity extends Activity {

	private static final int SPLASHTIME = 5000;
	public static final int SCANNER_REQUEST_CODE = 0;
	protected Handler exitHandler = null;
	protected Runnable exitRunnable = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.splashscreen);
		// Runnable exiting the splash screen and launching the menu
		exitRunnable = new Runnable() {
			public void run() {
				exitSplash();
			}
		};
		// Run the exitRunnable in in _splashTime ms
		exitHandler = new Handler();
		exitHandler.postDelayed(exitRunnable, SPLASHTIME);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// Remove the exitRunnable callback from the handler queue
			exitHandler.removeCallbacks(exitRunnable);
			// Run the exit code manually
			exitSplash();
		}
		return true;
	}

	private void exitSplash() {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, data);
		if (scanResult != null) {
			String url = scanResult.getContents();
			SharedPreferences settings = getSharedPreferences(DataSourceList.SHARED_PREFS, 0);
			SharedPreferences.Editor dataSourceEditor = settings.edit();
			dataSourceEditor.putString("DataSource4", "Arena|"+url+"|5|2|true");
			dataSourceEditor.commit();
			startActivity(new Intent(this, MixView.class));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}