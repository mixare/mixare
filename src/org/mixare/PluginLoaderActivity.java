package org.mixare;

import java.util.List;

import org.mixare.data.DataSource;
import org.mixare.data.DataSourceStorage;
import org.mixare.plugin.Plugin;
import org.mixare.plugin.PluginLoader;
import org.mixare.plugin.PluginStatus;
import org.mixare.plugin.PluginType;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

/**
 * This is the plugin loading activity for mixare. This activity will load a splashscreen and then initializes the PluginLoader
 * It will then launch the visible bootstrap plugins and waits for their results. After all bootstrap plugins are loaded
 * then mixare will be launched.
 * @author A.Egal
 */
public class PluginLoaderActivity extends Activity {

	private static final int SPLASHTIME = 2000; // 2 seconds
	public static final int SCANNER_REQUEST_CODE = 0;
	private static final String CLOSE_ACTIVITY_CALL = "closed";
	protected Handler exitHandler = null;
	protected Runnable exitRunnable = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null){
			try {
				if (extras.containsKey("AppName")) {
					getInstalledPluginsByName(extras.getString("AppName"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		DataSourceStorage.init(this);
		PluginLoader.getInstance().setActivity(this);
		PluginLoader.getInstance().unBindServices();
		PluginLoader.newInstance();
		PluginLoader.getInstance().setActivity(this);
		PluginLoader.getInstance().loadPlugin(PluginType.BOOTSTRAP_PHASE_1);

		if (arePendingActivitiesFinished()) {
			startDefaultSplashScreen();
		}
	}
	
	private void startDefaultSplashScreen() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.splashscreen);
		// Runnable exiting the splash screen and launching the menu
		exitRunnable = new Runnable() {
			public void run() {
				startMixare();
			}
		};
		// Run the exitRunnable in in _splashTime ms
		exitHandler = new Handler();
		exitHandler.postDelayed(exitRunnable, SPLASHTIME);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if(exitHandler != null){
				//only call this when the default splashscreen is used
				exitHandler.removeCallbacks(exitRunnable);
			}
			startMixare();
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data != null && data.getExtras() != null && data.getExtras().getString(CLOSE_ACTIVITY_CALL) != null){
			//back button was pressed, close mixare now.
			finish();
			return;
		}
		
		if (requestCode == 0) {
			finish();
			return;
		}

		processDataSourceFromPlugin(data);
		procesCustomSplashScreen(data);

		PluginLoader.getInstance().decreasePendingActivitiesOnResult();
		startMixare();
	}

	private void startMixare() {
		if(!PluginLoader.getInstance().isPluginTypeLoaded(PluginType.MARKER)){
			loadPlugins();
		}
		if (arePendingActivitiesFinished()) {
			startActivityForResult(new Intent(this, MixView.class),0);
			finish();
		}
	}

	private boolean arePendingActivitiesFinished() {
		return (PluginLoader.getInstance().getPendingActivitiesOnResult() <= 0);
	}

	private void processDataSourceFromPlugin(Intent data) {
		if (data != null
				&& data.getExtras().getString("resultType")
						.equals("Datasource")) {
			String[] url = data.getExtras().getStringArray("url");
			// clear all datasources for a reinit
			for (int i = 0; i < url.length; i++) {
				DataSourceStorage.getInstance().clear();
				DataSource newDs = new DataSource("Barcode source", url[i],
						DataSource.TYPE.values()[5],
						DataSource.DISPLAY.values()[2], true);
				DataSourceStorage.getInstance().add(newDs);
			}
		}
	}

	@Override
	protected void onDestroy() {
		PluginLoader.getInstance().unBindServices();
		super.onDestroy();
	}

	private void procesCustomSplashScreen(Intent data) {
		if (data != null
				&& data.getExtras().getString("resultType")
						.equals("Splashscreen")) {
			loadPlugins();
		}
	}

	private void loadPlugins() {
		PluginLoader.getInstance().setActivity(this);
		PluginLoader.getInstance().loadPlugin(PluginType.MARKER);
		PluginLoader.getInstance().loadPlugin(PluginType.BOOTSTRAP_PHASE_2);
		PluginLoader.getInstance().loadPlugin(PluginType.DATAHANDLER);
	}

	/**
	 * Fills a list with installed Plugins
	 */
	private void getInstalledPluginsByName(String name) {
		PluginType[] allPluginTypes = PluginType.values();
		for (PluginType pluginType : allPluginTypes) {
			PackageManager packageManager = getPackageManager();
			Intent baseIntent = new Intent(pluginType.getActionName());
			List<ResolveInfo> list = packageManager.queryIntentServices(
					baseIntent, PackageManager.GET_RESOLVED_FILTER);

			for (ResolveInfo resolveInfo : list) {
				String lable = (String) resolveInfo.loadLabel(packageManager);
				if (lable.equalsIgnoreCase(name)) {
					Plugin plugin = new Plugin(PluginStatus.Activated,
						resolveInfo.serviceInfo,
						lable,
						resolveInfo.loadIcon(packageManager), pluginType);
					MainActivity.getPlugins().add(plugin);
				}
			}
		}
	}
}
