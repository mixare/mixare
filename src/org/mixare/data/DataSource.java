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

package org.mixare.data;

import org.mixare.MixListView;
import org.mixare.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

/**
 * The DataSource class is able to create the URL where the information about a
 * place can be found.
 * 
 * @author hannes
 * 
 */
public class DataSource {
	
	// Datasource and dataformat are not the same. datasource is where the data comes from
	// and dataformat is how the data is formatted. 
	// this is necessary for example when you have multiple datasources with the same
	// dataformat
	public enum DATASOURCE { WIKIPEDIA, BUZZ, TWITTER, OSM, OWNURL};
	public enum DATAFORMAT { WIKIPEDIA, BUZZ, TWITTER, OSM, MIXARE};	

	/** default URL */
	private static final String WIKI_BASE_URL = "http://ws.geonames.org/findNearbyWikipediaJSON";
	//private static final String WIKI_BASE_URL =	"file:///sdcard/wiki.json";
	private static final String TWITTER_BASE_URL = "http://search.twitter.com/search.json";
	private static final String BUZZ_BASE_URL = "https://www.googleapis.com/buzz/v1/activities/search?alt=json&max-results=20";
	// OpenStreetMap API see http://wiki.openstreetmap.org/wiki/Xapi
	// eg. only railway stations:
	//private static final String OSM_BASE_URL =	"http://www.informationfreeway.org/api/0.6/node[railway=station]";
	//private static final String OSM_BASE_URL =	"http://xapi.openstreetmap.org/api/0.6/node[railway=station]";
	private static final String OSM_BASE_URL =		"http://osmxapi.hypercube.telascience.org/api/0.6/node[railway=station]";
	//all objects that have names: 
	//String OSM_URL = "http://xapi.openstreetmap.org/api/0.6/node[name=*]"; 
	//caution! produces hugh amount of data (megabytes), only use with very small radii or specific queries

	public static Bitmap twitterIcon;
	public static Bitmap buzzIcon;
	
	public DataSource() {
		
	}
	
	public static void createIcons(Resources res) {
		twitterIcon=BitmapFactory.decodeResource(res, R.drawable.twitter);
		buzzIcon=BitmapFactory.decodeResource(res, R.drawable.buzz);
	}
	
	public static Bitmap getBitmap(DATASOURCE ds) {
		Bitmap bitmap=null;
		switch (ds) {
			case TWITTER: bitmap=twitterIcon; break;
			case BUZZ: bitmap=buzzIcon; break;
		}
		return bitmap;
	}
	
	public static DATAFORMAT dataFormatFromDataSource(DATASOURCE ds) {
		DATAFORMAT ret;
		switch (ds) {
			case WIKIPEDIA: ret=DATAFORMAT.WIKIPEDIA; break;
			case BUZZ: ret=DATAFORMAT.BUZZ; break;
			case TWITTER: ret=DATAFORMAT.TWITTER; break;
			case OSM: ret=DATAFORMAT.OSM; break;
			case OWNURL: ret=DATAFORMAT.MIXARE; break;
			default: ret=DATAFORMAT.MIXARE; break;
		}
		return ret;
	}
	
	public static String createRequestURL(DATASOURCE source, double lat, double lon, double alt, float radius,String locale) {
		String ret="";
		switch(source) {
		
			case WIKIPEDIA: 
				ret= WIKI_BASE_URL;
			break;
			
			case BUZZ: 
				ret= BUZZ_BASE_URL;
			break;
			
			case TWITTER: 
				ret = TWITTER_BASE_URL;			
			break;
				
			case OSM: 
				ret = OSM_BASE_URL;
			break;
			
			case OWNURL:
				ret = MixListView.customizedURL;
			break;
			
		}
		if (!ret.startsWith("file://")) {
			switch(source) {
			
			case WIKIPEDIA: 
				ret+=
				"?lat=" + lat +
				"&lng=" + lon + 
				"&radius="+ radius +
				"&maxRows=50" +
				"&lang=" + locale; 
			break;
			
			case BUZZ: 
				ret+= 
				"&lat=" + lat+
				"&lon=" + lon + 
				"&radius="+ radius*1000;
			break;
			
			case TWITTER: 
				ret+=
				"?geocode=" + lat + "%2C" + lon + "%2C" + 
				Math.max(radius, 1.0) + "km" ;				
			break;
				
			case OSM: 
				ret+= XMLHandler.getOSMBoundingBox(lat, lon, radius);
			break;
			
			case OWNURL:
				ret+=
				"?latitude=" + Double.toString(lat) + 
				"&longitude=" + Double.toString(lon) + 
				"&altitude=" + Double.toString(alt) +
				"&radius=" + Double.toString(radius);
			break;
			
			}
			
		}
		
		return ret;
	}
	
	public static int getColor(DATASOURCE datasource) {
		int ret;
		switch(datasource) {
			case BUZZ:		ret=Color.rgb(4, 228, 20); break;
			case TWITTER:	ret=Color.rgb(50, 204, 255); break;
			case OSM:		ret=Color.rgb(255, 168, 0); break;
			case WIKIPEDIA:	ret=Color.RED; break;
			default:		ret=Color.WHITE; break;
		}
		return ret;
	}

}
