/**
 * 
 */
package org.mixare;

import java.io.IOException;
import java.net.MalformedURLException;

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.render.MixVector;
import org.mixare.lib.marker.draw.DrawImage;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.util.Log;



/**
 * TODO
 * @author devBinnooh
 * @author A.Egal
 */
public class ImageMarker extends LocalMarker {

	/** Int MaxObjects that can be create of this marker */
	public static final int maxObjects = 30;
	/** BitMap Image storage */
	private Bitmap image;
	
	public ImageMarker(String id, String title, double latitude,
			double longitude, double altitude, String link, int type, int colour) {
		super(id, title, latitude, longitude, altitude, link, type, colour);
		this.setImage(Bitmap.createBitmap(10, 10, Config.ARGB_4444)); //TODO set default Image if image not Available
	}
	
	public ImageMarker (String id, String title, double latitude,
			double longitude, double altitude, final String pageLink, 
			final int type, final int colour,final String imageOwner,
			final String ImageUrl) {
		super(id, title, latitude, longitude, altitude, pageLink, type, colour);
		
		try {
			
			final java.net.URL imageURI = new java.net.URL (ImageUrl);
			this.setImage(BitmapFactory.decodeStream(imageURI.openConnection().getInputStream()));
			
		}  catch (MalformedURLException e) {
			Log.e("Mixare - local ImageMarker", e.getMessage());
		} catch (IOException e) {
			Log.e("Mixare - local ImageMarker", e.getMessage());
		}finally {
			if (null == this.getImage()){
				this.setImage(Bitmap.createBitmap(10, 10, Config.ARGB_4444));
			}
		}
	}
	
	public void draw(final PaintScreen dw){
		drawImage(dw);
		drawTitle(dw);
	}

	/**
	 * Draw a title for image. It displays full title if title's length is less
	 * than 10 chars, otherwise, it displays the first 10 chars and concatenate
	 * three dots "..."
	 * 
	 * @param PaintScreen View Screen that title screen will be drawn into
	 */
	public void drawTitle(final PaintScreen dw) {
		if (isVisible) {
			final float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
			String textStr = MixUtils.shortenTitle(title,distance);
			textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1, 250,
					dw, underline);
			// dw.setColor(DataSource.getColor(type));
			final float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
					getSignMarker().x, getSignMarker().y);
			txtLab.prepare(textBlock);
			dw.setStrokeWidth(1f);
			dw.setFill(true);
			dw.paintObj(txtLab, getSignMarker().x - txtLab.getWidth() / 2,
					getSignMarker().y + maxHeight, currentAngle + 90, 1);
		}
	}
	
	/**
	 * Handles Drawing Images
	 * @param PaintScreen Screen that Image will be drawn into
	 */
	public void drawImage(final PaintScreen dw) {
		final DrawImage Image = new DrawImage(isVisible, cMarker, image);
		Image.draw(dw);
	}
	

	private MixVector getSignMarker() {
		return this.signMarker;
	}

	@Override
	public int getMaxObjects() {
		return maxObjects;
	}

	/**
	 * @return the image
	 */
	public Bitmap getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public void setImage(Bitmap image) {
		this.image = image;
	}

}
