/**
 * 
 */
package org.mixare;

import org.mixare.data.DataSource;
import org.mixare.data.DataSource.DATASOURCE;
import org.mixare.gui.PaintScreen;

import android.graphics.Bitmap;
import android.location.Location;

/**
 * @author hannes
 *
 */
public class SocialMarker extends Marker {
	
	public static final int MAX_OBJECTS=15;

	public SocialMarker(String title, double latitude, double longitude,
			double altitude, String URL, DATASOURCE datasource) {
		super(title, latitude, longitude, altitude, URL, datasource);
	}

	@Override
	public void update(Location curGPSFix) {

		//0.35 radians ~= 20 degree
		//0.85 radians ~= 45 degree
		//minAltitude = sin(0.35)
		//maxAltitude = sin(0.85)
		
		// we want the social markers to be on the upper part of
		// your surrounding sphere 
		double altitude = curGPSFix.getAltitude()+Math.sin(0.35)*distance+Math.sin(0.4)*(distance/(MixView.dataView.getRadius()*1000f/distance));
		mGeoLoc.setAltitude(altitude);
		super.update(curGPSFix);

	}

	@Override
	public void draw(PaintScreen dw) {

		drawTextBlock(dw);

		if (isVisible) {
			float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
			Bitmap bitmap = DataSource.getBitmap(datasource);
			if(bitmap!=null) {
				dw.paintBitmap(bitmap, cMarker.x - maxHeight/1.5f, cMarker.y - maxHeight/1.5f);				
			}
			else {
				dw.setStrokeWidth(maxHeight / 10f);
				dw.setFill(false);
				dw.setColor(DataSource.getColor(datasource));
				dw.paintCircle(cMarker.x, cMarker.y, maxHeight / 1.5f);
			}
		}
	}

	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}

}
