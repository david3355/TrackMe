package com.jager.trackme.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Jager on 2016.07.08..
 */
public class Calculator
{
       public static LatLng calculateAverageLocation(List<LatLng> locations)
       {
              double lat, lng;
              lat = lng = 0;
              for (LatLng l : locations)
              {
                     lat += l.latitude;
                     lng += l.longitude;
              }
              return new LatLng(lat / locations.size(), lng / locations.size());
       }

       /**
        * Calculates the uppermost, lowermost, leftmost and rightmost points, as the window of locations, and calculates the midpoints of these
        * @param locations
        */
       public static LatLng getLocationWindow(List<LatLng> locations)
       {
              LatLng pos; // longitude ~ x and latitude ~ y
              int min_up, min_bottom, min_left, min_right;
              min_up = min_bottom = min_left = min_right = 0;
              double x, y;
              for (int i = 1; i < locations.size(); i++)
              {
                     pos = locations.get(i);
                     x = locations.get(i).longitude;
                     y = locations.get(i).latitude;
                     if (y > locations.get(min_up).latitude) min_up = i;
                     if (y < locations.get(min_bottom).latitude) min_bottom = i;
                     if (x < locations.get(min_left).longitude) min_left = i;
                     if (x > locations.get(min_right).longitude) min_right = i;
              }
              double lat = (locations.get(min_up).latitude + locations.get(min_bottom).latitude)/2;
              double lng = (locations.get(min_left).longitude + locations.get(min_right).longitude)/2;
              return new LatLng(lat, lng);
       }

}
