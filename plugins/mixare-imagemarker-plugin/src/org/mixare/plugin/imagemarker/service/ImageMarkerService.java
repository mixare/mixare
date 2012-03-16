package org.mixare.plugin.imagemarker.service;

import java.util.HashMap;
import java.util.Map;

import org.mixare.lib.gui.Label;
import org.mixare.lib.gui.TextObj;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.render.Camera;
import org.mixare.lib.render.MixVector;
import org.mixare.lib.service.IMarkerService;
import org.mixare.plugin.imagemarker.ImageMarker;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;

public class ImageMarkerService extends Service{
	
	static final String PLUGIN_NAME = "imagemarker";
	static final String CATEGORY_PLUGIN= "mixare.intent.category.MARKER_PLUGIN";
	private Map<String, Marker> markers = new HashMap<String, Marker>();
	private Integer count = 0;

	public void onStart(Intent intent, int startId) {
		super.onStart( intent, startId );
	}

	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

    private final IMarkerService.Stub binder = new IMarkerService.Stub() {

		@Override
		public int getPid() throws RemoteException {
			return 0;
		}

		@Override
		public String buildMarker(String title, double latitude, double longitude, double altitude, String URL, int type, int color)
				throws RemoteException {
			Marker marker = new ImageMarker(title, latitude, longitude, altitude, URL, type, color);
			String markerName = "imageMarker-"+count+"-"+marker.getID();
			markers.put(markerName, marker);
			return markerName;
		}

		@Override
		public String getPluginName() throws RemoteException {
			return PLUGIN_NAME;
		}

		@Override
		public void calcPaint(String markerName, Camera viewCam, float addX, float addY)
				throws RemoteException {
			markers.get(markerName).calcPaint(viewCam, addX, addY);			
		}

		@Override
		public String[] remoteDraw(String markerName) throws RemoteException {
			return markers.get(markerName).remoteDraw();
		}

		@Override
		public double getAltitude(String markerName) throws RemoteException {
			return markers.get(markerName).getAltitude();
		}

		@Override
		public int getColour(String markerName) throws RemoteException {
			return markers.get(markerName).getColour();
		}

		@Override
		public double getDistance(String markerName) throws RemoteException {
			return markers.get(markerName).getDistance();
		}

		@Override
		public String getID(String markerName) throws RemoteException {
			return markers.get(markerName).getID();
		}

		@Override
		public double getLatitude(String markerName) throws RemoteException {
			return markers.get(markerName).getLatitude();
		}

		@Override
		public MixVector getLocationVector(String markerName) throws RemoteException {
			return markers.get(markerName).getLocationVector();
		}

		@Override
		public double getLongitude(String markerName) throws RemoteException {
			return markers.get(markerName).getLongitude();
		}

		@Override
		public int getMaxObjects(String markerName) throws RemoteException {
			return markers.get(markerName).getMaxObjects();
		}

		@Override
		public String getTitle(String markerName) throws RemoteException {
			return markers.get(markerName).getTitle();
		}

		@Override
		public Label getTxtLab(String markerName) throws RemoteException {
			return markers.get(markerName).getTxtLab();
		}

		@Override
		public String getURL(String markerName) throws RemoteException {
			return markers.get(markerName).getURL();
		}

		@Override
		public boolean isActive(String markerName) throws RemoteException {
			return markers.get(markerName).isActive();
		}

		@Override
		public void setActive(String markerName, boolean active) throws RemoteException {
			markers.get(markerName).setActive(active);
		}

		@Override
		public void setDistance(String markerName, double distance) throws RemoteException {
			markers.get(markerName).setDistance(distance);
		}

		@Override
		public void setID(String markerName, String iD) throws RemoteException {
			markers.get(markerName).setID(iD);
		}

		@Override
		public void update(String markerName, Location curGPSFix) throws RemoteException {
			markers.get(markerName).update(curGPSFix);
		}

		@Override
		public void removeMarker(String markerName) throws RemoteException {
			markers.remove(markerName);			
		}

		@Override
		public String fClick(String markerName, float x, float y)
				throws RemoteException {
			return markers.get(markerName).fClick(x, y);
		}

		@Override
		public void setImage(String markerName, Bitmap bitmap) throws RemoteException {
			markers.get(markerName).setImage(bitmap);			
		}

		@Override
		public MixVector getCMarker(String markerName) throws RemoteException {
			return markers.get(markerName).getCMarker();
		}

		@Override
		public Bitmap getImage(String markerName) throws RemoteException {
			return markers.get(markerName).getImage();
		}

		@Override
		public MixVector getSignMarker(String markerName) throws RemoteException {
			return markers.get(markerName).getSignMarker();
		}

		@Override
		public TextObj getTextBlock(String markerName) throws RemoteException {
			return markers.get(markerName).getTextBlock();
		}

		@Override
		public boolean getUnderline(String markerName) throws RemoteException {
			return markers.get(markerName).getUnderline();
		}

		@Override
		public void setTextBlock(String markerName, TextObj txtBlock)
				throws RemoteException {
			markers.get(markerName).setTextBlock(txtBlock);
		}

		@Override
		public boolean isVisible(String markerName) throws RemoteException {
			return markers.get(markerName).isVisible();
		}

		public void setTxtLab(String markerName, Label txtLab) throws RemoteException {
			markers.get(markerName).setTxtLab(txtLab);
		}
    };

}