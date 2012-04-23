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
package org.mixare.lib.gui;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.FloatMath;

/**
 * The class stores a point of a two-dimensional coordinate system.
 * (values of the x and y axis)
 */

public class ScreenLine implements Parcelable{
	public float x, y;

	public ScreenLine() {
		set(0, 0);
	}

	public ScreenLine(float x, float y) {
		set(x, y);
	}

	public static final Parcelable.Creator<ScreenLine> CREATOR = new Parcelable.Creator<ScreenLine>() {
		public ScreenLine createFromParcel(Parcel in) {
			return new ScreenLine(in);
		}

		public ScreenLine[] newArray(int size) {
			return new ScreenLine[size];
		}
	};

	public ScreenLine(Parcel in){
		readParcel(in);
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void rotate(float t) {
		float xp = (float) FloatMath.cos(t) * x - (float) FloatMath.sin(t) * y;
		float yp = (float) FloatMath.sin(t) * x + (float) FloatMath.cos(t) * y;

		x = xp;
		y = yp;
	}

	public void add(float x, float y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(x);
		dest.writeFloat(y);
	}

	public void readParcel(Parcel in){
		x = in.readFloat();
		y = in.readFloat();
	}
}