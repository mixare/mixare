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
package org.mixare.render;

/**
 * The Camera class uses the Matrix and MixVector classes to store information
 * about camera properties like the view angle and calculates the coordinates of
 * the projected point
 * 
 */
public class Camera {

	public int width, height;

	public Matrix transform = new Matrix();
	public MixVector lco = new MixVector();

	float viewAngle;
	float dist;

	public Camera(int width, int height) {
		this(width, height, true);
	}

	public Camera(int width, int height, boolean init) {
		this.width = width;
		this.height = height;

		transform.toIdentity();
		lco.set(0, 0, 0);
	}

	public void setViewAngle(float viewAngle) {
		this.viewAngle = viewAngle;
		this.dist = (this.width / 2) / (float) Math.tan(viewAngle / 2);
	}

	public void setViewAngle(int width, int height, float viewAngle) {
		this.viewAngle = viewAngle;
		this.dist = (width / 2) / (float) Math.tan(viewAngle / 2);
	}

	public void projectPoint(MixVector orgPoint, MixVector prjPoint,
			float addX, float addY) {
		prjPoint.x = dist * orgPoint.x / -orgPoint.z;
		prjPoint.y = dist * orgPoint.y / -orgPoint.z;
		prjPoint.z = orgPoint.z;
		prjPoint.x = prjPoint.x + addX + width / 2;
		prjPoint.y = -prjPoint.y + addY + height / 2;
	}

	@Override
	public String toString() {
		return "CAM(" + width + "," + height + ")";
	}
}
