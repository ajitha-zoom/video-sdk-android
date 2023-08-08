package com.test.myapp.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

public class ZMAdapterOsBugHelper {
    private static final ZMAdapterOsBugHelper ourInstance = new ZMAdapterOsBugHelper();
    private boolean mCanDraw;
    private AppOpsManager.OnOpChangedListener mOnOpChangedListener = null;

    private ZMAdapterOsBugHelper() {
    }

    public static ZMAdapterOsBugHelper getInstance() {
        return ourInstance;
    }

    public boolean isNeedListenOverlayPermissionChanged() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == (Build.VERSION_CODES.O + 1));
    }

    //fix the bug that "Settings.canDrawOverlays(context)" return false even if user allow the app to draw overlay in android 8.0 and 8.1
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startListenOverlayPermissionChange(Context context) {
        AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        if (opsManager == null)
            return;
        mCanDraw = Settings.canDrawOverlays(context);
        final String myPackageName = context.getPackageName();
        if (TextUtils.isEmpty(myPackageName))
            return;
        mOnOpChangedListener = new AppOpsManager.OnOpChangedListener() {

            @Override
            public void onOpChanged(String op, String packageName) {
                if (myPackageName.equals(packageName) &&
                        AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW.equals(op)) {
                    mCanDraw = !mCanDraw;
                }
            }
        };
        opsManager.startWatchingMode(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                null, mOnOpChangedListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void stopListenOverlayPermissionChange(Context context) {
        if (mOnOpChangedListener != null) {
            AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (opsManager == null)
                return;
            opsManager.stopWatchingMode(mOnOpChangedListener);
            mOnOpChangedListener = null;
        }
    }

    public boolean ismCanDraw() {
        return mCanDraw;
    }

}