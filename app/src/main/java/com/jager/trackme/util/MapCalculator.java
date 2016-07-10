package com.jager.trackme.util;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;

/**
 * Created by Jager on 2016.07.08..
 */
public class MapCalculator
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
       public static LatLngBounds getLocationWindow(List<LatLng> locations)
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

              LatLng southwest = new LatLng(locations.get(min_bottom).latitude, locations.get(min_left).longitude);
              LatLng northeast = new LatLng(locations.get(min_up).latitude, locations.get(min_right).longitude);
              LatLngBounds bounds = new LatLngBounds(southwest, northeast);
              return bounds;
       }

       public static LatLng getLocationWindowCenter(List<LatLng> locations)
       {
              LatLngBounds bounds = getLocationWindow(locations);
              return bounds.getCenter();
       }

}
