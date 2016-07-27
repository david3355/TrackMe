package com.jager.trackme;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ImageView;
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
        OnMapReadyCallback, ExpandableListView.OnGroupClickListener, ExpandableListView.OnGroupCollapseListener,
        ExpandableListView.OnChildClickListener, View.OnClickListener, DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener, GoogleMap.OnMarkerClickListener, CheckBox.OnCheckedChangeListener
{
       private ExpandableListView expList;
       private TextView txt_from_date, txt_from_time, txt_to_date, txt_to_time;
       private SeekBar seekbar_locations;
       private CheckBox check_posvisible, check_zoompos;
       private ImageView btn_mapsettings, btn_boundzoom;
       private LinearLayout panel_mapoptions;

       private GoogleMap map;
       private LocationsManager locDatabase;
       private List<LatLng> selectedLocations;
       private List<Marker> markers;
       private List<Long> timestamps;
       private HashMap<String, List<HistoryInterval>> intervals;
       private IntervalListAdapter adapter;
       private List<String> intervalSelector;
       private DateTime dtFrom, dtTo, selectedDateTime;
       private ModifiedDate modifiedDate;
       private boolean zoomToPosition;
       private boolean addMarkers;
       private MarkerOptions selectedPosMarkerOptions;
       private Marker selectedPosMarker;
       private int pathLineWidht;

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
              check_posvisible = (CheckBox) findViewById(R.id.check_posvisible);
              check_zoompos = (CheckBox) findViewById(R.id.check_zoompos);
              btn_mapsettings = (ImageView) findViewById(R.id.btn_mapsettings);
              btn_boundzoom = (ImageView) findViewById(R.id.btn_boundzoom);
              panel_mapoptions = (LinearLayout) findViewById(R.id.panel_mapoptions);
              btn_mapsettings.setOnClickListener(this);
              btn_boundzoom.setOnClickListener(this);
              check_posvisible.setOnCheckedChangeListener(this);
              check_zoompos.setOnCheckedChangeListener(this);
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

              init();

              // Obtain the SupportMapFragment and get notified when the map is ready to be used.
              SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                      .findFragmentById(R.id.map);
              mapFragment.getMapAsync(this);
       }

       private void init()
       {
              locDatabase = LocationsManager.getInstance();
              zoomToPosition = false;
              addMarkers = false;
              selectedPosMarkerOptions = new MarkerOptions();
              selectedPosMarkerOptions.icon(getOwnMarkerIcon(R.drawable.gpoint));
              selectedPosMarkerOptions.anchor(0.5f, 0.5f);
              selectedPosMarker = null;
              pathLineWidht = 6;
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
              timestamps = new ArrayList<>();
              MarkerOptions markerOptions;
              BitmapDescriptor icon = getOwnMarkerIcon(R.drawable.point);
              LatLng latLng;
              Marker marker;
              if (markers != null)
              {
                     markers.clear();
                     markers = null;
              }
              if (addMarkers) markers = new ArrayList<>();

              for (PositionsTableDef p : positions)
              {
                     latLng = new LatLng(p.getLatitude(), p.getLongitude());
                     selectedLocations.add(latLng);
                     timestamps.add(p.getTimestamp());

                     if (addMarkers)
                     {
                            markerOptions = new MarkerOptions();
                            markerOptions.icon(icon);
                            markerOptions.position(latLng);
                            markerOptions.snippet(String.format("[%s;%s]", latLng.latitude, latLng.longitude));
                            markerOptions.title(new DateTime(p.getTimestamp()).toString(DateTimeFormats.DATETIME_FORMAT));
                            markerOptions.anchor(0.5f, 0.5f);
                            marker = map.addMarker(markerOptions);
                            markers.add(marker);
                     }
              }

              setSeekBar(selectedLocations);

              PolylineOptions polylineOptions = new PolylineOptions();
              polylineOptions.addAll(selectedLocations);
              polylineOptions.color(Color.BLUE);
              polylineOptions.clickable(true);
              polylineOptions.width(pathLineWidht);
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
              if (selectedLocations.size() > 0)
              {
                     LatLngBounds bound = MapCalculator.getLocationWindow(selectedLocations);
                     map.moveCamera(CameraUpdateFactory.newLatLngBounds(bound, 50));
              }
       }

       private BitmapDescriptor getDefaultMarkerIcon()
       {
              return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
       }

       private BitmapDescriptor getOwnMarkerIcon(int imageResourceID)
       {
              Bitmap scaledMarkerImage = BitmapHelper.adjustImage(this, imageResourceID);
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
              LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400);
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
              if (zoomToPosition)
                     map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 18));
       }

       private void selectLocation(LatLng position, DateTime time)
       {
              if (selectedPosMarker != null) selectedPosMarker.remove();
              selectedPosMarkerOptions.position(position);
              selectedPosMarkerOptions.snippet(String.format("[%s;%s]", position.latitude, position.longitude));
              selectedPosMarkerOptions.title(time.toString(DateTimeFormats.DATETIME_FORMAT));
              selectedPosMarker = map.addMarker(selectedPosMarkerOptions);
              selectedPosMarker.showInfoWindow();
              if (zoomToPosition)
                     map.animateCamera(CameraUpdateFactory.newLatLngZoom(selectedPosMarker.getPosition(), 18));
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
                     case R.id.btn_mapsettings:
                            if (panel_mapoptions.getVisibility() == View.VISIBLE)
                                   panel_mapoptions.setVisibility(View.GONE);
                            else panel_mapoptions.setVisibility(View.VISIBLE);
                            break;
                     case R.id.btn_boundzoom:
                            zoomToBounds();
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

       @Override
       public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
       {
              switch (compoundButton.getId())
              {
                     case R.id.check_posvisible:
                            addMarkers = isChecked;
                            setPositionsBetween(dtFrom, dtTo);
                            break;
                     case R.id.check_zoompos:
                            zoomToPosition = isChecked;
                            break;
              }
       }

       private class LocationsSeekbarChangeListener implements SeekBar.OnSeekBarChangeListener
       {
              @Override
              public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
              {
                     if (fromUser)
                     {
                            if (markers != null)
                            {
                                   selectLocation(markers.get(progress));
                            } else
                            {
                                   selectLocation(selectedLocations.get(progress), new DateTime(timestamps.get(progress)));
                            }
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
