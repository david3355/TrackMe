package com.jager.trackme.intercomponentcommunicator;

import android.os.Bundle;

/**
 * Created by Jager on 2016.06.04..
 */
public class InterComponentCommunicator
{
       /**
        *   Creates a Bundle object from the InterComponentData object
        */
       public static Bundle getBundle(InterComponentData data)
       {
              Bundle bundle = new Bundle();
              bundle.putDouble(InterComponentData.KEY_LAT, data.latitude);
              bundle.putDouble(InterComponentData.KEY_LONG, data.longitude);
              bundle.putDouble(InterComponentData.KEY_ACC, data.accuracy);
              return bundle;
       }

       /**
        *   Gets the appropriate data from the Bundle object and creates an InterComponentData object
        */
       public static InterComponentData readBundle(Bundle data)
       {
              InterComponentData icd = new InterComponentData();
              icd.latitude = data.getDouble(InterComponentData.KEY_LAT);
              icd.longitude = data.getDouble(InterComponentData.KEY_LONG);
              icd.accuracy = data.getDouble(InterComponentData.KEY_ACC);
              return icd;
       }

}
