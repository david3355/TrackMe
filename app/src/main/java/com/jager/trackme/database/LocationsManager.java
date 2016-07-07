package com.jager.trackme.database;

import android.content.Context;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Jager on 2016.02.10..
 */
public class LocationsManager
{
       private LocationsManager(Context context)
       {
              database = PositionDatabaseManager.getInstance(context);
       }

       private PositionDatabaseManager database;
       private static LocationsManager self;

       public static LocationsManager getInstance(Context context)
       {
              if (self == null) self = new LocationsManager(context);
              return self;
       }

       public static LocationsManager getInstance()
       {
              if (self == null)
                     throw new RuntimeException("You must call getInstance(Context) first!");
              return self;
       }

       public List<PositionsTableDef> getPositionsBetween(DateTime from, DateTime to)
       {
              String fromvalue = String.valueOf(from.getMillis());
              String tovalue = String.valueOf(to.getMillis());
              List<PositionsTableDef> positions = database.fetchDataBetween(PositionsTableDef.COLUMN_TIMESTAMP, fromvalue, tovalue);
              return positions;
       }

       public PositionsTableDef getLatestPosition()
       {
              return database.selectMax(PositionsTableDef.COLUMN_TIMESTAMP);
       }

       public void saveNewPosition(PositionsTableDef position)
       {
              database.insertData(position);
       }

       public List<PositionsTableDef> getAllPositions()
       {
              return database.fetchAll();
       }
}
