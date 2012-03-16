package org.mixare.lib.marker;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.reality.PhysicalPlace;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;

import android.graphics.Bitmap;
import android.location.Location;

/**
 * IN PROGRESS
 * @author abdullahi
 *
 */
public class PluginMarker implements MarkerInterface {

	private String ID;
	protected String title;
	protected boolean underline = false;
	private String URL;
	protected PhysicalPlace mGeoLoc;
	// distance from user to mGeoLoc in meters
	protected double distance;
	// The marker color
	private int colour;

	private boolean active;
	// Draw properties
	protected boolean isVisible;

	public MixVector cMarker = new MixVector();
	protected MixVector signMarker = new MixVector();

	protected MixVector locationVector = new MixVector();
	private MixVector origin = new MixVector(0, 0, 0);
	private MixVector upV = new MixVector(0, 1, 0);

	private ScreenLine pPt = new ScreenLine();

	@Override
	public int compareTo(MarkerInterface another) {
		return this.getID().compareTo(another.getID());
	}

	@Override
	public String getURL() {
		return URL;
	}

	public double getLatitude() {
		return mGeoLoc.getLatitude();
	}

	public double getLongitude() {
		return mGeoLoc.getLongitude();
	}

	public double getAltitude() {
		return mGeoLoc.getAltitude();
	}

	public MixVector getLocationVector() {
		return locationVector;
	}

	@Override
	public double getDistance() {
		return distance;
	}

	@Override
	public void setDistance(double distance) {
		this.distance = distance;
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public void setID(String iD) {
		this.ID = iD;

	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setActive(boolean active) {
		this.active = active;

	}

	@Override
	public int getColour() {
		return colour;
	}

	@Override
	public boolean isVisible() {
		return isVisible;
	}

	@Override
	public String fClick(float x, float y) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxObjects() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setImage(Bitmap image) {
		// TODO Auto-generated method stub

	}

	@Override
	public Bitmap getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MixVector getCMarker() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MixVector getSignMarker() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getUnderline() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TextObj getTextBlock() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTextBlock(TextObj txtObj) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void draw(PaintScreen dw) {
		// TODO Auto-generated method stub

	}

	@Override
	public String[] remoteDraw() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void update(Location curGPSFix) {
		// An elevation of 0.0 probably means that the elevation of the
		// POI is not known and should be set to the users GPS height
		// Note: this could be improved with calls to
		// http://www.geonames.org/export/web-services.html#astergdem
		// to estimate the correct height with DEM models like SRTM, AGDEM or
		// GTOPO30
		if (mGeoLoc.getAltitude() == 0.0)
			mGeoLoc.setAltitude(curGPSFix.getAltitude());

		// compute the relative position vector from user position to POI
		// location
		PhysicalPlace.convLocToVec(curGPSFix, mGeoLoc, locationVector);
	}

	@Override
	public void calcPaint(Camera viewCam, float addX, float addY) {
		//cCMarker(origin, viewCam, addX, addY);
		//calcV(viewCam);
	}

	@Override
	public boolean fClick(float x, float y, MixContextInterface ctx,
			MixStateInterface state) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setTxtLab(Label txtLab) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Label getTxtLab() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
