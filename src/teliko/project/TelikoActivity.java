package teliko.project;


import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TelikoActivity extends Activity {
	Button Scan;
	EditText spotx, spoty, Azimuth;
	TextView Result, userx, usery, Exhibit;
	double bearing;
	MediaPlayer mp;
	Timer timer;
	TimerTask tone;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	LocationListener mlocListener = new MyLocationListener();
    	mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
		spotx = (EditText) findViewById(R.id.SpotPositionX);
		spoty = (EditText) findViewById(R.id.SpotPositionY);
		userx = (TextView) findViewById(R.id.UserPositionX);
		usery = (TextView) findViewById(R.id.UserPositionY);
		Azimuth = (EditText) findViewById(R.id.Azimuth);
		Scan = (Button) findViewById(R.id.ScanButton);
		Result = (TextView) findViewById(R.id.Result);
		Exhibit = (TextView) findViewById(R.id.ExhibitInfo);
		

		timer = new Timer("Proximity", true);
		tone = new TimerTask(){
		     @Override
		     public void run(){
		    	 ProximitySound();
		     }
		};
		timer.scheduleAtFixedRate(tone, 120000, 120000);
		
		Scan.setOnClickListener(new OnClickListener() {
            
            public void onClick(View v) {
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "ONE_D_MODE");
                startActivityForResult(intent, 0);
            }

        });
    }
    
    private double findDistance(double spotlat, double spotlng, double userlat, double userlng) {
    	
        double R = 6371; 
        double dLat =  Math.toRadians(userlat-spotlat);

        double dLon =  Math.toRadians(userlng-spotlng); //Haversine formula
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(spotlat)) * Math.cos(Math.toRadians(userlat)) * 
                Math.sin(dLon/2) * Math.sin(dLon/2); 
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
        double d = R * c;
        if (d>2000 && d<=3000) PlaySound(2000,2000);
        if (d>1000 && d<=2000) PlaySound(1500,1500);
        if (d>300 && d<=1000) PlaySound(1000,1000);
        if (d>10 && d<=300) PlaySound(250,250);
        if (d>0 && d<=10) PlaySound(50,50);
        return d;
    }
    
    public static double calculateBearing(double lat1, double long1, double lat2, double long2)  
    {  
        //Convert input values to radians  
        lat1 = Math.toRadians(lat1);  
        long1 = Math.toRadians(long1);  
        lat2 = Math.toRadians(lat2);  
        long2 = Math.toRadians(long2);  

        double deltaLong = long2 - long1;  //Delta Longitude

        double y = Math.sin(deltaLong) * Math.cos(lat2);  
        double x = Math.cos(lat1) * Math.sin(lat2) -  
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLong);  
        double bearing = Math.atan2(y, x);  
        return (Math.toDegrees(bearing)+360)%360;  
    }  
    
    private String degreesToBearing(double azimuth){
    	String bearingtxt="";
	    if (azimuth>=0 && azimuth<91) bearingtxt = "N "+String.format("%.2f",azimuth)+" E ";
	    else if (azimuth>89 && azimuth<181) bearingtxt = "S "+String.format("%.2f",(180-azimuth))+" E ";
	    else if (azimuth>179 && azimuth<271) bearingtxt = "S "+String.format("%.2f",(azimuth-180))+" W ";
	    else bearingtxt = "N "+String.format("%.2f",(360-azimuth))+" W ";
    	return bearingtxt;
    }
    
    private String instructCourse(double Bearing, double Azimuth){
    	double deviation = Bearing - Azimuth;
    	if ((deviation>329 && deviation<=360) || (deviation>=0 && deviation<31)) return "Keep going foward!";
    	else if (deviation>30 && deviation<=81) return "Turn left!";
    	else if (deviation>81 && deviation<=171) return "Go back!";
    	else if (deviation>171 && deviation<=329) return "Turn right!";
    	else return "No good reading! You're lost!";
    }
    
    public class MyLocationListener implements LocationListener{
    	  public void onLocationChanged(Location loc) {
    		  if (spotx.getText().length() != 0 && spoty.getText().length() != 0 && Azimuth.getText().length() != 0){
    		  
    		userx.setText(" Lat. = " + String.format("%.2f",loc.getLatitude()) + " ");
    	    usery.setText(" Long. = " + String.format("%.2f",loc.getLongitude())); 
        	double Azim = Double.parseDouble(Azimuth.getText().toString());   	    
    	    bearing = calculateBearing((Double.parseDouble(spotx.getText().toString())),
					  Double.parseDouble(spoty.getText().toString()),
					  loc.getLatitude(),
					  loc.getLongitude());
    	    Result.setText(String.format("%.2f", findDistance(
					Double.parseDouble(spotx.getText().toString()),
					Double.parseDouble(spoty.getText().toString()),
					loc.getLatitude(),
					loc.getLongitude()
					))+" Km. Your bearing is "+degreesToBearing(Azim)+
					". Spot's bearing is at "+degreesToBearing(bearing)+
					". Your deviation is by "+String.format("%.2f",Math.abs(bearing - Azim))+
					"°. "+instructCourse(bearing, Azim));    	    
    	  }
    	  }

    	  public void onProviderDisabled(String provider) {
    	    //Toast.makeText( getApplicationContext(), ((String) "GPS Disabled"), Toast.LENGTH_SHORT ).show();
    	  }

    	  public void onProviderEnabled(String provider) {
    	    //Toast.makeText( getApplicationContext(), ((String) "GPS Enabled"), Toast.LENGTH_SHORT).show();
    	  }

    	  public void onStatusChanged(String provider, int status, Bundle extras) {
    	  }
    	 }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	Toast toast;
    	String BarContents, x, y, exhibit;
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                BarContents = intent.getStringExtra("SCAN_RESULT");
                x = BarContents.substring(0, BarContents.indexOf(","));
                y = BarContents.substring(BarContents.indexOf(",")+1,BarContents.indexOf("|"));
                exhibit = BarContents.substring(BarContents.indexOf("|")+1);
                spotx.setText(x, TextView.BufferType.EDITABLE);
                spoty.setText(y, TextView.BufferType.EDITABLE);
                Exhibit.setText(exhibit);
                toast = Toast.makeText(this, ((String) "Next Exhibit's coords are loaded"), Toast.LENGTH_SHORT);
                toast.show();
            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                toast = Toast.makeText(this, "Scan was Cancelled!", Toast.LENGTH_LONG);               
                toast.show();
                
            }
        }
    }
    public void PlaySound(long delay, long period){
    	tone.cancel();
    	tone = new TimerTask(){
		     @Override
		     public void run(){
		    	 ProximitySound();
		     }
		};
    	timer.scheduleAtFixedRate(tone, delay, period);
    }
    public void ProximitySound(){
    	
    mp = MediaPlayer.create(TelikoActivity.this, R.raw.proximity);   
    mp.start();
    mp.setOnCompletionListener(new OnCompletionListener() {

        public void onCompletion(MediaPlayer mp) {
            mp.release();
        }

    });
    	
    }

	public void onDestroy(){
		super.onDestroy();
		System.runFinalizersOnExit(true);
		tone.cancel();
		timer.cancel();
	}
}