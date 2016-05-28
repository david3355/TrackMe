package com.jager.trackme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
{
       private GoogleMap partial_map;


       @Override
       protected void onCreate(Bundle savedInstanceState)
       {
              super.onCreate(savedInstanceState);
              setContentView(R.layout.activity_main);

              // Obtain the SupportMapFragment and get notified when the map is ready to be used.
              SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                      .findFragmentById(R.id.partial_map);
              mapFragment.getMapAsync(this);
       }

       @Override
       public void onMapReady(GoogleMap googleMap)
       {
              partial_map = googleMap;

              // Add a marker in Sydney and move the camera
              LatLng sydney = new LatLng(-34, 151);
              partial_map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
              partial_map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
       }
}
