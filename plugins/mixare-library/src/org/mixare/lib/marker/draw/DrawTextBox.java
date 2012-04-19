package org.mixare.lib.marker.draw;

import java.text.DecimalFormat;

import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.render.MixVector;

import android.graphics.Color;
import android.os.Parcel;

public class DrawTextBox extends DrawCommand {

	private static String CLASS_NAME = DrawTextBox.class.getName();
	
	private static String PROPERTY_NAME_VISIBLE = "visible";
	private static String PROPERTY_NAME_DISTANCE = "distance";
	private static String PROPERTY_NAME_TITLE = "title";
	private static String PROPERTY_NAME_UNDERLINE = "underline";
	private static String PROPERTY_NAME_TEXTBLOCK = "textblock";
	private static String PROPERTY_NAME_TEXTLAB = "textlab";
	private static String PROPERTY_NAME_SIGNMARKER = "signmarker";
	
	public static DrawTextBox init(Parcel in){
		Boolean visible = Boolean.valueOf(in.readString());
		Double distance = in.readDouble();
		String title = in.readString();
		Boolean underline = Boolean.valueOf(in.readString());
		ParcelableProperty textObjHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		ParcelableProperty textLabholder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		ParcelableProperty signMarkerHolder = in.readParcelable(ParcelableProperty.class.getClassLoader());
		return new DrawTextBox(visible, distance, title, underline, (TextObj)textObjHolder.getObject(),
								(Label)textLabholder.getObject(), (MixVector)signMarkerHolder.getObject() );
	}
	
	public DrawTextBox(Boolean visible, Double distance, String title, Boolean underline, TextObj textblock, Label textlab, MixVector signMarker){
		super(CLASS_NAME);
		setProperty(PROPERTY_NAME_VISIBLE, visible);
		setProperty(PROPERTY_NAME_DISTANCE, distance);
		setProperty(PROPERTY_NAME_TITLE, title);
		setProperty(PROPERTY_NAME_UNDERLINE, underline);
		setProperty(PROPERTY_NAME_TEXTBLOCK, new ParcelableProperty("org.mixare.lib.gui.TextObj", textblock));
		setProperty(PROPERTY_NAME_TEXTLAB, new ParcelableProperty("org.mixare.lib.gui.Label", textlab ));
		setProperty(PROPERTY_NAME_SIGNMARKER, new ParcelableProperty("org.mixare.lib.render.MixVector", signMarker));
	}
	
	@Override
	public void draw(PaintScreen dw) {
		
		double distance = getDoubleProperty(PROPERTY_NAME_DISTANCE);
		String title = getStringProperty(PROPERTY_NAME_TITLE);
		TextObj textBlock = (TextObj)getParcelableProperty(PROPERTY_NAME_TEXTBLOCK);
		Boolean underline = getBooleanProperty(PROPERTY_NAME_UNDERLINE);
		Boolean visible = getBooleanProperty(PROPERTY_NAME_VISIBLE);
		MixVector signMarker = getMixVectorProperty(PROPERTY_NAME_SIGNMARKER);
		Label txtlab = (Label)getParcelableProperty(PROPERTY_NAME_TEXTLAB);
		
		if(txtlab == null){
			txtlab = new Label();
		}
		
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		String textStr = "";

		DecimalFormat df = new DecimalFormat("@#");
		if (distance < 1000.0) {
			textStr = title + " (" + df.format(distance) + "m)";
		} else {
			distance = distance / 1000.0;
			textStr = title + " (" + df.format(distance) + "km)";
		}

		textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1,
				250, dw, underline);

		if (visible) {
			// based on the distance set the colour
			if (distance < 100.0) {
				textBlock.setBgColor(Color.argb(128, 52, 52, 52));
				textBlock.setBorderColor(Color.rgb(255, 104, 91));
			} else {
				textBlock.setBgColor(Color.argb(128, 0, 0, 0));
				textBlock.setBorderColor(Color.rgb(255, 255, 255));
			}

			txtlab.prepare(textBlock);

			dw.setStrokeWidth(1f);
			dw.setFill(true);
			dw.paintObj(txtlab, signMarker.x
					- txtlab.getWidth() / 2, signMarker.y
					+ maxHeight, 0, 1);

		}

	}

}
