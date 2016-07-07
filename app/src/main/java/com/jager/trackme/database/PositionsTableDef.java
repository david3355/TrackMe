package com.jager.trackme.database;

public class PositionsTableDef
{
       public static String DATABASE_NAME = "locations.db";

       public static final String DB_TABLE = "positions";
       public static final String COLUMN_ID = "id";
       public static final String COLUMN_LATITUDE = "latitude";
       public static final String COLUMN_LONGITUDE = "longitude";
       public static final String COLUMN_ACCURACY = "accuracy";
       public static final String COLUMN_TIMESTAMP = "timestamp";

       public static final String DATABASE_CREATE = String
               .format("CREATE TABLE IF NOT EXISTS %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s REAL, %s REAL, %s INTEGER, %s INTEGER)",
                       DB_TABLE, COLUMN_ID, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_ACCURACY, COLUMN_TIMESTAMP);
       public static final String DATABASE_DROP = String.format("drop table if exists %s", DB_TABLE);

       public PositionsTableDef(double latitude, double longitude, int accuracy, long timestamp)
       {
              this.latitude = latitude;
              this.longitude = longitude;
              this.accuracy = accuracy;
              this.timestamp = timestamp;
       }

       private double latitude;
       private double longitude;
       private int accuracy;
       private long timestamp;

       public double getLatitude()
       {
              return latitude;
       }

       public void setLatitude(double latitude)
       {
              this.latitude = latitude;
       }

       public double getLongitude()
       {
              return longitude;
       }

       public void setLongitude(double longitude)
       {
              this.longitude = longitude;
       }

       public int getAccuracy()
       {
              return accuracy;
       }

       public void setAccuracy(int accuracy)
       {
              this.accuracy = accuracy;
       }

       public long getTimestamp()
       {
              return timestamp;
       }

       public void setTimestamp(long timestamp)
       {
              this.timestamp = timestamp;
       }
}
