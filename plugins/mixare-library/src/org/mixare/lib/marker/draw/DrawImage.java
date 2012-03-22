package org.mixare.lib.marker.draw;

import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.render.MixVector;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;

public class DrawImage extends DrawCommand{
	
	private static String CLASS_NAME = "org.mixare.lib.marker.draw.DrawImage";
	
	private static String PROPERTY_NAME_VISIBLE = "visible";
	private static String PROPERTY_NAME_SIGNMARKER = "signMarker";
	private static String PROPERTY_NAME_IMAGE = "image";
	
	public static DrawImage init(Parcel in){
		Boolean visible = Boolean.valueOf(in.readString());
		ParcelableProperty signMarkerHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		ParcelableProperty bitmapHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		return new DrawImage(visible, (MixVector)signMarkerHolder.getObject(), (Bitmap)bitmapHolder.getObject());
	}
	
	public DrawImage(boolean visible,MixVector signMarker, Bitmap image) {
		super(CLASS_NAME);
		setProperty(PROPERTY_NAME_VISIBLE, visible);
		setProperty(PROPERTY_NAME_SIGNMARKER, new ParcelableProperty("org.mixare.lib.render.MixVector", signMarker));
		setProperty(PROPERTY_NAME_IMAGE,  new ParcelableProperty("android.graphics.Bitmap",image));
	}
	
	@Override
	public void draw(PaintScreen dw){
		if (getBooleanProperty(PROPERTY_NAME_VISIBLE)) {
			MixVector signMarker = getMixVectorProperty(PROPERTY_NAME_SIGNMARKER);
			Bitmap bitmap = getBitmapProperty(PROPERTY_NAME_IMAGE);
			
			dw.setColor(Color.argb(155, 255, 255, 255));
			try{
			dw.paintBitmap(bitmap, signMarker.x - (bitmap.getWidth()/2), signMarker.y - (bitmap.getHeight() / 2));
			}catch(NullPointerException ne){
				throw new RuntimeException(ne);
			}
		}
	}	
	
}
