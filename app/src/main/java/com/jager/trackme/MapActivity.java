package com.jager.trackme;

import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jager.trackme.database.LocationsManager;
import com.jager.trackme.database.PositionsTableDef;
import com.jager.trackme.util.Calculator;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback
{

       private GoogleMap map;
       private LocationsManager locDatabase;

       @Override
       protected void onCreate(Bundle savedInstanceState)
       {
              super.onCreate(savedInstanceState);
              setContentView(R.layout.activity_map);
              locDatabase = LocationsManager.getInstance();
              // Obtain the SupportMapFragment and get notified when the map is ready to be used.
              SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                      .findFragmentById(R.id.map);
              mapFragment.getMapAsync(this);
       }


       @Override
       public void onMapReady(GoogleMap googleMap)
       {
              map = googleMap;

              List<PositionsTableDef> positions = locDatabase.getAllPositions();
              List<LatLng> locations = new ArrayList<>();
              for (PositionsTableDef p : positions)
              {
                     locations.add(new LatLng(p.getLatitude(), p.getLongitude()));
              }

              PolylineOptions polylineOptions = new PolylineOptions();
              polylineOptions.addAll(locations);
              polylineOptions.color(Color.BLUE);
              polylineOptions.clickable(true);
              polylineOptions.width(2);
              map.addPolyline(polylineOptions);
              LatLng avgPosition = Calculator.calculateAverageLocation(locations);
              map.moveCamera(CameraUpdateFactory.newLatLngZoom(avgPosition, 13));
       }
}
