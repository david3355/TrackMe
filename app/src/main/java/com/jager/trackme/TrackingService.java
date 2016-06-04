package com.jager.trackme;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;

import com.jager.trackme.intercomponentcommunicator.InterComponentCommunicator;
import com.jager.trackme.intercomponentcommunicator.InterComponentData;

public class TrackingService extends Service implements LocationListener
{
       public final static String SERVICENAME = "com.jager.trackme.TrackingService";
       public final static String KEY_RESULTRECEIVER = "loc_svc_resultreceiver";
       public final static int LOCATION_SEND = 110;
       public final static int MESSAGE_RESULTCODE = 101;
       private boolean locationingRuns = false;
       private ResultReceiver replier;

       protected LocationManager locationManager;

       @Override
       public int onStartCommand(Intent intent, int flags, int startId)
       {
              locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
              replier = intent.getParcelableExtra(KEY_RESULTRECEIVER);
              startLocationing();
              return super.onStartCommand(intent, flags, startId);
       }

       @Override
       public void onDestroy()
       {
              stopLocationing();
              super.onDestroy();
       }

       @Override
       public IBinder onBind(Intent intent)
       {
              return null;
       }


       private void startLocationing()
       {
              if (!locationingRuns)
              {
                     //locationManager.requestSingleUpdate();
                     final long MIN_TIME_BTW_UPDATES = 1000 * 30;
                     final long MIN_DIST = 10;
                     if (isGpsEnabled())
                     {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                            {
                                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BTW_UPDATES, MIN_DIST, this);
                                   locationingRuns = true;
                            }
                     }
              }
       }

       private void stopLocationing()
       {
              if (locationingRuns)
              {
                     if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                     {
                            locationManager.removeUpdates(this);
                            locationingRuns = false;
                     }
              }
       }

       private boolean isGpsEnabled()
       {
              return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
       }

       private boolean isNetworkEnabled()
       {
              return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
       }

       private void sendDataBackToActivity(Location data)
       {
              try
              {
                     InterComponentData icd = new InterComponentData();
                     icd.latitude = data.getLatitude();
                     icd.longitude = data.getLongitude();
                     icd.accuracy = data.getAccuracy();
                     replier.send(LOCATION_SEND, InterComponentCommunicator.getBundle(icd));
              }
              catch(Exception e)
              {
                     //When activity is not active
              }
       }

       private void sendMessageToActivity(String message)
       {
              try
              {
                     Bundle data = new Bundle();
                     data.putString(InterComponentData.KEY_MSG, message);
                     replier.send(MESSAGE_RESULTCODE, data);
              }
              catch(Exception e)
              {
                     //When activity is not active
              }
       }



       @Override
       public void onLocationChanged(Location location)
       {
              sendDataBackToActivity(location);
       }

       @Override
       public void onStatusChanged(String s, int i, Bundle bundle)
       {

       }

       @Override
       public void onProviderEnabled(String s)
       {

       }

       @Override
       public void onProviderDisabled(String s)
       {

       }
}
