/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
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
package org.mixare.lib.marker.draw;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A draw property which contains a parcelable object with its classname so it
 * can be loaded. This class should be used, when a plugin wants to send a 'unknown' parcelable object to the core
 * This is needed because the classname of a parcel should be known before the core can convert that into an object.
 * @author A. Egal
 */
public class ParcelableProperty implements Parcelable {

	private String className;
	private Parcelable object;

	public ParcelableProperty(String className, Parcelable object) {
		this.className = className;
		this.object = object;
	}

	public static final Parcelable.Creator<ParcelableProperty> CREATOR = new Parcelable.Creator<ParcelableProperty>() {
		public ParcelableProperty createFromParcel(Parcel in) {
			return new ParcelableProperty(in);
		}

		public ParcelableProperty[] newArray(int size) {
			return new ParcelableProperty[size];
		}
	};

	private ParcelableProperty(Parcel in) {
		className = in.readString();
		object = in.readParcelable(getClassLoader());
	}

	public ClassLoader getClassLoader() {
		try {
			return Class.forName(className).getClassLoader();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException();
		}
	}

	public Parcelable getObject() {
		return object;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(className);
		dest.writeParcelable(object, 0);
	}

}
