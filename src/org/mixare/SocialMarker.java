/**
 * 
 */
package org.mixare;

import org.mixare.data.DataSource.DATASOURCE;
import org.mixare.reality.PhysicalPlace;

import android.location.Location;

/**
 * @author hannes
 *
 */
public class SocialMarker extends Marker {
	
	public static final int MAX_OBJECTS=20;

	public SocialMarker(String title, double latitude, double longitude,
			double altitude, String URL, DATASOURCE datasource) {
		super(title, latitude, longitude, altitude, URL, datasource);
	}

	@Override
	public void update(Location curGPSFix, long time) {

		super.update(curGPSFix, time);
		
		// we want the social markers to be on the upper part of
		// your surrounding sphere so we set the height component of 
		// the position vector 300m above the user
		
		locationVector.y+=300;
	}


}
