 package org.mixare;
 
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MixTextViews extends Activity{
	
	private final static int INFO_TEXT_VIEW=1;
	private final static int LICENSE_TEXT_VIEW=2;
	private final static int ZOOM_TEXT_VIEW=3;

	public static int MENU_VIEW;
	
	/*strings for GPS info assigned in Data View*/
	//public static String GPS_LOCATION;
	public static double GPS_LONGITUDE =0;
	public static double GPS_LATITUDE =0;
	public static float GPS_ACURRACY =0;
	public static String GPS_LAST_FIX="";
	public static double GPS_ALTITUDE=0;
	public static float GPS_SPEED=0;
	public static String GPS_ALL="";
	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
		if(MENU_VIEW==3){
			LayoutInflater  layout = LayoutInflater.from(this);
			LinearLayout root = new LinearLayout(this);
 
			View myView = layout.inflate(0, root);
			
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
		    EditText et = new EditText(this);   
        alert.setTitle("From: ");
	        et = new EditText(this);

	        alert.setView(myView);
	        
//	        InputFilter[] filterArray = new InputFilter[1];
//	        filterArray[0] = new InputFilter.LengthFilter(5);
//	        et.setFilters(filterArray);
//	        
//	        DigitsKeyListener keylistener = new DigitsKeyListener(false, true);
//	        et.setKeyListener(keylistener);
//	        
//	        alert.show();
	         
	         
	       //  setContentView(et);
	    }
		else {
			TextView tv = new TextView(this);
			setTextView(tv, null);
        	setContentView(tv); 
        	tv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
           		finish();
                }
           });
		}
         
        
	}
	
	public void setTextView(TextView tv, EditText et){
	switch(MENU_VIEW){
		case 1:	
			tv.setText("GPS Info: \n\n" +
					"Longitude: " + GPS_LONGITUDE + " \n\n" +
					"Latitude: " + GPS_LATITUDE + " \n\n" +
					"Altitude: "+ GPS_ALTITUDE + " m \n\n" +
					"Speed: "+ GPS_SPEED + " km/h \n\n" +
					"Acurracy: " + GPS_ACURRACY + " m \n\n" +
					"Last GPS Fix: " + GPS_LAST_FIX + " \n\n");

		    tv.setTextSize(14);
	    tv.setPadding(10, 10, 10, 10);
		    tv.setScrollContainer(false);
		   // tv.setScrollBarStyle(1);
		    break;
		    
	
		case 2:
			 tv.setText(getString(R.string.license));
		     tv.setTextSize(13);
		     tv.setPadding(10, 10, 10, 10);
		     break;
	
		case 3:
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
	        alert.setTitle("Zoom Range From: ");
	        et = new EditText(this);
 
	        alert.setView(et);
	        alert.show();
	        break;

		}
 	}
	
 }
