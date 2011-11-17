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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Ensures compatibility with older and newer versions of the API. 
 * See the SDK docs for comments
 * 
 * @author daniele
 *
 */
public class Compatibility {

	public static final float DEFAULT_VIEW_ANGLE = (float) Math.toRadians(45);

	private static Method mParameters_getSupportedPreviewSizes;
	private static Method mParameters_getHorizontalViewAngle;
	private static Method mParameters_getVerticalViewAngle;
	private static Method mDefaultDisplay_getRotation;

	static {  
		initCompatibility();
	};

	/** this will fail on older phones (Android version < 2.0) */
	private static void initCompatibility() {
		try {
			mParameters_getSupportedPreviewSizes = Camera.Parameters.class.getMethod(
					"getSupportedPreviewSizes", new Class[] { } );
			mParameters_getHorizontalViewAngle = Camera.Parameters.class.getMethod(
					"getHorizontalViewAngle", new Class[] { } );
			mParameters_getVerticalViewAngle = Camera.Parameters.class.getMethod(
					"getVerticalViewAngle", new Class[] { } );
			mDefaultDisplay_getRotation = Display.class.getMethod("getRotation", new Class[] { } );

			/* success, this is a newer device */
		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
		}
	}

	/** If it's running on a new phone, let's get the supported preview sizes, before it was fixed to 480 x 320*/
	@SuppressWarnings("unchecked")
	public static List<Camera.Size> getSupportedPreviewSizes(Camera.Parameters params) {
		List<Camera.Size> retList = null;

		try {
			Object retObj = mParameters_getSupportedPreviewSizes.invoke(params);
			if (retObj != null) {
				retList = (List<Camera.Size>)retObj;
			}
		}
		catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			//System.err.println("unexpected " + ie);
		}
		return retList;
	}

	public static float getHorizontalViewAngle() {
		float hva = DEFAULT_VIEW_ANGLE;

		try {
			Camera camera = Camera.open();
			Object retObj = mParameters_getHorizontalViewAngle.invoke(camera);
			if (retObj != null) {
				hva = (Float) retObj;
			}

		} catch (Exception ex) {
			
			Log.d("Mixare", "using default hva");
			//ex.printStackTrace();
		}
		return hva;
	}

	public static float getVerticalViewAngle() {
		float vva = DEFAULT_VIEW_ANGLE;

		try {
			Camera camera = Camera.open();
			Object retObj = mParameters_getVerticalViewAngle.invoke(camera);
			if (retObj != null) {
				vva = (Float) retObj;
			}

		} catch (Exception ex) {
			//ex.printStackTrace();
		}
		return vva;
	}
	static public int getRotation(final Activity activity) {
		int result = 1;
		try {
			Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
			Object retObj = mDefaultDisplay_getRotation.invoke(display);
			if( retObj != null) {
				result = (Integer) retObj;
			}
		} catch (Exception ex) {
			//ex.printStackTrace();
		}
		return result;
	}

}