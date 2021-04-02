package com.eyecool.face.compare.demo.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class MeasureUtil {

    private static final String TAG = "MeasureUtil";

    public static int[] getScreenSize(Context activity) {
        DisplayMetrics dm = new DisplayMetrics();
        Log.d(TAG, "activity" + activity);
        dm = activity.getResources().getDisplayMetrics();
        float density = dm.density;
        int densityDPI = dm.densityDpi;
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int[] screens = {screenWidth, screenHeight};
        return screens;
    }

    /**
     * @param context
     * @param dpValue
     * @return dp to px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     *
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public static Point getScreenPoint(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            return new Point(display.getWidth(), display.getHeight());
        } else {
            Point point = new Point();
            display.getSize(point);
            return point;
        }
    }

    /**
     * @param context
     * @param pxValue
     * @return px to dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
