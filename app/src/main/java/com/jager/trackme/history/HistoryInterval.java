package com.jager.trackme.history;

import com.jager.trackme.util.TimeSpan;

import org.joda.time.DateTime;

/**
 * Created by Jager on 2016.07.09..
 */
public class HistoryInterval
{
       public HistoryInterval(int value, Unit unit)
       {
              this.value = value;
              this.unit = unit;
       }

       private int value;
       private Unit unit;

       public int getValue()
       {
              return value;
       }

       public void setValue(int value)
       {
              this.value = value;
       }

       public Unit getUnit()
       {
              return unit;
       }

       public void setUnit(Unit unit)
       {
              this.unit = unit;
       }

       @Override
       public String toString()
       {
              String unitText = "";
              switch (unit)
              {
                     case MINUTE:
                            unitText = "minute";
                            break;
                     case HOUR:
                            unitText = "hour";
                            break;
                     case DAY:
                            unitText = "day";
                            break;
                     case MONTH:
                            unitText = "month";
                            break;
                     case YEAR:
                            unitText = "year";
                            break;
              }

              return String.format("%s %s", value, unitText);
       }

       public TimeSpan toTimeSpan()
       {
              TimeSpan ts = null;
              switch (unit)
              {
                     case MINUTE:
                            ts = new TimeSpan(value, 0, 0);
                            break;
                     case HOUR:
                            ts = new TimeSpan(value, 0, 0, 0, 0);
                            break;
                     case DAY:
                            ts = new TimeSpan(value, 0, 0, 0, 0);
                            break;
                     case MONTH:
                            break;
                     case YEAR:
                            break;
              }
              return ts;
       }
}
