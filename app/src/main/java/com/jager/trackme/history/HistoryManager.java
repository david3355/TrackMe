package com.jager.trackme.history;

import org.joda.time.DateTime;

/**
 * Created by Jager on 2016.07.09..
 */
public class HistoryManager
{
       public static DateTime getDateTimeMinus(DateTime to, HistoryInterval interval)
       {
              DateTime from = null;
              int value = interval.getValue();
              switch (interval.getUnit())
              {
                     case MINUTE:
                            from = to.minusMinutes(value);
                            break;
                     case HOUR:
                            from = to.minusHours(value);
                            break;
                     case DAY:
                            from = to.minusDays(value);
                            break;
                     case MONTH:
                            from = to.minusMonths(value);
                            break;
                     case YEAR:
                            from = to.minusYears(value);
                            break;
              }
              return from;
       }
}
