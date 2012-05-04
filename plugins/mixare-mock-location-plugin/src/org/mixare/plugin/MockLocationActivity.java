package org.mixare.plugin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class MockLocationActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		            //Yes button clicked
		        	Intent serviceIntent = new Intent();
					serviceIntent.setClassName("org.mixare.plugin", "org.mixare.plugin.MockLocationService");
					stopService(serviceIntent);
					startService(serviceIntent);
		        	finish();
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            finish();
		            break;
		        }
		    }
		};

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
		    .setNegativeButton("No", dialogClickListener).show();
		
		super.onCreate(savedInstanceState);
	}
	
}
