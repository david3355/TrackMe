package com.jager.trackme.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Jager on 2016.04.10..
 */
public class ActivityUtil
{
       public static void clearFields(EditText... fields)
       {
              for (EditText et : fields)
              {
                     clearField(et);
              }
       }

       public static void clearField(EditText edittext)
       {
              edittext.setText("");
       }

       public static void popup(final String text, final Activity context)
       {
              context.runOnUiThread(new Runnable()
              {
                     @Override
                     public void run()
                     {
                            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
                     }
              });
       }

       public static String getStringRes(Context context, int id)
       {
              return context.getResources().getString(id);
       }

       public static boolean isServiceRunning(String serviceName, Context context)
       {
              ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
              for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
              {
                     if (serviceName.equals(service.service.getClassName()))
                     {
                            return true;
                     }
              }
              return false;
       }

}
