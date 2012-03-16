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
package org.mixare.lib.render;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The Camera class uses the Matrix and MixVector classes to store information
 * about camera properties like the view angle and calculates the coordinates of
 * the projected point
 * 
 */
public class Camera implements Parcelable {

	public static final float DEFAULT_VIEW_ANGLE = (float) Math.toRadians(45);

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

	public Camera(Parcel in) {
		readFromParcel(in);
	}

	public static final Parcelable.Creator<Camera> CREATOR = new Parcelable.Creator<Camera>() {
		public Camera createFromParcel(Parcel in) {
			return new Camera(in);
		}

		public Camera[] newArray(int size) {
			return new Camera[size];
		}
	};

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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeInt(width);
		dest.writeInt(height);
		dest.writeParcelable(transform, 0);
		dest.writeParcelable(lco, 0);
		dest.writeFloat(viewAngle);
		dest.writeFloat(dist);
	}

	public void readFromParcel(Parcel in) {
		width = in.readInt();
		height = in.readInt();
		transform = in.readParcelable(Matrix.class.getClassLoader());
		lco = in.readParcelable(MixVector.class.getClassLoader());
		viewAngle = in.readFloat();
		dist = in.readFloat();
	}
}