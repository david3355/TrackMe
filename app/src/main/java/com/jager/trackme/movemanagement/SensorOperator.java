package com.jager.trackme.movemanagement;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.joda.time.DateTime;

/**
 * Created by Jager on 2016.07.10..
 */
public class SensorOperator implements SensorEventListener
{
       public  SensorOperator(Context context, DeviceMovesEventListener moveEventListener)
       {
              this.context = context;
              this.moveEventListener = moveEventListener;
              this.mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
              this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
       }

       private final Context context;
       private final SensorManager mSensorManager;
       private final Sensor mAccelerometer;
       private DateTime sensorChangedTime;
       private DeviceMovesEventListener moveEventListener;

       public void registerAccelerometer()
       {
              mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
       }

       public void unregisterAccelerometer()
       {
              mSensorManager.unregisterListener(this);
       }

       @Override
       public void onSensorChanged(SensorEvent sensorEvent)
       {
              sensorChangedTime = DateTime.now();
              float x, y, z;
              x = sensorEvent.values[0];
              y = sensorEvent.values[1];
              z = sensorEvent.values[2];
              moveEventListener.deviceMoves(x, y, z);

              Log.d("SensorOperator", String.format("Sensor data changed! x=%s, y=%s, z=%s", x, y, z));
       }

       @Override
       public void onAccuracyChanged(Sensor sensor, int i)
       {

       }
}
