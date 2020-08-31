package com.insoline.pnd.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Device utils.
 */
public class DeviceUtil {

    public static boolean hasBluetoothLeFeature(Context context) {
        return hasSystemFeature(context, PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static boolean hasSystemFeature(Context context, String feature) {
        PackageManager manager = context.getPackageManager();
        return manager.hasSystemFeature(feature);
    }

    public static int convertDpToPx(Context context, float valueInDp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

}
