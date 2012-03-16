package org.mixare.plugin;

import java.lang.reflect.Method;
import java.text.DecimalFormat;

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;

import android.graphics.Color;
import android.graphics.Path;

public class RemoteDrawer {

	private RemoteMarker rm;

	public RemoteDrawer(RemoteMarker rm){
		this.rm = rm;
	}

	public void draw(String[] methods, PaintScreen dw){
		try{
			@SuppressWarnings("rawtypes")
			Class[] args = new Class[1];
			args[0] = PaintScreen.class;
			for(String method: methods){
				Method m = RemoteDrawer.class.getMethod(method, args);
				m.invoke(this, dw);
			}		
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void drawArrow(PaintScreen dw) {
		if (rm.isVisible()) {
			float currentAngle = MixUtils.getAngle(rm.getCMarker().x, rm.getCMarker().y, rm.getSignMarker().x, rm.getSignMarker().y);
			float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

			dw.setStrokeWidth(maxHeight / 10f);
			dw.setFill(false);

			Path arrow = new Path();
			float radius = maxHeight / 1.5f;
			float x=0;
			float y=0;
			arrow.moveTo(x-radius/3, y+radius);
			arrow.lineTo(x+radius/3, y+radius);
			arrow.lineTo(x+radius/3, y);
			arrow.lineTo(x+radius, y);
			arrow.lineTo(x, y-radius);
			arrow.lineTo(x-radius, y);
			arrow.lineTo(x-radius/3,y);
			arrow.close();
			dw.paintPath(arrow,rm.getCMarker().x,rm.getCMarker().y,radius*2,radius*2,currentAngle+90,1);			
		}
	}

	public void drawImage(PaintScreen dw){
		if (rm.isVisible()) {
			dw.setColor(Color.argb(155, 255, 255, 255));
			dw.paintBitmap(rm.getImage(), rm.getSignMarker().x - (rm.getImage().getWidth()/2), rm.getSignMarker().y - (rm.getImage().getHeight() / 2));
		}
	}

	public void drawCircle(PaintScreen dw) {
		if (rm.isVisible()) {
			float maxHeight = dw.getHeight();
			dw.setStrokeWidth(maxHeight / 100f);
			dw.setFill(false);

			//draw circle with radius depending on distance
			//0.44 is approx. vertical fov in radians 
			double angle = 2.0*Math.atan2(10, rm.getDistance());
			double radius = Math.max(Math.min(angle/0.44 * maxHeight, maxHeight),maxHeight/25f);

			dw.paintCircle(rm.getCMarker().x, rm.getCMarker().y, (float)radius);
		}
	}

	public void drawTextBlock(PaintScreen dw) {
		//TODO: grandezza cerchi e trasparenza
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		// TODO: change textblock only when distance changes

		String textStr = "";

		double d = rm.getDistance();
		DecimalFormat df = new DecimalFormat("@#");
		if (d < 1000.0) {
			textStr = rm.getTitle() + " (" + df.format(d) + "m)";
		} else {
			d = d / 1000.0;
			textStr = rm.getTitle() + " (" + df.format(d) + "km)";
		}

		rm.setTextBlock(new TextObj(textStr, Math.round(maxHeight / 2f) + 1, 250,
				dw, rm.getUnderline()));

		if (rm.isVisible()) {
			// based on the distance set the colour
			if (rm.getDistance() < 100.0) {
				rm.getTextBlock().setBgColor(Color.argb(128, 52, 52, 52));
				rm.getTextBlock().setBorderColor(Color.rgb(255, 104, 91));
			} else {
				rm.getTextBlock().setBgColor(Color.argb(128, 0, 0, 0));
				rm.getTextBlock().setBorderColor(Color.rgb(255, 255, 255));
			}
			
			rm.getTxtLab().prepare(rm.getTextBlock());
			
			dw.setStrokeWidth(1f);
			dw.setFill(true);
			dw.paintObj(rm.getTxtLab(), rm.getSignMarker().x - rm.getTxtLab().getWidth() / 2,
					rm.getSignMarker().y + maxHeight, 0, 1);

		}
	}


}