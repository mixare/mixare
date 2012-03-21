package org.mixare;

import org.mixare.data.DataSourceList;
import org.mixare.plugin.PluginLoader;
import org.mixare.plugin.PluginType;
import org.mixare.plugin.connection.BootStrapActivityConnection;

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
 * This is the main activity for mixare. This activity will load a splashscreen and then initializes the PluginLoader
 * It will then launch the visible bootstrapplugins and waits for their results. After all bootstrap plugins are loaded
 * then mixare will be launched
 * @author A.Egal
 */
public class MainActivity extends Activity {

	private static final int SPLASHTIME = 2000; //2 seconds
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
		PluginLoader.getInstance().setActivity(this);
		PluginLoader.getInstance().loadPlugin(PluginType.MARKER);
		PluginLoader.getInstance().loadPlugin(PluginType.BOOTSTRAP);
		
		startMixare();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(BootStrapActivityConnection.BOOTSTRAP_REQUEST_CODE == requestCode){
			String url = data.getExtras().getString("url");
			SharedPreferences settings = getSharedPreferences(DataSourceList.SHARED_PREFS, 0);
			SharedPreferences.Editor dataSourceEditor = settings.edit();
			//remove other datasources because you scanned a new one with a barcodescanner.
			dataSourceEditor.clear();
			dataSourceEditor.putString("DataSource0", "Arena|"+url+"|5|2|true");
			dataSourceEditor.commit();
			PluginLoader.getInstance().decreasePendingActivitiesOnResult();
		}
		startMixare();
	}
	
	private void startMixare(){
		if(PluginLoader.getInstance().getPendingActivitiesOnResult() == 0){
			startActivity(new Intent(this, MixView.class));
		}
	}
}