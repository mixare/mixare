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

import org.mixare.data.Json;
import org.mixare.render.Matrix;
import org.mixare.render.MixVector;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;

public class MixState {
	public static int NOT_STARTED = 0; 
	public static int PROCESSING = 1; 
	public static int READY = 2; 
	public static int DONE = 3; 

	int nextLStatus = MixState.NOT_STARTED;
	String downloadId;
	DownloadResult dRes;

	public Location curFix;
	private float curBearing, curPitch;
	public float screenWidth, screenHeight;

	public String radius = "20";

	public String startUrl = "";

	public Json jLayer = new Json();
	boolean detailsView = false;

	boolean handleEvent(MixContext ctx, String xmlId, String onPress) {

		if (onPress != null){
			if (onPress.startsWith("webpage")) {
				try {
					String webpage = MixUtils.parseAction(onPress);
					this.detailsView = true;
					ctx.loadWebPage(webpage);
				} catch (Exception ex) {
				}
			} else if (onPress.startsWith("return")){
				Intent intent = new Intent();
				intent.putExtra("RESULT", xmlId);
				ctx.mixView.setResult(Activity.RESULT_OK, intent);
				ctx.mixView.finish();
			}
		} 
		return true;
	}

	public float getCurBearing() {
		return curBearing;
	}
	public float getCurPitch() {
		return curPitch;
	}

	public void calcPitchBearing(Matrix rotationM) {

		MixVector looking = new MixVector();
		rotationM.transpose();
		looking.set(1, 0, 0);
		looking.prod(rotationM);
		this.curBearing = (int) (MixUtils.getAngle(0, 0, looking.x, looking.z)  + 360 ) % 360 ;

		rotationM.transpose();
		looking.set(0, 1, 0);
		looking.prod(rotationM);
		this.curPitch = -MixUtils.getAngle(0, 0, looking.y, looking.z);


	}


}
