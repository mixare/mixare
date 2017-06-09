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

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.render.MixVector;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This drawcommand class, can be implemented by subclasses that can draw items on the screen
 * through the plugin. This class can be created on the plugin side, and is responsible
 * for transfering objects to the "core" side, through the parcelable interface. 
 * The main task for this class are
 * - storing values in the properties attribute, 
 * - converting it and send to the "core" (through the aidl structure and parcelable interface)
 * - reverting the original (implentation) class in the "core" that can draw items on the screen * 
 * @author A.Egal
 *
 */
public abstract class DrawCommand implements Parcelable{

	private String command;
	
	private final Map<String, Object> properties=new LinkedHashMap<String, Object>();
	
	protected DrawCommand(String command){
		this.command=command;
	}
	
	protected void setProperty(String key,Object value){
		properties.put(key, value);
	}
	
	public Object getProperty(String key){
		return properties.get(key);
	}
	
	public abstract void draw(PaintScreen dw);
		
	Boolean getBooleanProperty(String key){
		return (Boolean)properties.get(key);
	}
	
	MixVector getMixVectorProperty(String key){
		return (MixVector)getParcelableProperty(key);
	}
	
	Bitmap getBitmapProperty(String key){
		return (Bitmap)getParcelableProperty(key);
	}
	
	String getStringProperty(String key){
		return (String)properties.get(key);
	}
	
	Float getFloatProperty(String key){
		return (Float)properties.get(key);
	}
	
	Integer getIntegerProperty(String key){
		return (Integer)properties.get(key);
	}
	
	Double getDoubleProperty(String key){
		return (Double)properties.get(key);
	}
	
	byte[] getByteArrayProperty(String key){
		return (byte[])properties.get(key);
	}
	
	Parcelable getParcelableProperty(String key){
		Parcelable o = (Parcelable)properties.get(key);
		if(o instanceof ParcelableProperty){
			return ((ParcelableProperty)o).getObject();
		}
		return o;
	}
	
	public static final Parcelable.Creator<DrawCommand> CREATOR = new Parcelable.Creator<DrawCommand>() {
		public DrawCommand createFromParcel(Parcel in) {
			return DrawCommand.buildObject(in);
		}
		
		public DrawCommand[] newArray(int size) {
			return new DrawCommand[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(command);
		for (String s: properties.keySet()) {
			
            if(properties.get(s) instanceof String){
            	dest.writeString(getStringProperty(s));
            } 
            else if(properties.get(s) instanceof Boolean){
            	dest.writeString(String.valueOf(getBooleanProperty(s)));
            }
			else if(properties.get(s) instanceof Float){
				dest.writeFloat(getFloatProperty(s));
			}			            	
			else if(properties.get(s) instanceof Integer){
				dest.writeInt(getIntegerProperty(s));
			}
			else if(properties.get(s) instanceof Double){
				dest.writeDouble(getDoubleProperty(s));
			}
			else if(properties.get(s) instanceof Byte[]){
				dest.writeByteArray(getByteArrayProperty(s));
			}
			else if(properties.get(s) instanceof ParcelableProperty){
				dest.writeParcelable((ParcelableProperty)properties.get(s), 0);
			}
        }
	}
	
	@SuppressWarnings("rawtypes")
	public static DrawCommand buildObject(Parcel in){
		String className = in.readString();
		String methodName = "init";
		Class[] args = new Class[1];
		args[0] = Parcel.class;
		try {
			Method m = Class.forName(className).getMethod(methodName, args);
			return (DrawCommand) m.invoke(null, in);			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
}
