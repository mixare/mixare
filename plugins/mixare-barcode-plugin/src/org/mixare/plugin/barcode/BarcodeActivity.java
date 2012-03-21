package org.mixare.plugin.barcode;

import org.mixare.plugin.barcode.intent.IntentIntegrator;
import org.mixare.plugin.barcode.intent.IntentResult;
import org.mixare.plugin.barcode.service.BarcodeService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class BarcodeActivity extends Activity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, data);
		if (scanResult != null) {
			String url = scanResult.getContents();
			
			Intent intent = new Intent();
			intent.putExtra("url", url);
			setResult(BarcodeService.ACTIVITY_REQUEST_CODE, intent);
		}
		super.onActivityResult(requestCode, resultCode, data);
		finish();
	}
}
