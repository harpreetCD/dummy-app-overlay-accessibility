package com.cd.testoverlay2;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class UsageStatsHelper {

    private static final String TAG = UsageStatsHelper.class.getSimpleName();

    public static String getForegroundApp(Context context) {
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - 10000; // check last 10 seconds

        UsageEvents usageEvents = usageStatsManager.queryEvents(beginTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();
        String packageName = "";
        String eventTime = "";
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event);
//            if (event.getEventType() == UsageEvents.Event.USER_INTERACTION) {
                packageName = event.getPackageName();
//                if(packageName.equals("com.JindoBlu.FourPlayers")) {
                    Log.d(TAG, "packageName: " + packageName);
                    int eventType = event.getEventType();
                    Log.d(TAG, "eventType: " + eventType);
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        int appStandByBucket = event.getAppStandbyBucket();
                        Log.d(TAG, "appStandByBucket: " + appStandByBucket);
                    }
                    long timeStamp = event.getTimeStamp();
                    Log.d(TAG, "timeStamp: " + timeStamp);
//                }
//            }
        }

        return packageName;
    }
}
