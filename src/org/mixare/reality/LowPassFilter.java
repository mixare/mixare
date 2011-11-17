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
package org.mixare.reality;

/**
* This class is intended to provie a filter for normalizing sensors' data.
* E.g. accelerometer, magnetic field, etc... The idea has been taken from
* <a href="http://stackoverflow.com/questions/4611599/help-smoothing-data-from-a-sensor" >
*   this post
* </a>.
*
* @author Armando Miraglia
*/
public class LowPassFilter {
  /* TAG info */
  //private static final String TAG = "LowPassFilter";

  /* alpha default value */
  private static final float ALPHA_STEADY       = 0.01f;
  private static final float ALPHA_START_MOVING = 0.2f;
  private static final float ALPHA_MOVING       = 0.5f;
  
  /* alpha coefficient */
  private float alpha;
  private float start_move_limit;
  private float move_limit;

  /**
  * Constructor initializing the default values needed
  * for cleaning the signal.
  */
  public LowPassFilter(float start_move_limit, float move_limit) {
    /* set the two limits provided */
    this.start_move_limit = start_move_limit;
    this.move_limit = move_limit;
    /* initialize the state and alpha values */
    this.alpha = ALPHA_STEADY;
  }

  /**
  * Method actually filtering the signal.
  *
  * @param  evt       input signal to be cleaned
  * @param  prev_evt      output signal cleaned
  * @return float[]  resulting cleaned signal
  */
  public float[] filter(float[] evt, float[] prev_evt) {
    /* trivial case where no output is defined */
    if(prev_evt == null) return evt;

    /* adjust the results, if the distance is changing rapidly */
    alpha = adjustMovement(prev_evt, evt);

    for(int i=0; i<evt.length; i++)
      prev_evt[i] = prev_evt[i] + alpha * (evt[i] - prev_evt[i]);

    return prev_evt;
  }

  /**
  * Function intended to check if the device is moving faster based
  * on the delta provided.
  */
  private float adjustMovement(float current[], float previous[]) {

    /* wrong arrays given? */
    if(previous.length != 3 || current.length != 3) return ALPHA_STEADY;

    float x1 = current[0],
          y1 = current[1],
          z1 = current[2];

    float x2 = previous[0],
          y2 = previous[1],
          z2 = previous[2];

    /* determine the distance of the two points */
    float d = (float)Math.sqrt(Math.pow((double)(x2 - x1), 2d) +
                               Math.pow((double)(y2 - y1), 2d) +
                               Math.pow((double)(z2 - z1), 2d));

    /* if the delta of the old and the new point is less than the distance,
       the ALPHA is left to be slow; differently the speed is increased
       proportionally to the acceleration */
    if(d < start_move_limit) {
      return ALPHA_STEADY;

    } else if(d >= this.start_move_limit || d < this.move_limit) {
      return ALPHA_START_MOVING;

    } else {
      return ALPHA_MOVING;
    }
  }
}
