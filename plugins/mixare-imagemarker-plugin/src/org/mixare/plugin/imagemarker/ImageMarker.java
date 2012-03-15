/**
 * 
 */
package org.mixare.plugin.imagemarker;

import org.mixare.lib.Marker;
import org.mixare.lib.gui.PaintScreen;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;

/**
 * @author A.Egal
 *
 */
public class ImageMarker extends Marker{

	public static final int MAX_OBJECTS = 20;
	private Bitmap image; 
	public static final int OSM_URL_MAX_OBJECTS = 5;
	private int rectangleBackgroundColor = Color.argb(155, 255, 255, 255);


	public ImageMarker(String title, double latitude, double longitude,
			double altitude, String URL, int type, int color) {
		super(title, latitude, longitude, altitude, URL, type, color);
	}

	@Override
	public void update(Location curGPSFix) {
		super.update(curGPSFix);
	}

	@Override
	public int getMaxObjects() {
		return MAX_OBJECTS;
	}

	@Override
	public void draw(PaintScreen dw) {
		this.drawImage(dw);
		super.drawTextBlock(dw);
	}
	
	public String[] remoteDraw(){
		String[] result = {"drawImage", "drawTextBlock"};
		return result;
	}

	public void drawImage(PaintScreen dw){
		if (isVisible) {
			dw.setColor(rectangleBackgroundColor);
			dw.paintBitmap(image, signMarker.x - (image.getWidth()/2), signMarker.y - (image.getHeight() / 2));
		}
	}
	
	public void setImage(Bitmap image) {
		this.image = image;
	}

	public Bitmap getImage() {
		return image;
	}

	
	
}