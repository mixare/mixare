/**
 * 
 */
package org.mixare.plugin.imagemarker;

import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.marker.PluginMarker;
import org.mixare.lib.marker.draw.DrawCommand;
import org.mixare.lib.marker.draw.DrawImage;
import org.mixare.lib.marker.draw.DrawTextBox;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;

/**
 * @author A.Egal
 *
 */
public class ImageMarker extends PluginMarker{

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
	
	public DrawCommand[] remoteDraw(){
		DrawCommand[] dCommands = new DrawCommand[2];
		dCommands[0] = new DrawImage(isVisible, signMarker, image);
		dCommands[1] = new DrawTextBox(isVisible, distance, title, underline, textBlock, txtLab, signMarker);
		return dCommands;
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