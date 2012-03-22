package org.mixare.plugin.arenasplash;

import org.mixare.plugin.arenasplash.service.ArenaSplashService;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class ArenaSplashActivity extends Activity {
	
	public final String resultType = "Splashscreen";
	private static final int SPLASHTIME = 2000; //2 seconds
	protected Handler exitHandler = null;
	protected Runnable exitRunnable = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
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
	
	private void exitSplash() {	
		Intent intent = new Intent();
		intent.putExtra("resultType", resultType);
		setResult(ArenaSplashService.ACTIVITY_REQUEST_CODE, intent);
		finish();
	}
	
}
