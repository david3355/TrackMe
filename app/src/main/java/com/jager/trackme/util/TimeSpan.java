package com.jager.trackme.util;

/**
 * Created by Jager on 2016.07.08..
 */
public class TimeSpan
{
       public TimeSpan(int days, int hours, int minutes, int seconds, long milliseconds)
       {
              this.days = days;
              this.hours = hours;
              this.minutes = minutes;
              this.seconds = seconds;
              this.milliseconds = milliseconds;
       }

       public TimeSpan(int hours, int minutes, int seconds, long milliseconds)
       {
              this(0, hours, minutes, seconds, milliseconds);
       }

       public TimeSpan(int minutes, int seconds, long milliseconds)
       {
              this(0, 0, minutes, seconds, milliseconds);
       }

       public TimeSpan(int seconds, long milliseconds)
       {
              this(0, 0, 0, seconds, milliseconds);
       }

       public TimeSpan(long milliseconds)
       {
              this(0, 0, 0, 0, milliseconds);
       }

       private int days;
       private int hours;
       private int minutes;
       private int seconds;
       private long milliseconds;

       public int getDays()
       {
              return days;
       }

       public void setDays(int days)
       {
              this.days = days;
       }

       public int getHours()
       {
              return hours;
       }

       public void setHours(int hours)
       {
              this.hours = hours;
       }

       public int getMinutes()
       {
              return minutes;
       }

       public void setMinutes(int minutes)
       {
              this.minutes = minutes;
       }

       public int getSeconds()
       {
              return seconds;
       }

       public void setSeconds(int seconds)
       {
              this.seconds = seconds;
       }

       public long getMilliseconds()
       {
              return milliseconds;
       }

       public void setMilliseconds(long milliseconds)
       {
              this.milliseconds = milliseconds;
       }

       public long getMillisFromSeconds(int seconds)
       {
              return seconds * 1000;
       }

       public long getMillisFromMinutes(int minutes)
       {
              return getMillisFromSeconds(minutes) * 60;
       }

       public long getMillisFromHours(int hours)
       {
              return getMillisFromMinutes(hours) * 60;
       }

       public long getMillisFromDays(int days)
       {
              return getMillisFromHours(days) * 24;
       }

       public long getTotalMillisec()
       {
              return milliseconds + getMillisFromSeconds(seconds) + getMillisFromMinutes(minutes) + getMillisFromHours(hours) + getMillisFromDays(days);
       }

}
