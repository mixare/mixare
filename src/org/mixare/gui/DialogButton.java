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
package org.mixare.gui;

import android.content.DialogInterface;
/**
* This class provides a mean for creating dynamically new dialog
* boxes. Is specifically meant for error handling utilities but can
* be used for creating other boxes.
*
* @author Armando Miraglia &lt;arma&#64;lamortenera.it&gt;
*/
public class DialogButton {
  /* type of dialog button */
  private ButtonTypes type;
  /* title of the dialog button */
  private String title;
  /* action associated to the button */
  private DialogInterface.OnClickListener ocl;

  /**
  * Construct a new DialogButton based on the information
  * provided. Passing null objects, the behaviour is unknown.
  *
  * @author Armando Miraglia &lt;arma&#64;lamortenera.it&gt;
  * @param type   type of the button
  *               @see org.mixare.gui.ButtonTypes
  * @param title  title of the button
  * @param ocl    on click listener associated to the button
  */
  public DialogButton(ButtonTypes type,
                      String title,
                      DialogInterface.OnClickListener ocl) {

    this.type = type;
    this.title = title;
    this.ocl = ocl;
  }

  /**
   * Gets the type for this instance.
   *
   * @return The type.
   */
  public ButtonTypes getType() {
    return this.type;
  }

  /**
   * Gets the title for this instance.
   *
   * @return The title.
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Gets the onClickListner associated to this instance.
   *
   * @return The ocl.
   */
  public DialogInterface.OnClickListener getOCL() {
    return this.ocl;
  }
}
