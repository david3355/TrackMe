package com.jager.trackme;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jager.trackme.database.LocationsManager;
import com.jager.trackme.database.PositionsTableDef;
import com.jager.trackme.history.HistoryInterval;
import com.jager.trackme.history.HistoryManager;
import com.jager.trackme.util.Calculator;
import com.jager.trackme.history.interval_list.IntervalDataProvider;
import com.jager.trackme.history.interval_list.IntervalListAdapter;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends FragmentActivity implements
        OnMapReadyCallback, ExpandableListView.OnGroupClickListener, ExpandableListView.OnGroupCollapseListener, ExpandableListView.OnChildClickListener
{

       private GoogleMap map;
       private LocationsManager locDatabase;

       private HashMap<String, List<HistoryInterval>> intervals;
       private List<String> intervalSelector;
       private ExpandableListView expList;
       private IntervalListAdapter adapter;
       private TextView txt_from, txt_to;

       @Override
       protected void onCreate(Bundle savedInstanceState)
       {
              super.onCreate(savedInstanceState);
              setContentView(R.layout.activity_map);
              txt_from = (TextView) findViewById(R.id.txt_from);
              txt_to = (TextView) findViewById(R.id.txt_to);
              expList = (ExpandableListView) findViewById(R.id.explist_intervals);
              expList.setOnGroupClickListener(this);
              expList.setOnGroupCollapseListener(this);
              expList.setOnChildClickListener(this);
              intervals = IntervalDataProvider.getInfo();
              intervalSelector = new ArrayList<>(intervals.keySet());
              adapter = new IntervalListAdapter(this, intervals, intervalSelector);
              expList.setAdapter(adapter);
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
              setPositionsOnMap(positions);
       }

       private void setPositionsOnMap(List<PositionsTableDef> positions)
       {
              map.clear();
              if (positions.size() == 0) return;
              List<LatLng> locations = new ArrayList<>();
              MarkerOptions marker;
              BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
              LatLng latLng;
              for (PositionsTableDef p : positions)
              {
                     latLng = new LatLng(p.getLatitude(), p.getLongitude());
                     locations.add(latLng);
                     marker = new MarkerOptions();
                     marker.icon(icon);
                     marker.position(latLng);
                     marker.snippet(new DateTime(p.getTimestamp()).toString(MainActivity.DATETIME_FORMAT));
                     marker.title(String.format("[%s;%s]", latLng.latitude, latLng.longitude));
                     map.addMarker(marker);
              }

              PolylineOptions polylineOptions = new PolylineOptions();
              polylineOptions.addAll(locations);
              polylineOptions.color(Color.BLUE);
              polylineOptions.clickable(true);
              polylineOptions.width(4);
              map.addPolyline(polylineOptions);
              LatLng avgPosition = Calculator.getLocationWindow(locations);
              map.moveCamera(CameraUpdateFactory.newLatLngZoom(avgPosition, 11)); // TODO: zoomolás a window-tól függjön!
       }

       private void showDatePickerDialog()
       {
              //new DatePickerDialog(this, this, 2016, 07, 07).show();
       }


       @Override
       public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id)
       {
              LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 350);
              expList.setLayoutParams(params);
              return false;
       }

       @Override
       public void onGroupCollapse(int groupID)
       {
              LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
              expList.setLayoutParams(params);
       }

       @Override
       public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long i)
       {
              HistoryInterval interval = intervals.get(intervalSelector.get(groupPosition)).get(childPosition);
              DateTime to = DateTime.now();
              DateTime from = HistoryManager.getDateTimeMinus(to, interval);
              txt_from.setText(from.toString(MainActivity.DATETIME_FORMAT));
              txt_to.setText(to.toString(MainActivity.DATETIME_FORMAT));

              List<PositionsTableDef> positions = locDatabase.getPositionsBetween(from, to);
              setPositionsOnMap(positions);

              return false;
       }
}
