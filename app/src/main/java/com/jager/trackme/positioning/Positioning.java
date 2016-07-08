package com.jager.trackme.positioning;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;

import com.jager.trackme.util.TimeSpan;

/**
 * Created by Jager on 2016.05.30..
 */
public class Positioning
{
       public Positioning(Context context, LocationListener serviceLocationListener)
       {
              this.context = context;
              this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
              this.serviceLocationListener = serviceLocationListener;
              locationingRuns = false;
       }

       private Context context;
       private LocationManager locationManager;
       private LocationListener serviceLocationListener;
       private boolean locationingRuns;
       private Location previousLocation;
       private PositioningLocationListener locationListener = new PositioningLocationListener();
       private TimeSpan waitingTimeBeforeLocating;
       private CountDownTimer timer;

       public void startPositioning()
       {
              if (!locationingRuns)
              {
                     startLocationingTimer();
                     locationingRuns = true;
              }
       }

       public void stopPositioning()
       {
              if (locationingRuns)
              {
                     stopLocationingTimer();
                     removeLocationUpdateRequest(); // TODO ez lehet hogy nem kell (sem a metódus)
                     locationingRuns = false;
              }
       }

       private class LocationTimer extends CountDownTimer
       {
              public LocationTimer(TimeSpan timeUntilNextPositioning)
              {
                     super(timeUntilNextPositioning.getTotalMillisec(), TICKINTERVAL);
              }

              private static final int TICKINTERVAL = 1000;
              public static final int DEFAULT_COUNTDOWN_SECONDS = 5;

              @Override
              public void onTick(long l)
              {

              }

              @Override
              public void onFinish()
              {
                     requestLocationUpdate();
                     setLocationingTimer();
              }
       }

       private class PositioningLocationListener implements LocationListener
       {
              @Override
              public void onLocationChanged(Location location)
              {
                     locationChanged(location);
                     serviceLocationListener.onLocationChanged(location);
              }

              @Override
              public void onStatusChanged(String s, int i, Bundle bundle)
              {
                     serviceLocationListener.onStatusChanged(s, i, bundle);
              }

              @Override
              public void onProviderEnabled(String s)
              {
                     serviceLocationListener.onProviderEnabled(s);
              }

              @Override
              public void onProviderDisabled(String s)
              {
                     serviceLocationListener.onProviderDisabled(s);
              }
       }

       private void startLocationingTimer()
       {
              waitingTimeBeforeLocating = new TimeSpan(1, 0);  // Initializing time, it could be even 0 second
              setLocationingTimer();
       }

       private void stopLocationingTimer()
       {
              if (timer != null) timer.cancel();
       }

       private void setLocationingTimer()
       {
              timer = null;
              timer = new LocationTimer(waitingTimeBeforeLocating);
              timer.start();
       }

       private void requestLocationUpdate()
       {
              if (isGpsEnabled())
              {
                     if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                     {
                            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
                     }
              }
       }

       public void removeLocationUpdateRequest()
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

       private void locationChanged(Location newLocation)
       {
              calculateWaitingTime(newLocation);
              previousLocation = newLocation;
       }

       private void calculateWaitingTime(Location lastMeasuredLocation)
       {
              if (previousLocation != null)
              {
                     double distanceBtwnPositions = lastMeasuredLocation.distanceTo(previousLocation);
                     waitingTimeBeforeLocating = new TimeSpan(20, 0); //TODO: itt valami fordított arányosság kell
              } else waitingTimeBeforeLocating = new TimeSpan(LocationTimer.DEFAULT_COUNTDOWN_SECONDS, 0);
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
