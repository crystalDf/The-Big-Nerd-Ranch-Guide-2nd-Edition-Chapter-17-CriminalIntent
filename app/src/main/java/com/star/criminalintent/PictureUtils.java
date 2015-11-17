package com.star.criminalintent;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;

public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;

        if ((srcWidth > destWidth) || (srcHeight > destHeight)) {
            int widthRatio = (int) (srcWidth / destWidth);
            int heightRatio = (int) (srcHeight / destHeight);
            inSampleSize = Math.max(widthRatio, heightRatio);
        }

        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        Display display = activity.getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
        } else {
            size.x = display.getWidth();
            size.y = display.getHeight();
        }

        return getScaledBitmap(path, size.x, size.y);
    }
}
