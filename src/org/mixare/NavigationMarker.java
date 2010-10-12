/**
 * 
 */
package org.mixare;

import org.mixare.data.DataSource;
import org.mixare.data.DataSource.DATASOURCE;
import org.mixare.gui.PaintScreen;

import android.graphics.Path;
import android.location.Location;

/**
 * @author hannes
 *
 */
public class NavigationMarker extends Marker {
	
	public static final int MAX_OBJECTS=20;

	public NavigationMarker(String title, double latitude, double longitude,
			double altitude, String URL, DATASOURCE datasource) {
		super(title, latitude, longitude, altitude, URL, datasource);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void update(Location curGPSFix, long time) {
	
		super.update(curGPSFix, time);
		
		// we want the navigation markers to be on the lower part of
		// your surrounding sphere so we set the height component of 
		// the position vector 1000m below the user
		
		locationVector.y+=-1000;
	}

	public void draw(PaintScreen dw) {
		drawArrow(dw);
		drawTextBlock(dw);
	}
	
	public void drawArrow(PaintScreen dw) {
		if (isVisible) {
			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);
			float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

			dw.setColor(DataSource.getColor(datasource));
			dw.setStrokeWidth(maxHeight / 10f);
			dw.setFill(false);
			
			Path arrow = new Path();
			float radius = maxHeight / 1.5f;
			float x=0;
			float y=0;
			arrow.moveTo(x-radius/3, y+radius);
			arrow.lineTo(x+radius/3, y+radius);
			arrow.lineTo(x+radius/3, y);
			arrow.lineTo(x+radius, y);
			arrow.lineTo(x, y-radius);
			arrow.lineTo(x-radius, y);
			arrow.lineTo(x-radius/3,y);
			arrow.close();
			dw.paintPath(arrow,cMarker.x,cMarker.y,radius*2,radius*2,currentAngle+90,1);			
		}
	}
}
