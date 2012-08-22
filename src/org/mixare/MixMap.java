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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class MixMap extends Activity {
	private static String mixMapPrefs = "mixmap";
	private static String mapUsage = "mapUsage";
	private static SharedPreferences prefs;
	
	public enum MAPS {
		GOOGLE,
		OSM
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("test", "Launch Map");
		super.onCreate(savedInstanceState);
		prefs = getSharedPreferences(mixMapPrefs, MODE_PRIVATE);
		Intent mapToLaunch;
		String map = prefs.getString(mapUsage, MAPS.GOOGLE.name());
		Log.d("test", map);
		if (map == MAPS.GOOGLE.name()) {
			Log.d("test", "Launch GoogleMaps");
			mapToLaunch = new Intent(this, GoogleMap.class);
		} else if (map == MAPS.OSM.name()){
			Log.d("test", "Launch OSM");
			mapToLaunch = new Intent(this, OsmMap.class);
		} else {
			Log.d("test", "Fallback");
			// fallback
			mapToLaunch = new Intent(this, GoogleMap.class);
			changeMap(MAPS.GOOGLE);
		}
		
		Intent intent = this.getIntent();
		if (intent.getBooleanExtra("center", false)) {
			mapToLaunch.putExtra("center", true);
			mapToLaunch.putExtra("latitude", intent.getDoubleExtra("latitude", 0.0));
			mapToLaunch.putExtra("longitude", intent.getDoubleExtra("longitude", 0.0));
		}
		
		startActivity(mapToLaunch);
		finish();
	}
	
	public static void changeMap(MAPS mapName) {
		Log.d("test", "Change map to: " + mapName.name());
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(mapUsage, mapName.name());
		editor.commit();
	}
}