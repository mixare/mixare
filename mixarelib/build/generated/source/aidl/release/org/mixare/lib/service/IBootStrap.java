/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\user\\Desktop\\thesis\\mixare\\mixarelib\\src\\main\\aidl\\org\\mixare\\lib\\service\\IBootStrap.aidl
 */
package org.mixare.lib.service;
public interface IBootStrap extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.mixare.lib.service.IBootStrap
{
private static final java.lang.String DESCRIPTOR = "org.mixare.lib.service.IBootStrap";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.mixare.lib.service.IBootStrap interface,
 * generating a proxy if needed.
 */
public static org.mixare.lib.service.IBootStrap asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.mixare.lib.service.IBootStrap))) {
return ((org.mixare.lib.service.IBootStrap)iin);
}
return new org.mixare.lib.service.IBootStrap.Stub.Proxy(obj);
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
case TRANSACTION_getZIndex:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getZIndex();
reply.writeNoException();
reply.writeInt(_result);
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
case TRANSACTION_getActivityPackage:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getActivityPackage();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getActivityName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getActivityName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_getActivityRequestCode:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getActivityRequestCode();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.mixare.lib.service.IBootStrap
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
@Override public int getZIndex() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getZIndex, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
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
@Override public java.lang.String getActivityPackage() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getActivityPackage, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String getActivityName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getActivityName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getActivityRequestCode() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getActivityRequestCode, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getZIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getPluginName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getActivityPackage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getActivityName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_getActivityRequestCode = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
public int getZIndex() throws android.os.RemoteException;
public java.lang.String getPluginName() throws android.os.RemoteException;
public java.lang.String getActivityPackage() throws android.os.RemoteException;
public java.lang.String getActivityName() throws android.os.RemoteException;
public int getActivityRequestCode() throws android.os.RemoteException;
}
