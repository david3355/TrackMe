package com.jager.trackme;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jager.trackme.database.LocationsManager;
import com.jager.trackme.database.PositionsTableDef;
import com.jager.trackme.intercomponentcommunicator.InterComponentCommunicator;
import com.jager.trackme.intercomponentcommunicator.InterComponentData;
import com.jager.trackme.util.ActivityUtil;

import org.joda.time.DateTime;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, ServiceConnection
{
       private GoogleMap map;
       private ImageView img_trackingstatus;
       private TextView txt_trackingstatus, txt_position, txt_latitude, txt_longitude, txt_accuracy, txt_lasttime;
       private Button btn_start_tracking, btn_stop_tracking;
       private ImageView img_map, img_settings;
       private CircleOptions circleOptions;
       private MarkerOptions marker;

       private Messenger messenger = null;
       private boolean boundToService;
       private final Messenger responseMessenger = new Messenger(new IncomingHandler());
       private LocationsManager locDatabase;
       private final static String DATETIME_FORMAT = "YYYY-MM-dd HH:mm:ss";


       @Override
       protected void onCreate(Bundle savedInstanceState)
       {
              super.onCreate(savedInstanceState);
              setContentView(R.layout.activity_main);

              img_trackingstatus = (ImageView) findViewById(R.id.img_trackingstatus);
              txt_trackingstatus = (TextView) findViewById(R.id.txt_trackingstatus);
              txt_position = (TextView) findViewById(R.id.txt_position);
              txt_latitude = (TextView) findViewById(R.id.txt_latitude);
              txt_longitude = (TextView) findViewById(R.id.txt_longitude);
              txt_accuracy = (TextView) findViewById(R.id.txt_accuracy);
              txt_lasttime = (TextView) findViewById(R.id.txt_lasttime);

              btn_start_tracking = (Button) findViewById(R.id.btn_trackingstart);
              btn_stop_tracking = (Button) findViewById(R.id.btn_trackingstop);
              img_map = (ImageView) findViewById(R.id.img_map);
              img_settings = (ImageView) findViewById(R.id.img_settings);

              btn_start_tracking.setOnClickListener(this);
              btn_stop_tracking.setOnClickListener(this);
              img_map.setOnClickListener(this);
              img_settings.setOnClickListener(this);
              txt_position.setOnClickListener(this);
              txt_latitude.setOnClickListener(this);
              txt_longitude.setOnClickListener(this);
              boundToService = false;

              locDatabase = LocationsManager.getInstance(this);
              initCircle();       //TODO: a circle-t ki kell venni, itt csak marker kell, majd a térképhez kell circle, talán

              // Obtain the SupportMapFragment and get notified when the map is ready to be used.
              SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                      .findFragmentById(R.id.partial_map);
              mapFragment.getMapAsync(this);
       }

       @Override
       public void onServiceConnected(ComponentName className, IBinder service)
       {
              boundToService = true;
              setCommunicationBetweenComponents(service);
       }

       @Override
       public void onServiceDisconnected(ComponentName className)
       {
              messenger = null;
              boundToService = false;
       }

       private void setCommunicationBetweenComponents(IBinder service)
       {
              messenger = new Messenger(service);
              Message message = new Message();
              message.what = TrackingService.MSG_REPLIER;
              message.replyTo = responseMessenger;
              trySendMessage(message);
       }

       class IncomingHandler extends Handler
       {
              // Process messages coming from the service
              @Override
              public void handleMessage(final Message msg)
              {
                     processMessageFromService(msg);
              }
       }

       private void processMessageFromService(Message msg)
       {
              Bundle data = msg.getData();
              switch (msg.what)
              {
                     case TrackingService.MSG_LOCATION_SEND:
                            locationChanged(InterComponentCommunicator.readBundle(data));
                            break;
                     case TrackingService.MSG_INFOMESSAGE:
                            processMessage(data.getString(InterComponentData.KEY_MSG));
                            break;
              }
       }

       private void loadLastLocation()
       {
              PositionsTableDef lastpos = locDatabase.getLatestPosition();
              String s_latitude = "";
              String s_longitude = "";
              String s_accuracy = "";
              String s_time = "";
              if(lastpos != null)
              {
                     s_latitude = String.valueOf(lastpos.getLatitude());
                     s_longitude = String.valueOf(lastpos.getLongitude());
                     s_accuracy = String.valueOf(lastpos.getAccuracy());
                     s_time = new DateTime(lastpos.getTimestamp()).toString(DATETIME_FORMAT);
                     LatLng newpos= new LatLng(lastpos.getLatitude(), lastpos.getLongitude());
                     marker = new MarkerOptions().position(newpos).title(String.format("[%s;%s]", newpos.latitude, newpos.longitude));
                     map.addMarker(marker);
                     map.moveCamera(CameraUpdateFactory.newLatLngZoom(newpos, 15));
              }
              txt_latitude.setText(s_latitude);
              txt_longitude.setText(s_longitude);
              txt_accuracy.setText(s_accuracy);
              txt_lasttime.setText(s_time);
       }

       private void initCircle() //TODO ezt törölni kell majd
       {
              circleOptions = new CircleOptions();
              circleOptions.fillColor(Color.rgb(255, 0, 0));
              circleOptions.strokeColor(Color.rgb(0, 255, 0));
              circleOptions.strokeWidth(2);
       }

       @Override
       public void onMapReady(GoogleMap googleMap)
       {
              map = googleMap;
              map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
              {
                     @Override
                     public void onMapLongClick(LatLng latLng)
                     {
                            startActivity(MapActivity.class);
                     }
              });
              loadLastLocation();
       }

       @Override
       protected void onResume()
       {
              initStatusBar();
              bindService(); // Bind must done after initializing the status bar, because bindig the service starts the service, but not with the positioning
              super.onResume();
       }

       @Override
       protected void onPause()
       {
              super.onPause();
              unbindService();
       }

       private void initStatusBar()
       {
              if (ActivityUtil.isServiceRunning(TrackingService.SERVICENAME, this))
              {
                     setTrackingStartedState();
              } else
              {
                     setTrackingStoppedState();
              }
       }

       @Override
       public void onClick(View view)
       {
              switch (view.getId())
              {
                     case R.id.btn_trackingstart:
                            startTracking();
                            break;
                     case R.id.btn_trackingstop:
                            stopTracking();
                            break;
                     case R.id.img_map:
                            startActivity(MapActivity.class);
                            break;
                     case R.id.img_settings:
                            startActivity(SettingsActivity.class);
                            break;
                     case R.id.txt_position:
                            copyText(String.format("%s;%s", txt_latitude.getText().toString(), txt_longitude.getText().toString()), "Position");
                            break;
                     case R.id.txt_latitude:
                            copyText(txt_latitude.getText().toString(), "Latitude");
                            break;
                     case R.id.txt_longitude:
                            copyText(txt_longitude.getText().toString(), "Longitude");
                            break;
              }
       }

       private void bindService()
       {
              Intent svc = new Intent(this, TrackingService.class);
              bindService(svc, this, Context.BIND_AUTO_CREATE);
       }

       private void unbindService()
       {
              if (boundToService)
              {
                     sendUnbindMessage();
                     unbindService(this);
                     boundToService = false;
              }
       }

       private void sendUnbindMessage()
       {
              Message msg = Message.obtain(null, TrackingService.MSG_UNBIND, 0, 0);
              trySendMessage(msg);
       }

       private void startTracking()
       {
              startPositioningService();
              setTrackingStartedState();
              ActivityUtil.popup("Tracking started!", this);
       }

       private void stopTracking()
       {
              stopPositioningService();
              setTrackingStoppedState();
              ActivityUtil.popup("Tracking stopped!", this);
       }

       private void startPositioningService()
       {
              if (!boundToService) bindService();
              Intent svc = new Intent(this, TrackingService.class);
              startService(svc);
       }

       private void stopPositioningService()
       {
              unbindService();
              Intent svc = new Intent(this, TrackingService.class);
              stopService(svc);
       }

       private void setTrackingStartedState()
       {
              btn_start_tracking.setVisibility(View.GONE);
              btn_stop_tracking.setVisibility(View.VISIBLE);
              img_trackingstatus.setImageDrawable(getResources().getDrawable(R.drawable.satellite_on));
              txt_trackingstatus.setText(ActivityUtil.getStringRes(this, R.string.tracking_on));
       }

       private void setTrackingStoppedState()
       {
              btn_start_tracking.setVisibility(View.VISIBLE);
              btn_stop_tracking.setVisibility(View.GONE);
              img_trackingstatus.setImageDrawable(getResources().getDrawable(R.drawable.satellite_off));
              txt_trackingstatus.setText(ActivityUtil.getStringRes(this, R.string.tracking_off));
       }

       private void copyText(String data, String component)
       {
              ActivityUtil.copyTextToClipboard(data, this);
              ActivityUtil.popup(String.format("%s [%s] copied to clipboard!", component, data), this);
       }

       private void startActivity(Class classToStart)
       {
              Intent activity = new Intent(this, classToStart);
              startActivity(activity);
       }

       private void trySendMessage(Message message)
       {
              try
              {
                     if (message != null) messenger.send(message);
              } catch (RemoteException e)
              {
                     e.printStackTrace();
              }
       }

       public void locationChanged(InterComponentData data)
       {
              final double latitude = data.latitude;
              final double longitude = data.longitude;
              final double accuracy = data.accuracy;
              final LatLng newpos = new LatLng(latitude, longitude);

              this.runOnUiThread(new Runnable()
              {
                     @Override
                     public void run()
                     {
                            txt_latitude.setText(String.valueOf(latitude));
                            txt_longitude.setText(String.valueOf(longitude));
                            txt_accuracy.setText(String.valueOf(accuracy));
                            txt_lasttime.setText(DateTime.now().toString(DATETIME_FORMAT));

                            marker = new MarkerOptions().position(newpos).title(String.format("[%s;%s]", newpos.latitude, newpos.longitude));
                            map.clear();
                            map.addMarker(marker);
                            circleOptions.center(newpos);
                            circleOptions.radius(accuracy);
                            map.addCircle(circleOptions);
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(newpos, 15));
                     }
              });
       }

       private void processMessage(String message)
       {
              Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
       }
}
