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
package org.mixare.lib.reality;

public class Filter {
	/* TAG info */
	private static final String TAG = "LowPassFilter";

	/* alpha coefficient */
	private float alpha = ALPHA_STEADY;
	/* alpha default value */
	private static final float ALPHA_STEADY = 0.01f;
	private static final float ALPHA_START_MOVING = 0.2f;
	private static final float ALPHA_MOVING = 0.5f;

	private float start_move_limit;
	private float move_limit;
	
	float tau = 0.075f;
	float a = 0.0f;

	/**
	 * Methode combining the signal of Gyroscope and the Accelerometer
	 * 
	 * @param acc
	 *            The data of the accelerometer
	 * @param gyro
	 *            The data of the gyroscope
	 * @param looptime
	 *            Duration of one loop?
	 * @param comp
	 *            The previous data of the filter
	 * @return
	 */
	public float[] complementaryFilter(float[] acc, float[] gyro, int looptime,
			float[] comp) {
		float dtC = (float) ((looptime) / 1000.0);
		a = tau / (tau + dtC);

		for (int i = 0; i < comp.length; i++) {
			comp[i] = a * (comp[i] + gyro[i] * dtC) + (1 - a) * (acc[i]);
		}

		return comp;
	}

	/**
	 * Method seting the values needed for cleaning the
	 * signal.
	 */
	public void setLimit(float start_move_limit, float move_limit) {
		/* set the two limits provided */
		this.start_move_limit = start_move_limit;
		this.move_limit = move_limit;
	}

	/**
	 * Method actually filtering the signal.
	 * 
	 * @param evt
	 *            input signal to be cleaned
	 * @param prev_evt
	 *            output signal cleaned
	 * @return float[] resulting cleaned signal
	 */
	public float[] lowPassFilter(float[] evt, float[] prev_evt) {
		/* trivial case where no output is defined */
		if (prev_evt == null)
			return evt;

		/* adjust the results, if the distance is changing rapidly */
		alpha = adjustMovement(prev_evt, evt);

		for (int i = 0; i < evt.length; i++)
			prev_evt[i] = prev_evt[i] + alpha * (evt[i] - prev_evt[i]);

		return prev_evt;
	}

	/**
	 * Function intended to check if the device is moving faster based on the
	 * delta provided.
	 */
	private float adjustMovement(float current[], float previous[]) {

		/* wrong arrays given? */
		if (previous.length != 3 || current.length != 3)
			return ALPHA_STEADY;

		float x1 = current[0], y1 = current[1], z1 = current[2];

		float x2 = previous[0], y2 = previous[1], z2 = previous[2];

		/* determine the distance of the two points */
		float d = (float) Math.sqrt(Math.pow((double) (x2 - x1), 2d)
				+ Math.pow((double) (y2 - y1), 2d)
				+ Math.pow((double) (z2 - z1), 2d));

		/*
		 * if the delta of the old and the new point is less than the distance,
		 * the ALPHA is left to be slow; differently the speed is increased
		 * proportionally to the acceleration
		 */
		if (d < this.start_move_limit) {
			return ALPHA_STEADY;

		} else if (d >= this.start_move_limit || d < this.move_limit) {
			return ALPHA_START_MOVING;

		} else {
			return ALPHA_MOVING;
		}
	}
}
