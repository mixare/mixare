package org.mixare.lib.marker;

import java.net.URLDecoder;

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.marker.draw.DrawCommand;
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
public abstract class PluginMarker{
	
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
	public Label txtLab = new Label();
	protected TextObj textBlock;
	
	public PluginMarker(String title, double latitude, double longitude, double altitude, String link, int type, int colour) {
		super();

		this.active = true;
		this.title = title;
		this.mGeoLoc = new PhysicalPlace(latitude,longitude,altitude);
		if (link != null && link.length() > 0) {
			URL = "webpage:" + URLDecoder.decode(link);
			this.underline = true;
		}
		this.colour = colour;

		this.ID=type +"##"+title;

	}	
	
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

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		this.ID = iD;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getColour() {
		return colour;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public String fClick(float x, float y) {
		try{
			if (isClickValid(x, y)) {
				return URL;
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		return null;
	}
	
	private boolean isClickValid(float x, float y) {
		float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				signMarker.x, signMarker.y);
		//if the marker is not active (i.e. not shown in AR view) we don't have to check it for clicks
		if (!isActive())
			return false;

		//TODO adapt the following to the variable radius!
		pPt.x = x - signMarker.x;
		pPt.y = y - signMarker.y;
		pPt.rotate(Math.toRadians(-(currentAngle + 90)));
		pPt.x += txtLab.getX();
		pPt.y += txtLab.getY();

		float objX = txtLab.getX() - txtLab.getWidth() / 2;
		float objY = txtLab.getY() - txtLab.getHeight() / 2;
		float objW = txtLab.getWidth();
		float objH = txtLab.getHeight();

		if (pPt.x > objX && pPt.x < objX + objW && pPt.y > objY
				&& pPt.y < objY + objH) {
			return true;
		} else {
			return false;
		}
	}

	public abstract int getMaxObjects();

	public abstract void setImage(Bitmap image);

	public abstract Bitmap getImage();

	public MixVector getCMarker() {
		return cMarker;
	}

	public MixVector getSignMarker() {
		return signMarker;
	}

	public boolean getUnderline() {
		return underline;
	}

	public String getTitle() {
		return title;
	}

	public abstract DrawCommand[] remoteDraw();
	
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

	public void calcPaint(Camera viewCam, float addX, float addY) {
		cCMarker(origin, viewCam, addX, addY);
		calcV(viewCam);
	}
	
	private void cCMarker(MixVector originalPoint, Camera viewCam, float addX, float addY) {
		// Temp properties
		MixVector tmpa = new MixVector(originalPoint);
		MixVector tmpc = new MixVector(upV);
		tmpa.add(locationVector); //3 
		tmpc.add(locationVector); //3
		tmpa.sub(viewCam.lco); //4
		tmpc.sub(viewCam.lco); //4
		tmpa.prod(viewCam.transform); //5
		tmpc.prod(viewCam.transform); //5

		MixVector tmpb = new MixVector();
		viewCam.projectPoint(tmpa, tmpb, addX, addY); //6
		cMarker.set(tmpb); //7
		viewCam.projectPoint(tmpc, tmpb, addX, addY); //6
		signMarker.set(tmpb); //7
	}

	private void calcV(Camera viewCam) {
		isVisible = true;
		
		if (cMarker.z < -1f) {
			isVisible = true;

			if (MixUtils.pointInside(cMarker.x, cMarker.y, 0, 0,
					viewCam.width, viewCam.height)) {
			}
		}
	}
	
	public void setTxtLab(Label txtLab) {
		this.txtLab = txtLab;
	}

	public Label getTxtLab() {
		return txtLab;
	}

}
