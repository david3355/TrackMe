package com.jager.trackme.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Jager on 2016.07.09..
 */
public class BitmapHelper
{
       public static Bitmap adjustImage(Context context, int imageResource)
       {
              float scalerate = 0.1f;
              Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageResource);
              int scaledx = (int) (bitmap.getWidth() * scalerate);
              int scaledy = (int) (bitmap.getHeight() * scalerate);
              Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, scaledx, scaledy, false);
              return resizedBitmap;
       }
}
