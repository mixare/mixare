/*
 * Copyright (C) 2010- Armando Miraglia &lt;arma&#64;lamortenera.it&gt;
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

import android.util.Log;

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
  private static final String TAG = "LowPassFilter";

  /* alpha default value */
  private static final float ALPHA_DEFAULT = 0.02f;
  /* alpha coefficient */
  private float alpha;
  private float distance = 1.8f;
  /* for each new object, this array is shared */
  private float previous[] = new float[3];


  /**
  * Constructor initializing the default values needed
  * for cleaning the signal.
  */
  public LowPassFilter() {
    this.alpha = ALPHA_DEFAULT;
    previous[0] = 0f;
    previous[1] = 0f;
    previous[2] = 0f;
  }

  /**
  * Method actually filtering the signal.
  *
  * @param  in       input signal to be cleaned
  * @param  out      output signal cleaned
  * @return float[]  resulting cleaned signal
  */
  public float[] filter(float[] in, float[] out) {
    /* trivial case where no output is defined */
    if(out == null) return in;

    /* now, the coefficient has to change based on the rate of acceleration
       that is checked; faster changes correspond to higher alpha value */
    if(previous[0] == 0f && previous[1] == 0f && previous[2] == 0f) {
      /* save the first "previous occurrence. */
      previous[0] = in[0];
      previous[1] = in[1];
      previous[2] = in[2];

      Log.d(TAG, "Storing the first sensor occurrence.");

    } else {
      /* adjust the results, if the distance is changing rapidly */
      adjustMovement(previous, in);

      for(int i=0; i<in.length; i++)
        out[i] = out[i] + alpha * (in[i] - out[i]);

      /* save the last current position as the previous one */
      previous[0] = in[0];
      previous[1] = in[1];
      previous[2] = in[2];

      Log.d(TAG, "Adjusting the sensor's value. Alpha value: " + alpha);
    }

    return out;
  }

  /**
  * Function intended to check if the device is moving faster based
  * on the delta provided.
  */
  private float adjustMovement(float previous[],
                               float current[]) {

    /* wrong arrays given? */
    if(previous.length != 3 || current.length != 3) return ALPHA_DEFAULT;

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
    if(d > this.distance) return d / 10;
    else                  return d * 2;
  }
}
