package org.mixare;

import org.mixare.data.DataSourceList;
import org.mixare.plugin.PluginLoader;
import org.mixare.plugin.PluginType;

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
		PluginLoader.getInstance().setActivity(this);
		PluginLoader.getInstance().loadPlugin(PluginType.BOOTSTRAP_PHASE_1);
		
		if(ArePendingActivitiesFinished()){
			startDefaultSplashScreen();
		}
	}
	
	private void startDefaultSplashScreen(){
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
		loadPlugins();		
		startMixare();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		procesDataSourceFromIntent(data);
		procesCustomSplashScreen(data);
		
		PluginLoader.getInstance().decreasePendingActivitiesOnResult();
		startMixare();
	}
	
	private void startMixare(){
		if(ArePendingActivitiesFinished()){
			startActivity(new Intent(this, MixView.class));
		}
	}
	
	private boolean ArePendingActivitiesFinished(){
		return (PluginLoader.getInstance().getPendingActivitiesOnResult() == 0);
	}
	
	private void procesDataSourceFromIntent(Intent data){
		if(data != null && data.getExtras().getString("resultType").equals("Datasource")){
			String url = data.getExtras().getString("url");
			SharedPreferences settings = getSharedPreferences(DataSourceList.SHARED_PREFS, 0);
			SharedPreferences.Editor dataSourceEditor = settings.edit();
			//remove other datasources because you received a new one with a plugin.
			dataSourceEditor.clear();
			dataSourceEditor.putString("DataSource0", "Arena|"+url+"|5|2|true");
			dataSourceEditor.commit();
		}
	}
	
	@Override
	protected void onDestroy() {
		PluginLoader.getInstance().unBindServices();
		PluginLoader.getInstance().setActivity(null);
		super.onDestroy();
	}
	
	private void procesCustomSplashScreen(Intent data){
		if(data != null && data.getExtras().getString("resultType").equals("Splashscreen")){
			loadPlugins();
		}
	}
	
	private void loadPlugins(){
		PluginLoader.getInstance().loadPlugin(PluginType.MARKER);
		PluginLoader.getInstance().loadPlugin(PluginType.BOOTSTRAP_PHASE_2);
	}
}