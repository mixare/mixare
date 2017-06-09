/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\user\\Desktop\\thesis\\mixare\\mixarelib\\src\\main\\aidl\\org\\mixare\\lib\\service\\IMarkerService.aidl
 */
package org.mixare.lib.service;
/**
 * Android Interface Definition Language for contact between services in different threads,
 * In this case: The IMarkerService connects the mixare core with the markers of the plugins.
 */
public interface IMarkerService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.mixare.lib.service.IMarkerService
{
private static final java.lang.String DESCRIPTOR = "org.mixare.lib.service.IMarkerService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.mixare.lib.service.IMarkerService interface,
 * generating a proxy if needed.
 */
public static org.mixare.lib.service.IMarkerService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.mixare.lib.service.IMarkerService))) {
return ((org.mixare.lib.service.IMarkerService)iin);
}
return new org.mixare.lib.service.IMarkerService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getPid:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getPid();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_buildMarker:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
java.lang.String _arg1;
_arg1 = data.readString();
double _arg2;
_arg2 = data.readDouble();
double _arg3;
_arg3 = data.readDouble();
double _arg4;
_arg4 = data.readDouble();
java.lang.String _arg5;
_arg5 = data.readString();
int _arg6;
_arg6 = data.readInt();
int _arg7;
_arg7 = data.readInt();
java.lang.String _result = this.buildMarker(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6, _arg7);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_removeMarker:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.removeMarker(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getPluginName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getPluginName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getTitle:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getTitle(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getURL:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getURL(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getLatitude:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
double _result = this.getLatitude(_arg0);
reply.writeNoException();
reply.writeDouble(_result);
return true;
}
case TRANSACTION_getLongitude:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
double _result = this.getLongitude(_arg0);
reply.writeNoException();
reply.writeDouble(_result);
return true;
}
case TRANSACTION_getAltitude:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
double _result = this.getAltitude(_arg0);
reply.writeNoException();
reply.writeDouble(_result);
return true;
}
case TRANSACTION_getLocationVector:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.mixare.lib.render.MixVector _result = this.getLocationVector(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_update:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
android.location.Location _arg1;
if ((0!=data.readInt())) {
_arg1 = android.location.Location.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.update(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_calcPaint:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.mixare.lib.render.Camera _arg1;
if ((0!=data.readInt())) {
_arg1 = org.mixare.lib.render.Camera.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
float _arg2;
_arg2 = data.readFloat();
float _arg3;
_arg3 = data.readFloat();
this.calcPaint(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
case TRANSACTION_remoteDraw:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.mixare.lib.marker.draw.DrawCommand[] _result = this.remoteDraw(_arg0);
reply.writeNoException();
reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
return true;
}
case TRANSACTION_getDistance:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
double _result = this.getDistance(_arg0);
reply.writeNoException();
reply.writeDouble(_result);
return true;
}
case TRANSACTION_setDistance:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
double _arg1;
_arg1 = data.readDouble();
this.setDistance(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getID:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _result = this.getID(_arg0);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_setID:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
this.setID(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_isActive:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.isActive(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setActive:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _arg1;
_arg1 = (0!=data.readInt());
this.setActive(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getColour:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.getColour(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getMaxObjects:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
int _result = this.getMaxObjects(_arg0);
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_fClick:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.mixare.lib.marker.draw.ClickHandler _result = this.fClick(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_isVisible:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.isVisible(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setExtrasParc:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
org.mixare.lib.marker.draw.ParcelableProperty _arg2;
if ((0!=data.readInt())) {
_arg2 = org.mixare.lib.marker.draw.ParcelableProperty.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.setExtrasParc(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_setExtrasPrim:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
org.mixare.lib.marker.draw.PrimitiveProperty _arg2;
if ((0!=data.readInt())) {
_arg2 = org.mixare.lib.marker.draw.PrimitiveProperty.CREATOR.createFromParcel(data);
}
else {
_arg2 = null;
}
this.setExtrasPrim(_arg0, _arg1, _arg2);
reply.writeNoException();
return true;
}
case TRANSACTION_getCMarker:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.mixare.lib.render.MixVector _result = this.getCMarker(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getSignMarker:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.mixare.lib.render.MixVector _result = this.getSignMarker(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getUnderline:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.getUnderline(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setTxtLab:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.mixare.lib.gui.Label _arg1;
if ((0!=data.readInt())) {
_arg1 = org.mixare.lib.gui.Label.CREATOR.createFromParcel(data);
}
else {
_arg1 = null;
}
this.setTxtLab(_arg0, _arg1);
reply.writeNoException();
return true;
}
case TRANSACTION_getTxtLab:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
org.mixare.lib.gui.Label _result = this.getTxtLab(_arg0);
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.mixare.lib.service.IMarkerService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/** Request the process ID of this service. */
@Override public int getPid() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPid, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
//--other marker interface methods--//

@Override public java.lang.String buildMarker(int id, java.lang.String title, double latitude, double longitude, double altitude, java.lang.String URL, int type, int color) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(id);
_data.writeString(title);
_data.writeDouble(latitude);
_data.writeDouble(longitude);
_data.writeDouble(altitude);
_data.writeString(URL);
_data.writeInt(type);
_data.writeInt(color);
mRemote.transact(Stub.TRANSACTION_buildMarker, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void removeMarker(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_removeMarker, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String getPluginName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getPluginName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getTitle(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getTitle, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getURL(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getURL, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public double getLatitude(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
double _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getLatitude, _data, _reply, 0);
_reply.readException();
_result = _reply.readDouble();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public double getLongitude(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
double _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getLongitude, _data, _reply, 0);
_reply.readException();
_result = _reply.readDouble();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public double getAltitude(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
double _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getAltitude, _data, _reply, 0);
_reply.readException();
_result = _reply.readDouble();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public org.mixare.lib.render.MixVector getLocationVector(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.mixare.lib.render.MixVector _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getLocationVector, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.mixare.lib.render.MixVector.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void update(java.lang.String markerName, android.location.Location curGPSFix) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
if ((curGPSFix!=null)) {
_data.writeInt(1);
curGPSFix.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_update, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void calcPaint(java.lang.String markerName, org.mixare.lib.render.Camera viewCam, float addX, float addY) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
if ((viewCam!=null)) {
_data.writeInt(1);
viewCam.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeFloat(addX);
_data.writeFloat(addY);
mRemote.transact(Stub.TRANSACTION_calcPaint, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public org.mixare.lib.marker.draw.DrawCommand[] remoteDraw(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.mixare.lib.marker.draw.DrawCommand[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_remoteDraw, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArray(org.mixare.lib.marker.draw.DrawCommand.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public double getDistance(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
double _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getDistance, _data, _reply, 0);
_reply.readException();
_result = _reply.readDouble();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setDistance(java.lang.String markerName, double distance) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
_data.writeDouble(distance);
mRemote.transact(Stub.TRANSACTION_setDistance, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String getID(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getID, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setID(java.lang.String markerName, java.lang.String iD) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
_data.writeString(iD);
mRemote.transact(Stub.TRANSACTION_setID, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean isActive(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_isActive, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setActive(java.lang.String markerName, boolean active) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
_data.writeInt(((active)?(1):(0)));
mRemote.transact(Stub.TRANSACTION_setActive, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public int getColour(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getColour, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getMaxObjects(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getMaxObjects, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public org.mixare.lib.marker.draw.ClickHandler fClick(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.mixare.lib.marker.draw.ClickHandler _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_fClick, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.mixare.lib.marker.draw.ClickHandler.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isVisible(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_isVisible, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setExtrasParc(java.lang.String markerName, java.lang.String name, org.mixare.lib.marker.draw.ParcelableProperty parcelableProperty) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
_data.writeString(name);
if ((parcelableProperty!=null)) {
_data.writeInt(1);
parcelableProperty.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setExtrasParc, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void setExtrasPrim(java.lang.String markerName, java.lang.String name, org.mixare.lib.marker.draw.PrimitiveProperty primitiveProperty) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
_data.writeString(name);
if ((primitiveProperty!=null)) {
_data.writeInt(1);
primitiveProperty.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setExtrasPrim, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public org.mixare.lib.render.MixVector getCMarker(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.mixare.lib.render.MixVector _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getCMarker, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.mixare.lib.render.MixVector.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public org.mixare.lib.render.MixVector getSignMarker(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.mixare.lib.render.MixVector _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getSignMarker, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.mixare.lib.render.MixVector.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean getUnderline(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getUnderline, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setTxtLab(java.lang.String markerName, org.mixare.lib.gui.Label txtLab) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
if ((txtLab!=null)) {
_data.writeInt(1);
txtLab.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_setTxtLab, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public org.mixare.lib.gui.Label getTxtLab(java.lang.String markerName) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
org.mixare.lib.gui.Label _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(markerName);
mRemote.transact(Stub.TRANSACTION_getTxtLab, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = org.mixare.lib.gui.Label.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getPid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_buildMarker = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_removeMarker = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getPluginName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getTitle = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_getURL = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_getLatitude = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_getLongitude = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_getAltitude = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_getLocationVector = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_update = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_calcPaint = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_remoteDraw = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getDistance = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_setDistance = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_getID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_setID = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_isActive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_setActive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_getColour = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
static final int TRANSACTION_getMaxObjects = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
static final int TRANSACTION_fClick = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
static final int TRANSACTION_isVisible = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
static final int TRANSACTION_setExtrasParc = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
static final int TRANSACTION_setExtrasPrim = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
static final int TRANSACTION_getCMarker = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
static final int TRANSACTION_getSignMarker = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
static final int TRANSACTION_getUnderline = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
static final int TRANSACTION_setTxtLab = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
static final int TRANSACTION_getTxtLab = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
}
/** Request the process ID of this service. */
public int getPid() throws android.os.RemoteException;
//--other marker interface methods--//

public java.lang.String buildMarker(int id, java.lang.String title, double latitude, double longitude, double altitude, java.lang.String URL, int type, int color) throws android.os.RemoteException;
public void removeMarker(java.lang.String markerName) throws android.os.RemoteException;
public java.lang.String getPluginName() throws android.os.RemoteException;
public java.lang.String getTitle(java.lang.String markerName) throws android.os.RemoteException;
public java.lang.String getURL(java.lang.String markerName) throws android.os.RemoteException;
public double getLatitude(java.lang.String markerName) throws android.os.RemoteException;
public double getLongitude(java.lang.String markerName) throws android.os.RemoteException;
public double getAltitude(java.lang.String markerName) throws android.os.RemoteException;
public org.mixare.lib.render.MixVector getLocationVector(java.lang.String markerName) throws android.os.RemoteException;
public void update(java.lang.String markerName, android.location.Location curGPSFix) throws android.os.RemoteException;
public void calcPaint(java.lang.String markerName, org.mixare.lib.render.Camera viewCam, float addX, float addY) throws android.os.RemoteException;
public org.mixare.lib.marker.draw.DrawCommand[] remoteDraw(java.lang.String markerName) throws android.os.RemoteException;
public double getDistance(java.lang.String markerName) throws android.os.RemoteException;
public void setDistance(java.lang.String markerName, double distance) throws android.os.RemoteException;
public java.lang.String getID(java.lang.String markerName) throws android.os.RemoteException;
public void setID(java.lang.String markerName, java.lang.String iD) throws android.os.RemoteException;
public boolean isActive(java.lang.String markerName) throws android.os.RemoteException;
public void setActive(java.lang.String markerName, boolean active) throws android.os.RemoteException;
public int getColour(java.lang.String markerName) throws android.os.RemoteException;
public int getMaxObjects(java.lang.String markerName) throws android.os.RemoteException;
public org.mixare.lib.marker.draw.ClickHandler fClick(java.lang.String markerName) throws android.os.RemoteException;
public boolean isVisible(java.lang.String markerName) throws android.os.RemoteException;
public void setExtrasParc(java.lang.String markerName, java.lang.String name, org.mixare.lib.marker.draw.ParcelableProperty parcelableProperty) throws android.os.RemoteException;
public void setExtrasPrim(java.lang.String markerName, java.lang.String name, org.mixare.lib.marker.draw.PrimitiveProperty primitiveProperty) throws android.os.RemoteException;
public org.mixare.lib.render.MixVector getCMarker(java.lang.String markerName) throws android.os.RemoteException;
public org.mixare.lib.render.MixVector getSignMarker(java.lang.String markerName) throws android.os.RemoteException;
public boolean getUnderline(java.lang.String markerName) throws android.os.RemoteException;
public void setTxtLab(java.lang.String markerName, org.mixare.lib.gui.Label txtLab) throws android.os.RemoteException;
public org.mixare.lib.gui.Label getTxtLab(java.lang.String markerName) throws android.os.RemoteException;
}
