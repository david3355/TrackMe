package com.jager.trackme;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.jager.trackme.database.LocationsManager;
import com.jager.trackme.database.PositionsTableDef;
import com.jager.trackme.intercomponentcommunicator.InterComponentCommunicator;
import com.jager.trackme.intercomponentcommunicator.InterComponentData;
import com.jager.trackme.positioning.Positioning;

import org.joda.time.DateTime;

public class TrackingService extends Service implements LocationListener
{
       public final static String SERVICENAME = TrackingService.class.getName();
       public static final int MSG_REPLIER = 10;
       public static final int MSG_UNBIND = 11;
       public final static int MSG_LOCATION_SEND = 100;
       public final static int MSG_INFOMESSAGE = 110;

       private LocationsManager locDatabase;
       private final Messenger messenger = new Messenger(new IncomingHandler());
       private Messenger replier = null;
       private Positioning positioning;

       @Override
       public int onStartCommand(Intent intent, int flags, int startId)
       {
              locDatabase = LocationsManager.getInstance();
              positioning = new Positioning(this, this);
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

       private void startLocationing()
       {
              positioning.startPositioning();
       }

       private void stopLocationing()
       {
              positioning.stopPositioning();
       }

       private boolean isGpsEnabled()
       {
              return positioning.isGpsEnabled();
       }

       private boolean isNetworkEnabled()
       {
              return positioning.isNetworkEnabled();
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

       private void saveLocation(Location location)
       {
              PositionsTableDef model = new PositionsTableDef(location.getLatitude(), location.getLongitude(), (int) location.getAccuracy(), DateTime.now().getMillis());
              locDatabase.saveNewPosition(model);
       }


       @Override
       public void onLocationChanged(Location location)
       {
              saveLocation(location);
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
