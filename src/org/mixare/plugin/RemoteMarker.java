package org.mixare.plugin;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.MixStateInterface;
import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.marker.MarkerInterface;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;
import org.mixare.lib.service.IMarkerService;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.RemoteException;


public class RemoteMarker implements MarkerInterface{

	private String markerName;
	private IMarkerService iMarkerService;
	
	private TextObj textObj;
	private Label txtLab = new Label();

	public RemoteMarker(String pluginName, IMarkerService iMarkerService){
		this.iMarkerService = iMarkerService;
	}

	public int getPid() {
		return 0;
	}

	public void buildMarker(String title, double latitude, double longitude, double altitude, String URL, int type, int color){
		try {
			this.markerName = iMarkerService.buildMarker(title, latitude, longitude, altitude, URL, type, color);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public String getPluginName(){
		try {
			return iMarkerService.getPluginName();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void calcPaint(Camera viewCam, float addX, float addY){	
		try {
			iMarkerService.calcPaint(markerName, viewCam, addX, addY);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void draw(PaintScreen dw) {
		try {
			String[] drawMethods = iMarkerService.remoteDraw(markerName);
			RemoteDrawer rd = new RemoteDrawer(this);
			rd.draw(drawMethods, dw);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}	
	}

	@Override
	public double getAltitude() {
		try {
			return iMarkerService.getAltitude(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getColour() {
		try {
			return iMarkerService.getColour(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getDistance() {
		try {
			return iMarkerService.getDistance(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getID() {
		try {
			return iMarkerService.getID(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getLatitude() {
		try {
			return iMarkerService.getLatitude(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MixVector getLocationVector() {
		try {
			return iMarkerService.getLocationVector(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public double getLongitude() {
		try {
			return iMarkerService.getLongitude(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int getMaxObjects() {
		try {
			return iMarkerService.getMaxObjects(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getTitle() {
		try {
			return iMarkerService.getTitle(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Label getTxtLab() {
		return txtLab;
	}

	@Override
	public String getURL() {
		try {
			return iMarkerService.getURL(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isActive() {
		try {
			return iMarkerService.isActive(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setActive(boolean active) {
		try {
			iMarkerService.setActive(markerName, active);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDistance(double distance) {
		try {
			iMarkerService.setDistance(markerName, distance);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setID(String iD) {
		try {
			iMarkerService.setID(markerName, iD);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void update(Location curGPSFix) {
		try {
			iMarkerService.update(markerName, curGPSFix);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fClick(float x, float y, MixContextInterface ctx, MixStateInterface state) {
		String url = fClick(x, y);
		return state.handleEvent(ctx, url);
	}

	@Override
	public boolean isVisible(){
		try {
			return iMarkerService.isVisible(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String fClick(float x, float y){
		try {
			this.setTxtLab(txtLab);
			return iMarkerService.fClick(markerName, x, y);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if(o instanceof RemoteMarker){
			RemoteMarker rm = (RemoteMarker)o;
			if(rm.markerName.equals(this.markerName)){
				return true;
			}
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return markerName.hashCode() + iMarkerService.hashCode();
	}

	@Override
	public int compareTo(MarkerInterface another) {
		if(another instanceof RemoteMarker){
			RemoteMarker rm = (RemoteMarker)another;
			this.getID().compareTo(rm.getID());
		}
		throw new IllegalArgumentException("param is not a instance of RemoteMarker");
	}

	@Override
	public void setImage(Bitmap image) {
		try {
			iMarkerService.setImage(markerName, image);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}	
	}

	@Override
	public Bitmap getImage(){
		try{
			return iMarkerService.getImage(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MixVector getCMarker(){
		try{
			return iMarkerService.getCMarker(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MixVector getSignMarker(){
		try{
			return iMarkerService.getSignMarker(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean getUnderline(){
		try{
			return iMarkerService.getUnderline(markerName);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TextObj getTextBlock(){
		return textObj;
	}

	@Override
	public void setTextBlock(TextObj txtObj){
		this.textObj = txtObj;
	}

	@Override
	public String[] remoteDraw() {
		return null;
	}

	@Override
	public void setTxtLab(Label txtLab) {
		try {
			iMarkerService.setTxtLab(markerName, txtLab);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

}