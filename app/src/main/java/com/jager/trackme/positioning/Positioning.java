package com.jager.trackme.positioning;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Jager on 2016.05.30..
 */
public class Positioning
{
       public Positioning(Context context, LocationListener locationListener)
       {
              this.context = context;
              this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
              this.locationListener = locationListener;
              locationingRuns = false;
       }

       private Context context;
       private LocationManager locationManager;
       private LocationListener locationListener;
       private boolean locationingRuns;

       public void startPositioning()
       {
              if (!locationingRuns)
              {
                     //locationManager.requestSingleUpdate();
                     final long MIN_TIME_BTW_UPDATES = 1000 * 30;
                     final long MIN_DIST = 10;
                     if (isGpsEnabled())
                     {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                            {
                                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BTW_UPDATES, MIN_DIST, locationListener);
                                   locationingRuns = true;
                            }
                     }
              }
       }

       public void stopPositioning()
       {
              if (locationingRuns)
              {
                     if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                     {
                            locationManager.removeUpdates(locationListener);
                            locationingRuns = false;
                     }
              }
       }

       public boolean isGpsEnabled()
       {
              return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
       }

       public boolean isNetworkEnabled()
       {
              return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
       }

}
