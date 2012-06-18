/**
 * 
 */
package org.mixare;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.text.DecimalFormat;

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.render.MixVector;
import org.mixare.lib.marker.draw.DrawImage;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;


/**
 * TODO
 * @author devBinnooh
 * @author A.Egal
 */
public class ImageMarker extends LocalMarker {

	/** Int MaxObjects that can be create of this marker */
	private static final int maxObjects = 30;
	private static final int IO_BUFFER_SIZE = 1024;
	private static final boolean FLAG_DECODE_PHOTO_STREAM_WITH_SKIA = false;
	/** BitMap Image storage */
	private final Bitmap image;
	
	public ImageMarker(String id, String title, double latitude,
			double longitude, double altitude, String link, int type, int colour) {
		super(id, title, latitude, longitude, altitude, link, type, colour);
		this.image = Bitmap.createBitmap(10, 10, Config.ARGB_4444); //TODO set default Image if image not Available
	}
	

	/**
	 * Draw a title for image. It displays full title if title's length is less
	 * than 10 chars, otherwise, it displays the first 10 chars and concatenate
	 * three dots "..."
	 * 
	 * @param PaintScreen View Screen that title screen will be drawn into
	 */
	public void drawTitle(final PaintScreen dw) {
		final float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		// TODO: change textblock only when distance changes
		String textStr = "";
		double d = distance;
		final DecimalFormat df = new DecimalFormat("@#");
		String imageTitle = "";
		if (title.length() > 10) {
			imageTitle = title.substring(0, 10) + "...";
		} else {
			imageTitle = title;
		}
		if (d < 1000.0) {
			textStr = imageTitle + " (" + df.format(d) + "m)";
		} else {
			d = d / 1000.0;
			textStr = imageTitle + " (" + df.format(d) + "km)";
		}
		textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1, 250,
				dw, underline);

		if (isVisible) {
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
		final DrawImage Image = new DrawImage(isActive(), cMarker, image);
		Image.draw(dw);
//		if (isVisible) {
//			dw.setStrokeWidth(dw.getHeight() / 100f);
//			dw.setFill(false);
//			dw.setColor(Color.WHITE);
//			dw.paintBitmap(getImage(), (float) (getSignMarker().x - (getImage().getWidth() / 2f)),
//					(float) (getSignMarker().y - (getImage().getHeight() / 2f)));
//
//		}
	}
	
	public Bitmap getBitmapFromURL(final String src) {

//		InputStream input = null;
//		BufferedOutputStream out = null;
//		Bitmap myBitmap = null;
//		try {
//			final URI url = new URI(src);
//			final HttpURLConnection connection = (HttpURLConnection) url
//					.openConnection();
//			connection.setDoInput(true);
//			connection.connect();
//			input = new BufferedInputStream(connection.getInputStream(),
//					IO_BUFFER_SIZE);
//			if (FLAG_DECODE_PHOTO_STREAM_WITH_SKIA) {
//				myBitmap = BitmapFactory.decodeStream(input);
//			} else {
//				final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
//				out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
//				copy(input, out);
//				out.flush();
//
//				final byte[] data = dataStream.toByteArray();
//				myBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//			}
//		} catch (final IOException e) {
//			Log.e("Mixare - LocalImageMarker", e.getMessage());
//			//return null;
//		} finally {
//			closeStream(input);
//			closeStream(out);
//		}
		return image;
	}

	/**
	 * Closes the specified stream.
	 * 
	 * @param stream The stream to close.
	 */
	private static void closeStream(final Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (final IOException e) {
				android.util.Log
						.e("Mixare - LocalImageMarker", "Could not close stream", e);
			}
		}
	}

	private static void copy(final InputStream input,
			final BufferedOutputStream out) {
		final byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		try {
			while ((read = input.read(b)) != -1) {
				out.write(b, 0, read);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	private MixVector getSignMarker() {
		return this.signMarker;
	}

	@Override
	public int getMaxObjects() {
		return maxObjects;
	}

}
