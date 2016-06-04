package com.jager.trackme;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jager.trackme.intercomponentcommunicator.InterComponentCommunicator;
import com.jager.trackme.intercomponentcommunicator.InterComponentData;
import com.jager.trackme.util.ActivityUtil;

import org.joda.time.DateTime;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener
{
       private GoogleMap partial_map;
       private ImageView img_trackingstatus;
       private TextView txt_trackingstatus, txt_position, txt_latitude, txt_longitude, txt_accuracy, txt_lasttime;
       private Button btn_start_tracking, btn_stop_tracking;
       private ImageButton btn_map, btn_settings;
       private CircleOptions circleOptions;

       private ResultReceiver svcResultReceiver;


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
              btn_map = (ImageButton) findViewById(R.id.btn_map);
              btn_settings = (ImageButton) findViewById(R.id.btn_settings);

              btn_start_tracking.setOnClickListener(this);
              btn_stop_tracking.setOnClickListener(this);
              btn_map.setOnClickListener(this);
              btn_settings.setOnClickListener(this);
              txt_position.setOnClickListener(this);
              txt_latitude.setOnClickListener(this);
              txt_longitude.setOnClickListener(this);

              setResultReceiver();
              initCircle();

              // Obtain the SupportMapFragment and get notified when the map is ready to be used.
              SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                      .findFragmentById(R.id.partial_map);
              mapFragment.getMapAsync(this);
       }

       private void initCircle()
       {
              circleOptions = new CircleOptions();
              circleOptions.fillColor(Color.rgb(255, 0, 0));
              circleOptions.strokeColor(Color.rgb(0, 255, 0));
              circleOptions.strokeWidth(2);
       }

       private void setResultReceiver()
       {
              svcResultReceiver = new ResultReceiver(null)
              {
                     @Override
                     protected void onReceiveResult(int resultCode, Bundle resultData)
                     {
                            switch (resultCode)
                            {
                                   case TrackingService.LOCATION_SEND:
                                          locationChanged(InterComponentCommunicator.readBundle(resultData));
                                          break;
                                   case TrackingService.MESSAGE_RESULTCODE:
                                          processMessage(resultData.getString(InterComponentData.KEY_MSG));
                                          break;
                            }

                     }
              };
       }


       @Override
       public void onMapReady(GoogleMap googleMap)
       {
              partial_map = googleMap;

              // Add a marker in Sydney and move the camera
              LatLng sydney = new LatLng(-34, 151);
              partial_map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
              partial_map.moveCamera(CameraUpdateFactory.newLatLng(sydney));

              partial_map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
              {
                     @Override
                     public void onMapLongClick(LatLng latLng)
                     {
                            startActivity(MapActivity.class);
                     }
              });
       }

       @Override
       protected void onResume()
       {
              initStatusBar();
              super.onResume();
       }

       @Override
       protected void onPause()
       {
              super.onPause();
       }

       private void initStatusBar()
       {
              if (ActivityUtil.isServiceRunning(TrackingService.SERVICENAME, this))
              {
                     trackingStarted();
              } else
              {
                     trackingStopped();
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
                     case R.id.btn_map:
                            startActivity(MapActivity.class);
                            break;
                     case R.id.btn_settings:
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

       private void startTracking()
       {
              startPositioningService();
              trackingStarted();
              ActivityUtil.popup("Tracking started!", this);
       }

       private void stopTracking()
       {
              stopPositioningService();
              trackingStopped();
              ActivityUtil.popup("Tracking stopped!", this);
       }

       private void startPositioningService()
       {
              Intent svc = new Intent(this, TrackingService.class);
              svc.putExtra(TrackingService.KEY_RESULTRECEIVER, svcResultReceiver);
              startService(svc);
       }

       private void stopPositioningService()
       {
              Intent svc = new Intent(this, TrackingService.class);
              stopService(svc);
       }

       private void trackingStarted()
       {
              btn_start_tracking.setVisibility(View.GONE);
              btn_stop_tracking.setVisibility(View.VISIBLE);
              img_trackingstatus.setImageDrawable(getResources().getDrawable(R.drawable.satellite_on));
              txt_trackingstatus.setText(ActivityUtil.getStringRes(this, R.string.tracking_on));
       }

       private void trackingStopped()
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
                            txt_lasttime.setText(DateTime.now().toString("YYYY-MM-dd HH:mm:ss"));

                            circleOptions.center(newpos);
                            circleOptions.radius(accuracy);
                            partial_map.addCircle(circleOptions);
                            partial_map.moveCamera(CameraUpdateFactory.newLatLng(newpos));
                     }
              });
       }

       private void processMessage(String message)
       {
              // TODO: process message
       }
}
