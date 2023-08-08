package com.test.myapp.notification;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import us.zoom.sdk.ZoomVideoSDK;


public class NotificationService extends Service {
    private static final String TAG = "NotificationService";

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        NotificationService getService() {
            return NotificationService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = NotificationMgr.getConfNotification();
        if (null != notification) {
            startForeground(NotificationMgr.PT_NOTICICATION_ID, notification);
        } else {
            stopSelf();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "service onDestroy isInSession=:" + ZoomVideoSDK.getInstance().isInSession());
//        ZoomVideoSDK.getInstance().getShareHelper().stopShare();
//        ZoomVideoSDK.getInstance().leaveSession(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "service onTaskRemoved:" + rootIntent);
        NotificationMgr.removeConfNotification();
        stopSelf();
        ZoomVideoSDK.getInstance().getShareHelper().stopShare();
        ZoomVideoSDK.getInstance().leaveSession(false);
    }

}
