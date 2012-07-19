package org.mixare.data.convert;

import java.util.concurrent.ExecutionException;

import org.mixare.mgr.HttpTools;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Elevation {

	private static Elevation instance;
	String url = "http://api.geonames.org/";
	String username = "mixare";

	/**
	 * Allowing static access to the class
	 * 
	 * @return A non static class Elevation
	 */
	public static Elevation getElevation() {
		if (instance == null) {
			instance = new Elevation();
		}
		return instance;
	}

	/**
	 * Calculates the Elevation of up to 20 Points at ones
	 * 
	 * @param lats
	 *            The array of points latitude. Max length 20
	 * @param lngs
	 *            The array of points longitude. Max length 20
	 * @return A string array of elevations for each given point
	 */
	public String[] calcElevations(Double[] lats, Double[] lngs) {
		if (lats.length != lngs.length) {
			// throw new
		}

		StringBuilder latString = new StringBuilder();
		StringBuilder lngString = new StringBuilder();

		for (int i = 0; i < lngs.length; i++) {
			latString.append(lats[i] + ",");
			lngString.append(lngs[i] + ",");
		}

		String requestUrl = url + "srtm3?lats=" + latString.toString()
				+ "&lngs=" + lngString.toString() + "&username=" + username;
//		Log.d("test", requestUrl);
		
		Downloader downloader = new Downloader();
		String content = "";
		try {
			content = downloader.execute(requestUrl).get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content.split("\n");
	}

	/**
	 * Calculates the Elevation of a given point using the api of geonames.org
	 * 
	 * @param lat
	 *            The latitude of the point
	 * @param lng
	 *            The longitude of the point
	 * @return The elevation of the point
	 */
	public String calcElevation(double lat, double lng) {
		String requestUrl = url + "astergdem?lat=" + lat + "&lng=" + lng
				+ "&username=" + username;
		Downloader downloader = new Downloader();
		String content = "";
		try {
			content = downloader.execute(requestUrl).get();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}

	private class Downloader extends AsyncTask<String, Void, String> {
		/**
		 * Makes a request to the give URL using HttpTools to get the
		 * pageContent
		 * 
		 * @param requestUrl
		 *            The URL which should be downloaded
		 * @return The content of the website
		 */
		@Override
		protected String doInBackground(String... params) {
			String content = "";
			try {
				content = HttpTools.getPageContent(params[0]);
			} catch (Exception e) {
				Log.d("test", "Error downloading page content.");
			}
			return content;
		}
	}
}