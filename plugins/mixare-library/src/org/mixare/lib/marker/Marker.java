package org.mixare.lib.marker;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;

import android.location.Location;

/**
 * The marker interface.
 * @author A. Egal
 */
public interface Marker extends Comparable<Marker>{

	String getTitle();

	String getURL();

	double getLatitude();

	double getLongitude();

	double getAltitude();

	MixVector getLocationVector();

	void update(Location curGPSFix);

	void calcPaint(Camera viewCam, float addX, float addY);

	void draw(PaintScreen dw);

	double getDistance();

	void setDistance(double distance);

	String getID();

	void setID(String iD);

	boolean isActive();

	void setActive(boolean active);

	int getColour();
	
	public void setTxtLab(Label txtLab);

	Label getTxtLab();

	public boolean fClick(float x, float y, MixContextInterface ctx, MixStateInterface state);

	int getMaxObjects();
	
	void setExtras(String name, ParcelableProperty parcelableProperty);
	
	void setExtras(String name, PrimitiveProperty primitiveProperty);

}