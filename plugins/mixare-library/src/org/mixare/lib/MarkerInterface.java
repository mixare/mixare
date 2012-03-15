package org.mixare.lib;

import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;

import android.graphics.Bitmap;
import android.location.Location;

public interface MarkerInterface extends Comparable<MarkerInterface>{

	String getTitle();

	String getURL();

	double getLatitude();

	double getLongitude();

	double getAltitude();

	MixVector getLocationVector();

	void update(Location curGPSFix);

	void calcPaint(Camera viewCam, float addX, float addY);

	void draw(PaintScreen dw);

	String[] remoteDraw();

	double getDistance();

	void setDistance(double distance);

	String getID();

	void setID(String iD);

	boolean isActive();

	void setActive(boolean active);

	int getColour();
	
	public void setTxtLab(Label txtLab);

	Label getTxtLab();

	boolean isVisible();

	public String fClick(float x, float y);

	public boolean fClick(float x, float y, MixContextInterface ctx, MixStateInterface state);

	int getMaxObjects();

	void setImage(Bitmap image);

	Bitmap getImage();

	MixVector getCMarker();

	MixVector getSignMarker();

	boolean getUnderline();

	TextObj getTextBlock();

	void setTextBlock(TextObj txtObj);

}