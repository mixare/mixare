/**
 * 
 */

package org.mixare.data;

import org.mixare.MixListView;

/**
 * @author hannes
 *
 */
public class DataSource {

	/**
	 * 
	 */
	
	public enum DATASOURCE { WIKIPEDIA,  BUZZ, TWITTER, OSM, OWNURL}

	/** default URL */
	private static final String WIKI_BASE_URL = "http://ws.geonames.org/findNearbyWikipediaJSON";
	private static final String TWITTER_BASE_URL = "http://search.twitter.com/search.json";
	private static final String BUZZ_BASE_URL = "https://www.googleapis.com/buzz/v1/activities/search?alt=json&max-results=20";
	// OpenStreetMap API see http://wiki.openstreetmap.org/wiki/Xapi
	// eg. only railway stations:
	private static final String OSM_BASE_URL = "http://xapi.openstreetmap.org/api/0.6/node[railway=station]";
	//all objects that have names: 
	//String OSM_URL = "http://xapi.openstreetmap.org/api/0.6/node[name=*]"; 
	//caution! produces hugh amount of data (megabytes), only use with very small radii or specific queries

	public DataSource() {
		// TODO Auto-generated constructor stub
	}
	
	public static String createRequestURL(DATASOURCE source, double lat, double lon, double alt, float radius,String locale) {
		String ret="";
		switch(source) {
		
			case WIKIPEDIA: 
				ret= WIKI_BASE_URL + 
				"?lat=" + lat +
				"&lng=" + lon + 
				"&radius="+ radius +
				"&maxRows=50" +
				"&lang=" + locale; 
				break;
			
			case BUZZ: 
				ret= BUZZ_BASE_URL + 
				"&lat=" + lat+
				"&lon=" + lon + 
				"&radius="+ radius*1000;
				break;
			
			case TWITTER: 
				ret = TWITTER_BASE_URL +
				"?geocode=" + lat + "%2C" + lon + "%2C" + 
				Math.max(radius, 1.0) + "km" ; 
				break;
				
			case OSM: 
				ret = OSM_BASE_URL + XMLHandler.getOSMBoundingBox(lat, lon, radius);
			break;
			
			case OWNURL:
				ret = MixListView.customizedURL + "?"+ "latitude=" + Double.toString(lat) + "&longitude=" + Double.toString(lon) + "&altitude=" + Double.toString(alt);
			break;
			
		}
		return ret;
	}

}
