/**
 * 
 */
package org.mixare;

import org.mixare.data.DataSource;
import org.mixare.data.DataSource.DATASOURCE;
import org.mixare.gui.PaintScreen;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

}
