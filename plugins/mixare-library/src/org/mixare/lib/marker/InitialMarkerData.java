package org.mixare.lib.marker;

import java.util.HashMap;
import java.util.Map;

import org.mixare.lib.marker.draw.ParcelableProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty;
import org.mixare.lib.marker.draw.PrimitiveProperty.primitive;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * An object that is responsible for transfering marker data from one plugin to another
 * 
 * This class can be used dataprocessors to fill marker data in it, so that this class
 * can help creating that marker for the data processor:
 * 
 * How to use:
 * -Set the marker name
 * -Fill the marker constructors in the constructor field: title,lat,lng,alt,link,type,color
 * -Add extra primitives with the setExtra(...) field
 * -Add extra non-primitives (but parcelable) objects with the setExtra(ParcelableProperty property)
 * 
 * @author A. Egal
 */
public class InitialMarkerData implements Parcelable{

	private String markerName;
	private Object[] constr; //constructor field
	private Map<String, PrimitiveProperty> extraPrimitives = new HashMap<String, PrimitiveProperty>();
	private Map<String, ParcelableProperty> extraParcelables = new HashMap<String, ParcelableProperty>();
	
	
	public InitialMarkerData(String title, double latitude, double longitude, 
			double altitude, String link, int type, int colour) {
		constr = new Object[7];
		constr[0] = title;
		constr[1] = latitude;
		constr[2] = longitude;
		constr[3] = altitude;
		constr[4] = link;
		constr[5] = type;
		constr[6] = colour;
	}
	
	public InitialMarkerData(Parcel in) {
		readParcel(in);
	}
	
	public void setMarkerName(String markerName) {
		this.markerName = markerName;
	}
	
	public String getMarkerName() {
		return markerName;
	}
	
	public static final Parcelable.Creator<InitialMarkerData> CREATOR = new Parcelable.Creator<InitialMarkerData>() {
		public InitialMarkerData createFromParcel(Parcel in) {
			return new InitialMarkerData(in);
		}

		public InitialMarkerData[] newArray(int size) {
			return new InitialMarkerData[size];
		}
	};
	
	public void setExtras(String name, int value){
		extraPrimitives.put(name, new PrimitiveProperty(primitive.INT.name(), value));
	}
	
	public void setExtras(String name, String value){
		extraPrimitives.put(name, new PrimitiveProperty(primitive.STRING.name(), value));
	}
	
	public void setExtras(String name, long value){
		extraPrimitives.put(name, new PrimitiveProperty(primitive.LONG.name(), value));
	}
	
	public void setExtras(String name, double value){
		extraPrimitives.put(name, new PrimitiveProperty(primitive.DOUBLE.name(), value));
	}
	
	public void setExtras(String name, float value){
		extraPrimitives.put(name, new PrimitiveProperty(primitive.FLOAT.name(), value));
	}
	
	public void setExtras(String name, byte[] value){
		extraPrimitives.put(name, new PrimitiveProperty(primitive.BYTE.name(), value));
	}
	
	public void setExtras(String name, boolean value){
		extraPrimitives.put(name, new PrimitiveProperty(primitive.STRING.name(), String.valueOf(value)));
	}
	
	public void setExtras(String name, ParcelableProperty value){
		extraParcelables.put(name, value);
	}
	
	public Object getExtras(String name){
		if(extraParcelables.containsKey(name)){
			return extraParcelables.get(name);
		}
		if(extraPrimitives.containsKey(name)){
			return extraPrimitives.get(name);
		}
		return null;
	}
	
	public Map<String, ParcelableProperty> getExtraParcelables() {
		return extraParcelables;
	}
	
	public Map<String, PrimitiveProperty> getExtraPrimitives() {
		return extraPrimitives;
	}

	public Object[] getConstr(){
		return constr;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(markerName);
		
		dest.writeString((String)constr[0]);
		dest.writeDouble((Double)constr[1]);
		dest.writeDouble((Double)constr[2]);
		dest.writeDouble((Double)constr[3]);
		dest.writeString((String)constr[4]);
		dest.writeInt((Integer)constr[5]);
		dest.writeInt((Integer)constr[6]);
		
		dest.writeInt(extraPrimitives.size());
		for (String s: extraPrimitives.keySet()) {
            dest.writeString(s);
            dest.writeParcelable(extraPrimitives.get(s), 0);
        }
		
		dest.writeInt(extraParcelables.size());
		for (String s: extraParcelables.keySet()) {
            dest.writeString(s);
            dest.writeParcelable(extraParcelables.get(s), 0);
        }
	}
	
	public void readParcel(Parcel in){
		markerName = in.readString();
		
		constr = new Object[7];
		constr[0] = in.readString(); 
		constr[1] = in.readDouble();
		constr[2] = in.readDouble();
		constr[3] = in.readDouble();
		constr[4] = in.readString();
		constr[5] = in.readInt();
		constr[6] = in.readInt();
		
		int countPrim = in.readInt();
        for (int i = 0; i < countPrim; i++) {
        	extraPrimitives.put(in.readString(),(PrimitiveProperty)in.readParcelable(PrimitiveProperty.class.getClassLoader()));
        }
        
		int countParc = in.readInt();
        for (int i = 0; i < countParc; i++) {
        	extraParcelables.put(in.readString(),(ParcelableProperty)in.readParcelable(ParcelableProperty.class.getClassLoader()));
        }
	}
}
