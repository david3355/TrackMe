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
              for(LatLng l : locations)
              {
                     lat += l.latitude;
                     lng += l.longitude;
              }
              return new LatLng(lat/locations.size(), lng/locations.size());
       }

}
