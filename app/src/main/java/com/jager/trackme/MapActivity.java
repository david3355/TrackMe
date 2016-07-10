package com.jager.trackme;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jager.trackme.database.LocationsManager;
import com.jager.trackme.database.PositionsTableDef;
import com.jager.trackme.history.HistoryInterval;
import com.jager.trackme.history.HistoryManager;
import com.jager.trackme.util.BitmapHelper;
import com.jager.trackme.util.MapCalculator;
import com.jager.trackme.history.interval_list.IntervalDataProvider;
import com.jager.trackme.history.interval_list.IntervalListAdapter;
import com.jager.trackme.util.DateTimeFormats;
import com.jager.trackme.util.ModifiedDate;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends FragmentActivity implements
        OnMapReadyCallback, ExpandableListView.OnGroupClickListener, ExpandableListView.OnGroupCollapseListener, ExpandableListView.OnChildClickListener, View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, GoogleMap.OnMarkerClickListener
{

       private GoogleMap map;
       private LocationsManager locDatabase;
       private List<LatLng> selectedLocations;
       private List<Marker> markers;

       private HashMap<String, List<HistoryInterval>> intervals;
       private List<String> intervalSelector;
       private ExpandableListView expList;
       private IntervalListAdapter adapter;
       private TextView txt_from_date, txt_from_time, txt_to_date, txt_to_time;
       private SeekBar seekbar_locations;
       private DateTime dtFrom, dtTo, selectedDateTime;
       private ModifiedDate modifiedDate;


       @Override
       protected void onCreate(Bundle savedInstanceState)
       {
              super.onCreate(savedInstanceState);
              setContentView(R.layout.activity_map);
              txt_from_date = (TextView) findViewById(R.id.txt_from_date);
              txt_from_time = (TextView) findViewById(R.id.txt_from_time);
              txt_to_date = (TextView) findViewById(R.id.txt_to_date);
              txt_to_time = (TextView) findViewById(R.id.txt_to_time);
              seekbar_locations = (SeekBar) findViewById(R.id.seekbar_locations);
              expList = (ExpandableListView) findViewById(R.id.explist_intervals);
              expList.setOnGroupClickListener(this);
              expList.setOnGroupCollapseListener(this);
              expList.setOnChildClickListener(this);
              intervals = IntervalDataProvider.getInfo();
              intervalSelector = new ArrayList<>(intervals.keySet());
              adapter = new IntervalListAdapter(this, intervals, intervalSelector);
              expList.setAdapter(adapter);
              txt_from_date.setOnClickListener(this);
              txt_from_time.setOnClickListener(this);
              txt_to_date.setOnClickListener(this);
              txt_to_time.setOnClickListener(this);
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
              map.setOnMarkerClickListener(this);
              setLastDayPositions();
       }

       private void setPositionsBetween(DateTime from, DateTime to)
       {
              txt_from_date.setText(from.toString(DateTimeFormats.DATE_FORMAT));
              txt_from_time.setText(from.toString(DateTimeFormats.TIME_FORMAT));
              txt_to_date.setText(to.toString(DateTimeFormats.DATE_FORMAT));
              txt_to_time.setText(to.toString(DateTimeFormats.TIME_FORMAT));
              List<PositionsTableDef> positions = locDatabase.getPositionsBetween(from, to);
              setPositionsOnMap(positions);
       }

       private void setLastDayPositions()
       {
              dtTo = DateTime.now();
              dtFrom = dtTo.minusDays(1);
              setPositionsBetween(dtFrom, dtTo);
       }

       private void setPositionsOnMap(List<PositionsTableDef> positions)
       {
              map.clear();
              if (positions.size() == 0)
              {
                     seekbar_locations.setVisibility(View.INVISIBLE);
                     return;
              } else seekbar_locations.setVisibility(View.VISIBLE);
              selectedLocations = new ArrayList<>();
              markers = new ArrayList<>();
              MarkerOptions markerOptions;
              BitmapDescriptor icon = getOwnMarkerIcon();
              LatLng latLng;
              Marker marker;
              for (PositionsTableDef p : positions)
              {
                     latLng = new LatLng(p.getLatitude(), p.getLongitude());
                     selectedLocations.add(latLng);
                     markerOptions = new MarkerOptions();
                     markerOptions.icon(icon);
                     markerOptions.position(latLng);
                     markerOptions.snippet(String.format("[%s;%s]", latLng.latitude, latLng.longitude));
                     markerOptions.title(new DateTime(p.getTimestamp()).toString(DateTimeFormats.DATETIME_FORMAT));
                     markerOptions.anchor(0.5f, 0.5f);
                     marker = map.addMarker(markerOptions);
                     markers.add(marker);
              }

              setSeekBar(selectedLocations);

              PolylineOptions polylineOptions = new PolylineOptions();
              polylineOptions.addAll(selectedLocations);
              polylineOptions.color(Color.BLUE);
              polylineOptions.clickable(true);
              polylineOptions.width(4);
              map.addPolyline(polylineOptions);
              map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback()
              {
                     @Override
                     public void onMapLoaded()
                     {
                            zoomToBounds();
                     }
              });
       }

       private void zoomToBounds()
       {
              LatLngBounds bound = MapCalculator.getLocationWindow(selectedLocations);
              map.moveCamera(CameraUpdateFactory.newLatLngBounds(bound, 50));
       }

       private BitmapDescriptor getDefaultMarkerIcon()
       {
              return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
       }

       private BitmapDescriptor getOwnMarkerIcon()
       {
              Bitmap scaledMarkerImage = BitmapHelper.adjustImage(this, R.drawable.point);
              return BitmapDescriptorFactory.fromBitmap(scaledMarkerImage);
       }

       private void setSeekBar(List<LatLng> locations)
       {
              seekbar_locations.setMax(locations.size() - 1);
              seekbar_locations.setOnSeekBarChangeListener(new LocationsSeekbarChangeListener());
       }

       private void showDatePickerDialog(DateTime defaultDate)
       {
              selectedDateTime = defaultDate;
              new DatePickerDialog(this, this, defaultDate.getYear(), defaultDate.getMonthOfYear() - 1, defaultDate.getDayOfMonth()).show();
       }

       private void showTimePickerDialog(DateTime defaultTime)
       {
              selectedDateTime = defaultTime;
              new TimePickerDialog(this, this, defaultTime.getHourOfDay(), defaultTime.getMinuteOfHour(), true).show();
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
              dtTo = DateTime.now();
              dtFrom = HistoryManager.getDateTimeMinus(dtTo, interval);
              setPositionsBetween(dtFrom, dtTo);
              return false;
       }

       private void selectLocation(Marker marker)
       {
              marker.showInfoWindow();
              map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18));
       }

       @Override
       public void onClick(View view)
       {
              switch (view.getId())
              {
                     case R.id.txt_from_date:
                            showDatePickerDialog(dtFrom);
                            modifiedDate = ModifiedDate.FROM_DATE_MODIFIED;
                            break;
                     case R.id.txt_from_time:
                            showTimePickerDialog(dtFrom);
                            modifiedDate = ModifiedDate.FROM_DATE_MODIFIED;
                            break;
                     case R.id.txt_to_date:
                            showDatePickerDialog(dtTo);
                            modifiedDate = ModifiedDate.TO_DATE_MODIFIED;
                            break;
                     case R.id.txt_to_time:
                            showTimePickerDialog(dtTo);
                            modifiedDate = ModifiedDate.TO_DATE_MODIFIED;
                            break;
              }
       }

       private void modifyDate(ModifiedDate modified, DateTime newDateTime)
       {
              switch (modified)
              {
                     case FROM_DATE_MODIFIED:
                            dtFrom = newDateTime;
                            break;
                     case TO_DATE_MODIFIED:
                            dtTo = newDateTime;
                            break;
              }
       }

       @Override
       public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth)
       {
              modifyDate(modifiedDate, selectedDateTime.withDate(year, month + 1, dayOfMonth));
              setPositionsBetween(dtFrom, dtTo);
       }

       @Override
       public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute)
       {
              modifyDate(modifiedDate, selectedDateTime.withTime(hourOfDay, minute, 0, 0));
              setPositionsBetween(dtFrom, dtTo);
       }

       @Override
       public boolean onMarkerClick(Marker marker)
       {
              int index = markers.indexOf(marker);
              if (index != -1) seekbar_locations.setProgress(index);
              return false;
       }

       private class LocationsSeekbarChangeListener implements SeekBar.OnSeekBarChangeListener
       {
              @Override
              public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
              {
                     if (fromUser)
                     {
                            Marker selectedMarker = markers.get(progress);
                            selectLocation(selectedMarker);
                     }
              }

              @Override
              public void onStartTrackingTouch(SeekBar seekBar)
              {

              }

              @Override
              public void onStopTrackingTouch(SeekBar seekBar)
              {

              }
       }
}
