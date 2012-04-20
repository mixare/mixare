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
