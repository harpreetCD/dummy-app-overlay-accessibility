package com.cd.testoverlay2;

import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.recyclerview.widget.SortedList;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

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
            if (packageName.equals("com.flipkart.android")) {
                Log.d(TAG, "packageName: " + packageName);
                int eventType = event.getEventType();
                Log.d(TAG, "eventType: " + eventType);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    int appStandByBucket = event.getAppStandbyBucket();
                    Log.d(TAG, "appStandByBucket: " + appStandByBucket);
                }
                long timeStamp = event.getTimeStamp();
                Log.d(TAG, "timeStamp: " + timeStamp);
            }
//            }
        }

//        Log.d(TAG, "getForegroundApp: " + isAppInBackground(context, "com.flipkart.android"));
        return packageName;
    }

    public static boolean isAppInBackground(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.processName.equals(packageName)) {
                return processInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                        processInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
            }
        }
        return false;
    }

    public static AppStateEnum getAppStatus(Context context, String targetPackageName) {
        String currentApp = "NULL";
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService("usagestats");
        long time = System.currentTimeMillis();
        long startTime = time - 24 * 60 * 60 * 1000;
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, time);
        if (appList != null && appList.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (mySortedMap != null && !mySortedMap.isEmpty()) {
                if (mySortedMap.get(mySortedMap.lastKey()).getPackageName().equals(targetPackageName)) {

                    ArrayList<Integer> onlyTargetMap = new ArrayList<>();
                    SortedMap<Long, UsageStats> onlyTargetUsageMap = new TreeMap<>();
                    UsageEvents usageEvents = usm.queryEvents(startTime, System.currentTimeMillis());
                    UsageEvents.Event event = new UsageEvents.Event();
                    String packageName = "";
                    String eventTime = "";
                    UsageEvents.Event lastTargetAppEvent = new UsageEvents.Event();
                    while (usageEvents.hasNextEvent()) {
                        usageEvents.getNextEvent(event);
                        if (event.getPackageName().equals(targetPackageName) && event.getClassName()!=null &&!event.getClassName().toLowerCase().contains("splash")) {
                            Log.d(TAG, "printForegroundTask rklsjwhpe: " + event.getClassName() + " eventType: " + event.getEventType());
                            onlyTargetMap.add(event.getEventType());
                        }
                    }
                    for (Long key : mySortedMap.keySet()) {
                        if (mySortedMap.get(key).getPackageName().equals(targetPackageName)) {
                            onlyTargetUsageMap.put(key, mySortedMap.get(key));
                        }
                    }
                    Log.d(TAG, "printForegroundTask: last event " + onlyTargetMap);
                    SortedMap<Long, UsageStats> targetMap = new TreeMap<>();

                    Log.d(TAG, "printForegroundTask: last used " + humanReadableTime(Objects.requireNonNull(onlyTargetUsageMap.get(onlyTargetUsageMap.lastKey())).getLastTimeUsed()) + " " + humanReadableTime(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(60)));

                    Log.d(TAG, "printForegroundTask: isAppKilled " + isAppKilled(context, targetPackageName));
                    if(onlyTargetMap.size()==0) return AppStateEnum.LASTSTATE;
                    if (onlyTargetMap.get(onlyTargetMap.size() - 1) == 1) {
                        return AppStateEnum.RUNNING;
                    } else if (onlyTargetMap.get(onlyTargetMap.size() - 1) == 23) {
                        return AppStateEnum.PAUSED;
                    }
                } else {
                    return AppStateEnum.KILLED;
                }
            }
        }
        Log.d(TAG, "foregroundApp: " + "this called");
        return AppStateEnum.KILLED;
    }

    private static String humanReadableTime(long time) {
        Date date = new Date(time);
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
        formatter.setTimeZone(TimeZone.getTimeZone("IST"));
        return formatter.format(date);
    }

//    public static boolean isAppKilled(Context context, String packageName) {
//        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//        long currentTime = System.currentTimeMillis();
//        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
//                currentTime - TimeUnit.MINUTES.toMillis(1000), currentTime);
//
//        if (usageStatsList != null) {
//            for (UsageStats usageStats : usageStatsList) {
//                if (usageStats.getPackageName().equals(packageName)) {
//                    Log.d(TAG, "isAppKilled: " + humanReadableTime(usageStats.getLastTimeUsed()) + " " +humanReadableTime(currentTime - TimeUnit.MINUTES.toMillis(10)));
//                    return usageStats.getLastTimeUsed() < (currentTime - TimeUnit.MINUTES.toMillis(10));
//                }
//            }
//        }
//        return true;  // App is not running (killed)
//    }

    public static boolean isAppKilled(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = activityManager.getRunningAppProcesses();

        if (runningProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.processName.equals(packageName)) {
                    return false;  // App is running
                }
            }
        }
        return true;  // App is not running (killed)
    }
}
