/*
 * Copyright (C) 2011- Armando Miraglia &lt;arma&#64;lamortenera.it&gt;
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
package org.mixare.utils;

import java.util.ArrayList;
import org.mixare.gui.DialogButton;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

/**
* Class intended to provide few utilities for printing errors, communicating
* the error to the user. This will also handle the logging of the errors.
*
* TODO
*   improve the code: remove the duplicated code.
*
* @author Armando Miraglia &lt;arma&#64;lamortenera.it&gt;
*/
public class ErrorUtility {
  /* Tag of the class for logging purpose */
  private static final String TAG = "ErrorUtility";

  /* Different error levels */
  public enum ErrorType {
    DEBUG, INFO, ERROR
  }

  /**
  * The constructor is set private because the class is not intended
  * to be instantiated.
  */
  private ErrorUtility() { /* private: not exposed for usage */ }

  public static void handleErrorDebug(String tag,
                                      Exception e) {

    performErrorHandling(ErrorType.DEBUG,
                         tag,
                         null,
                         e.getStackTrace().toString(),
                         null,
                         null);
  }

  public static void handleErrorDebug(String tag,
                                      String title,
                                      Exception e,
                                      Context ctx,
                                      ArrayList<DialogButton> btns) {

    performErrorHandling(ErrorType.DEBUG,
                         tag,
                         title,
                         e.getStackTrace().toString(),
                         ctx,
                         btns);
  }

  public static void handleErrorDebug(String tag,
                                      String title,
                                      String msg,
                                      Context ctx,
                                      ArrayList<DialogButton> btns) {

    performErrorHandling(ErrorType.DEBUG, tag, title, msg, ctx, btns);
  }

  /**
  * This method takes care to handle the exception so that it is
  * correctly logged and also shown to the user, if needed.
  */
  public static void handleError(String tag,
                                 Exception e) {

    performErrorHandling(ErrorType.ERROR,
                         tag,
                         null,
                         e.getStackTrace().toString(),
                         null,
                         null);
  }

  /**
  * This method takes care to handle the exception so that it is
  * correctly logged and also shown to the user, if needed.
  */
  public static void handleError(String tag,
                                 String title,
                                 Exception e,
                                 Context ctx,
                                 ArrayList<DialogButton> btns) {

    performErrorHandling(ErrorType.ERROR,
                         tag,
                         title,
                         e.getStackTrace().toString(),
                         ctx,
                         btns);
  }

  /**
  * This method takes care to handle the exception so that it is
  * correctly logged and also shown to the user, if needed.
  */
  public static void handleError(String tag,
                                 String title,
                                 String msg,
                                 Context ctx,
                                 ArrayList<DialogButton> btns) {

    performErrorHandling(ErrorType.ERROR, tag, title, msg, ctx, btns);
  }

  /**
  * Function actual performing the different type of error handling.
  */
  private static void performErrorHandling(ErrorType type,
                                           String tag,
                                           String title,
                                           String msg,
                                           Context ctx,
                                           ArrayList<DialogButton> btns) {

    switch(type) {
      case DEBUG:
        Log.d(tag, msg);
        break;
      case ERROR:
        Log.e(tag, msg);
        break;
      case INFO:
        Log.i(tag, msg);
        break;
    }

    if(ctx != null && title != null && btns != null )
      setErrorDialog(ctx, title, msg, btns);
    else
      Log.d(TAG, "Error dialog has been not shown.");
  }

  /**
  * This function is intended as alert builder for informing the device
  * user of the error detected.
  *
  * @author Armando Miraglia &lt;arma&#64;lamortenera.it&gt;
  * @param  ctx     context where the exception needs to be shown
  * @param  title   title of the dialog box
  * @param  e       exception that needs to be presented
  * @param  btns    list of buttons that have to be inserted in the box
  */
	private static void setErrorDialog(Context ctx,
                                     String title,
                                     Exception e,
                                     ArrayList<DialogButton> btns) {

    setErrorDialog(ctx, title, e.getStackTrace().toString(), btns);
  }


	private static void setErrorDialog(Context ctx,
                                     String title,
                                     String msg,
                                     ArrayList<DialogButton> btns) {
    DialogButton btn;

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(msg);
		builder.setCancelable(false);

    /* set provided buttons on the dialog */
    for(int i=0; i<btns.size(); ++i) {
      btn = btns.get(i);
      switch(btn.getType()) {
        case POSITIVE:
		      builder.setPositiveButton(btn.getTitle(), btn.getOCL());
          break;
        case NEUTRAL:
		      builder.setNeutralButton(btn.getTitle(), btn.getOCL());
          break;
        case NEGATIVE:
		      builder.setNegativeButton(btn.getTitle(), btn.getOCL());
          break;
      }
    }

		AlertDialog alert = builder.create();
		alert.show();
	}
}
