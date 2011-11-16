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
package org.mixare;

import org.mixare.utils.ErrorUtility;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.view.View;

class AugmentedView extends View {
  /* Logger tag */
  private static final String TAG = "AugmentedView";

	MixView app;
	int xSearch=200;
	int ySearch = 10;
	int searchObjWidth = 0;
	int searchObjHeight=0;

	public AugmentedView(Context context) {
		super(context);

		try {
			app = (MixView) context;

			//app.killOnError();

		} catch (Exception e) {
			//ErrorUtility.handleError(TAG, e, true);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			//			if (app.fError) {
			//
			//				Paint errPaint = new Paint();
			//				errPaint.setColor(Color.RED);
			//				errPaint.setTextSize(16);
			//				
			//				/*Draws the Error code*/
			//				canvas.drawText("ERROR: ", 10, 20, errPaint);
			//				canvas.drawText("" + app.fErrorTxt, 10, 40, errPaint);
			//
			//				return;
			//			}

			//app.killOnError();

			MixView.dWindow.setWidth(canvas.getWidth());
			MixView.dWindow.setHeight(canvas.getHeight());

			MixView.dWindow.setCanvas(canvas);

			if (!MixView.dataView.isInited()) {
				MixView.dataView.init(MixView.dWindow.getWidth(), MixView.dWindow.getHeight());
			}
			if (app.isZoombarVisible()){
				Paint zoomPaint = new Paint();
				zoomPaint.setColor(Color.WHITE);
				zoomPaint.setTextSize(14);
				String startKM, endKM;
				endKM = "80km";
				startKM = "0km";
				/*if(MixListView.getDataSource().equals("Twitter")){
					startKM = "1km";
				}*/
				canvas.drawText(startKM, canvas.getWidth()/100*4, canvas.getHeight()/100*85, zoomPaint);
				canvas.drawText(endKM, canvas.getWidth()/100*99+25, canvas.getHeight()/100*85, zoomPaint);

				int height= canvas.getHeight()/100*85;
				int zoomProgress = app.getZoomProgress();
				if (zoomProgress > 92 || zoomProgress < 6) {
					height = canvas.getHeight()/100*80;
				}
				canvas.drawText(app.getZoomLevel(),  (canvas.getWidth())/100*zoomProgress+20, height, zoomPaint);
			}

			MixView.dataView.draw(MixView.dWindow);
		} catch (Exception e) {
			//ErrorUtility.handleError(TAG, e, true);
		}
	}
}

