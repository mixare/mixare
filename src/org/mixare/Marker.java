/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

import org.mixare.gui.PaintScreen;
import org.mixare.gui.ScreenObj;
import org.mixare.gui.ScreenLine;
import org.mixare.gui.TextObj;
import org.mixare.reality.PhysicalPlace;
import org.mixare.render.Camera;
import org.mixare.render.MixVector;

import android.graphics.Color;
import android.location.Location;

public class Marker {
	//	// XML properties
	public String mOnPress;
	public PhysicalPlace mGeoLoc = new PhysicalPlace();

	// State properties
	float locX, locY, locZ;

	// Draw properties
	boolean isVisible, isLookingAt, isNear;
	float deltaCenter;
	MixVector cMarker = new MixVector();
	MixVector signMarker = new MixVector();
	MixVector oMarker = new MixVector();
	static int color = Color.rgb(255, 0, 0), decInnerColor = Color.rgb(255, 0, 0), decWorkColor = Color.rgb(50, 50, 255);

	Label txtLab = new Label();
	
	// Temp properties
	MixVector tmpa = new MixVector();
	MixVector tmpb = new MixVector();
	MixVector tmpc = new MixVector();
	
	public MixVector loc = new MixVector();
	MixVector origin = new MixVector(0, 0, 0);
	MixVector upV = new MixVector(0, 1, 0);
	ScreenLine pPt = new ScreenLine();


	void cCMarker(MixVector originalPoint, Camera viewCam, float addX,
			float addY) {
		tmpa.set(originalPoint); //1
		tmpc.set(upV); 
		tmpa.add(loc); //3
		tmpc.add(loc); //3
		tmpa.sub(viewCam.lco); //4
		tmpc.sub(viewCam.lco); //4
		tmpa.prod(viewCam.transform); //5
		tmpc.prod(viewCam.transform); //5

		viewCam.projectPoint(tmpa, tmpb, addX, addY); //6
		cMarker.set(tmpb); //7
		viewCam.projectPoint(tmpc, tmpb, addX, addY); //6
		signMarker.set(tmpb); //7
		
	}



	void calcV(Camera viewCam) {
		isVisible = false;
		isLookingAt = false;
		deltaCenter = Float.MAX_VALUE;

		if (cMarker.z < -1f) {
			isVisible = true;

			if (MixUtils.pointInside(cMarker.x, cMarker.y, 0, 0,
					viewCam.width, viewCam.height)) {

				float xDist = cMarker.x - viewCam.width / 2;
				float yDist = cMarker.y - viewCam.height / 2;
				float dist = xDist * xDist + yDist * yDist;

				deltaCenter = (float) Math.sqrt(dist);

				if (dist < 50 * 50) {
					isLookingAt = true;
				}
			}
		}
	}


	void update(Location curGPSFix, long time) {
		PhysicalPlace.convLocToVec(curGPSFix, mGeoLoc, loc);
	}

	void calcPaint(Camera viewCam, float addX, float addY) {
		cCMarker(origin, viewCam, addX, addY);
		calcV(viewCam);
	}

	void calcPaint(Camera viewCam) {
		cCMarker(origin, viewCam, 0, 0);
	}

	boolean isClickValid(float x, float y) {
		float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				signMarker.x, signMarker.y);

		pPt.x = x - signMarker.x;
		pPt.y = y - signMarker.y;
		pPt.rotate(Math.toRadians(-(currentAngle + 90)));
		pPt.x += txtLab.x;
		pPt.y += txtLab.y;

		float objX = txtLab.x - txtLab.getWidth() / 2;
		float objY = txtLab.y - txtLab.getHeight() / 2;
		float objW = txtLab.getWidth();
		float objH = txtLab.getHeight();

		if (pPt.x > objX && pPt.x < objX + objW && pPt.y > objY
				&& pPt.y < objY + objH) {
			return true;
		} else {
			return false;
		}
	}

	public String mText;
	TextObj textBlock;

	void draw(PaintScreen dw) {

		//TODO: grandezza cerchi e trasparenza
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		if (textBlock == null) {
			textBlock = new TextObj(mText, Math.round(maxHeight / 2f) + 1,
					160, dw);
		}

		if (isVisible) {
			//default color
			dw.setColor(color);
			if(MixListView.getDataSource()=="Wikipedia")
				dw.setColor(Color.rgb(255, 0, 0));
			if(MixListView.getDataSource()=="Buzz")
				dw.setColor(Color.rgb(4, 228, 20));
			if(MixListView.getDataSource()=="Twitter")
				dw.setColor(Color.rgb(50, 204, 255));
			
			dw.setStrokeWidth(maxHeight / 10f);
			dw.setFill(false);
			dw.paintCircle(cMarker.x, cMarker.y, maxHeight / 1.5f);

			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);

			txtLab.prepare(textBlock);

			dw.setStrokeWidth(1f);
			dw.setFill(true);
			dw.paintObj(txtLab, signMarker.x - txtLab.getWidth()
					/ 2, signMarker.y + maxHeight, currentAngle + 90, 1);
		}
	}

	boolean fClick(float x, float y, MixContext ctx, MixState state) {
		boolean evtHandled = false;

		if (isClickValid(x, y)) {
			evtHandled = state.handleEvent(ctx, mOnPress);
		}
		return evtHandled;
	}
	
	public String getText(){
		return mText;
	}
	public String getURL(){
		return mOnPress;
	}
}


class Label implements ScreenObj {
	float x, y, w, h;
	float width, height;
	ScreenObj obj;

	public void prepare(ScreenObj drawObj) {
		obj = drawObj;
		w = obj.getWidth();
		h = obj.getHeight();

		x = w / 2;
		y = 0;

		width = w * 2;
		height = h * 2;
	}

	public void paint(PaintScreen dw) {
		dw.paintObj(obj, x, y, 0, 1);
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	
}


/**
 * Compares the markers. The closer they are the higher in the stack.
 * @author daniele
 *
 */
class MarkersOrder implements java.util.Comparator<Object> {
	public int compare(Object left, Object right) {
		Marker leftPm = (Marker) left;
		Marker rightPm = (Marker) right;

		return Float.compare(leftPm.cMarker.z, rightPm.cMarker.z);
	}
}
