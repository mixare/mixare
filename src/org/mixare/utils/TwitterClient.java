/*
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
package org.mixare.utils;

import org.mixare.MixView;

import android.util.Log;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * Twitter client wrapper used to make searches for tweets with the new Rest API
 * @author Michele Di Capua
 *
 */
public class TwitterClient 
{
	private static double lat=0;
	private static double lon=0;
	private static double rad=20;//default radius
		
	/**
	 * Set configuration for twitter client (lat, lon, radius) 
	 * @param latitude
	 * @param longitude
	 * @param radius
	 */
	public static void config(double latitude, double longitude, double radius)
	{
		lat = latitude;
		lon = longitude;
		rad = radius;
	}
	
	/**
	 * Query the twitter search API using oAuth 2.0
	 * @return
	 */
	public static String queryData() 
	{
		ConfigurationBuilder cb = new ConfigurationBuilder(); //to be configured in a properties...
		cb.setDebugEnabled(true)
		.setOAuthConsumerKey("mt10dv6tTKacqlm14lw5w")
		.setOAuthConsumerSecret("4kRV1E1XIU3kj4JQj2R5LE1yct0RRaRl9sB5PpPrB0")
		.setOAuthAccessToken("390019380-IQ5VdvUKvxY9JOsTToEU8ElCabebc76H9X2g3QX4")
		.setOAuthAccessTokenSecret("ghJn4LTfDr7uHUCsbt6ycmpeVTwwpa3hZnXyEjyZvs");
		cb.setJSONStoreEnabled(true);

		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();

		Query query = new Query();
		query = query.geoCode(new GeoLocation(lat,lon), rad, Query.KILOMETERS);

		String jsonArrayAsString = "{\"results\":[";//start
		try
		{
			QueryResult result = twitter.search(query);
			int size=0;
			for (Status status : result.getTweets()) 
			{
				{
					if (status.getGeoLocation()!= null) 
					{
						String jsonSingleObject = DataObjectFactory.getRawJSON(status);
						if (size==0) jsonArrayAsString += jsonSingleObject;
						else		 jsonArrayAsString += ","+jsonSingleObject;
						size++;
					}
				}
			}
			jsonArrayAsString += "]}";//close array
			return jsonArrayAsString;
		}
		catch(Exception e)
		{
			Log.e(MixView.TAG, "Error querying twitter data :"+e);
			e.printStackTrace();
		}
		return null;
	}
}