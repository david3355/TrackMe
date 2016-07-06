package com.jager.trackme;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.jager.trackme.intercomponentcommunicator.InterComponentCommunicator;
import com.jager.trackme.intercomponentcommunicator.InterComponentData;

public class TrackingService extends Service implements LocationListener
{
       public final static String SERVICENAME = TrackingService.class.getName();
       public static final int MSG_REPLIER = 10;
       public static final int MSG_UNBIND = 11;
       public final static int MSG_LOCATION_SEND = 100;
       public final static int MSG_INFOMESSAGE = 110;

       private boolean locationingRuns = false;
       private Thread worker;
       private final Messenger messenger = new Messenger(new IncomingHandler());

       private Messenger replier = null;


       protected LocationManager locationManager;

       @Override
       public int onStartCommand(Intent intent, int flags, int startId)
       {
              locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
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
              return messenger.getBinder();
       }

       class IncomingHandler extends Handler
       {
              @Override
              public void handleMessage(Message msg)
              {
                     switch (msg.what)
                     {
                            case MSG_REPLIER:
                                   replier = msg.replyTo;
                                   Log.d("TestService", "Replier object gained from message");
                                   break;
                            case MSG_UNBIND:
                                   replier = null;
                                   Log.d("TestService", "An activity unbinded");
                                   break;
                            default:
                                   super.handleMessage(msg);
                     }
              }
       }

       private class Worker implements Runnable
       {
              @Override
              public void run()
              {
                     locationHandling();
              }
       }

       private void locationHandling()
       {
              if (!locationingRuns)
              {
                     //locationManager.requestSingleUpdate();
                     final long MIN_TIME_BTW_UPDATES = 1000 * 30;
                     final long MIN_DIST = 10;
                     if (isGpsEnabled())
                     {
                            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                            {
                                   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BTW_UPDATES, MIN_DIST, this);
                                   locationingRuns = true;
                            }
                     }
              }
       }

       private void startLocationingOnThread()
       {
              worker = new Thread(new Worker());
              worker.start();
       }

       private void startLocationing()
       {
              locationHandling();
       }

       private void stopLocationing()
       {
              if (locationingRuns)
              {
                     if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                     {
                            locationManager.removeUpdates(this);
                            locationingRuns = false;
                     }
              }
              if (worker != null && worker.isAlive()) worker.interrupt();
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
                     Message msg = createMessage(MSG_LOCATION_SEND, InterComponentCommunicator.getBundle(icd));
                     replier.send(msg);
              } catch (Exception e)
              {
                     //When activity is not active
              }
       }

       private Message createMessage(int messageKey, Bundle data)
       {
              Message msg = new Message();
              msg.setData(data);
              msg.what = messageKey;
              return msg;
       }

       private void sendMessageToActivity(String message)
       {
              try
              {
                     Bundle data = new Bundle();
                     data.putString(InterComponentData.KEY_MSG, message);
                     Message msg = createMessage(MSG_INFOMESSAGE, data);
                     replier.send(msg);
              } catch (Exception e)
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
