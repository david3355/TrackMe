package com.jager.trackme;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class TrackingService extends Service
{
       public final static String SERVICENAME = "com.jager.trackme.TrackingService";

       public TrackingService()
       {
       }

       @Override
       public IBinder onBind(Intent intent)
       {
              // TODO: Return the communication channel to the service.
              throw new UnsupportedOperationException("Not yet implemented");
       }
}
