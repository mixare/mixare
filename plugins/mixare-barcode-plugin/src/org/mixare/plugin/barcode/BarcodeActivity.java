/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
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
package org.mixare.plugin.barcode;

import org.mixare.plugin.barcode.intent.IntentIntegrator;
import org.mixare.plugin.barcode.intent.IntentResult;
import org.mixare.plugin.barcode.service.BarcodeService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class BarcodeActivity extends Activity{
	
	public final String resultType = "Datasource";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		scan();
	}
	
	private void scan(){
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, data);
		if (scanResult != null) {
			String[] url = new String[1];
			url[0] = scanResult.getContents();
			
			if((url[0] == null || url[0].equals("null"))  && data != null ){ //if no url is found: scan again
				Toast.makeText(this, "No url found, scan again!", Toast.LENGTH_LONG).show();
				scan();
				return;
			}else{ // url found, return it as result.
				Intent intent = new Intent();
				intent.putExtra("resultType", resultType);
				intent.putExtra("url", url);
				setResult(BarcodeService.ACTIVITY_REQUEST_CODE, intent);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}
}
