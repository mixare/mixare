package org.mixare.lib.gui;

import android.os.Parcel;
import android.os.Parcelable;

public class Label implements ScreenObj, Parcelable {
	private float x, y;
	private float width, height;
	private ScreenObj obj;

	public void prepare(ScreenObj drawObj) {
		obj = drawObj;
		float w = obj.getWidth();
		float h = obj.getHeight();

		x = w / 2;
		y = 0;

		width = w * 2;
		height = h * 2;
	}
	
	public Label(){
		
	}

	public static final Parcelable.Creator<Label> CREATOR = new Parcelable.Creator<Label>() {
		public Label createFromParcel(Parcel in) {
			return new Label(in);
		}

		public Label[] newArray(int size) {
			return new Label[size];
		}
	};

	public Label(Parcel in){
		readParcel(in);
	}
	
	public void paint(PaintScreen dw) {
		dw.paintObj(obj, x, y, 0, 1);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(x);
		dest.writeFloat(y);
		dest.writeFloat(width);
		dest.writeFloat(height);	
	}

	public void readParcel(Parcel in){
		x = in.readFloat();
		x = in.readFloat();
		width = in.readFloat();
		height = in.readFloat();
	}

	@Override
	public int describeContents() {
		return 0;
	}
}