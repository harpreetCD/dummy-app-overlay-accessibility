package com.cd.testoverlay2;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForegroundService extends Service {

    private static final String TAG = ForegroundService.class.getSimpleName();
    private Handler handler;
    WindowManager.LayoutParams mParams = null;
    LayoutInflater layoutInflater;
    View mView;
    WindowManager mWindowManager;
    TextView title;
    ImageView appIcon;

    public ForegroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // create the custom or default notification
        // based on the android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // set the layout parameters of the window
            mParams = new WindowManager.LayoutParams(
                    // Shrink the window to wrap the content rather
                    // than filling the screen
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);

        }

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        mParams.gravity = Gravity.TOP | Gravity.LEFT;
        mParams.x = -width;
        mParams.y = -height;

        // getting a LayoutInflater
        layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.popup_window, null);
        title = mView.findViewById(R.id.timer);
        appIcon = mView.findViewById(R.id.app_icon);
        // set onClickListener on the remove button, which removes
        // the view from the window

        // Define the position of the
        // window within the screen
        mParams.gravity = Gravity.CENTER;
        mWindowManager = (WindowManager)getApplicationContext().getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mView, mParams);
        handler = new Handler();
        startUpdatingAppName();


    }

    private String lastOpenedApp;
    private Handler apphandler;
    private Runnable appRunnable;

    private Handler visibilityHandler;

    private Map<String, Integer> appTimes = new HashMap<>();
    private void startUpdatingAppName() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                String currentApp = UsageStatsHelper.getForegroundApp(ForegroundService.this);
//                if(currentApp!="" && currentApp!=lastOpenedApp){
//                    title.setVisibility(View.INVISIBLE);
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            title.setVisibility(View.VISIBLE);
//                        }
//                    }, 1000);
//                }
                if(currentApp==""){
                    currentApp = lastOpenedApp;
                }
                lastOpenedApp = currentApp;
                if(apphandler==null){

//                    for (Map.Entry<String, Handler> entry : apphandlers.entrySet()) {
//                        String key = entry.getKey();
//                        if(key!=currentApp) {
//                            apphandlers.remove(key);
//                            appRunnables.remove(key);
//                        }
//                    }
                    apphandler = new Handler(Looper.getMainLooper());
                    appRunnable = new Runnable() {
                        @Override
                        public void run() {
                            // Your code to run periodically
                            // Schedule the runnable again after the specified delay
                            if(!appTimes.containsKey(lastOpenedApp)){
                                appTimes.put(lastOpenedApp,0);
                            }
                            appTimes.put(lastOpenedApp, appTimes.get(lastOpenedApp)+1);
                            title.setText(formatSeconds(appTimes.get(lastOpenedApp)));
//                            Log.d("Debug:weklfhioqwefhqwe", "run: " + lastOpenedApp);
                            apphandler.postDelayed(this, 1000); // 1000 ms = 1 second
                        }
                    };
                    apphandler.postDelayed(appRunnable, 1000);
                }
                PackageManager packageManager = getApplicationContext().getPackageManager();
                ApplicationInfo applicationInfo = null;
                try {
                    applicationInfo = packageManager.getApplicationInfo(currentApp, 0);
                    String appName = packageManager.getApplicationLabel(applicationInfo).toString();
                    appIcon.setImageDrawable(packageManager.getApplicationIcon(applicationInfo));
                    currentApp = appName;
//                    Log.d("PackageManager", "run: ");
                } catch (PackageManager.NameNotFoundException e) {
//                    throw new RuntimeException(e);
                }
//                Log.d("PackageManager", "run: ");

                handler.postDelayed(this, 0 ); // Update every second
            }
        });
    }

    public String formatSeconds(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String packageName = intent.getStringExtra("package_name");
        Log.d(TAG, "onStartCommand: " + packageName);

        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(launchIntent);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AppStateEnum status = UsageStatsHelper.getAppStatus(getApplicationContext(), packageName);
                Log.d(TAG, "foregroundApp: " + packageName+ " App state: " + status);
                if(status == AppStateEnum.RUNNING) {
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    // for android version >=O we need to create
    // custom notification stating
    // foreground service is running
    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Service running")
                .setContentText("Displaying over other apps")

                // this is important, otherwise the notification will show the way
                // you want i.e. it will show some default notification
                .setSmallIcon(R.drawable.ic_launcher_foreground)

                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    public void close() {

        try {
            // remove the view from the window
            ((WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE)).removeView(mView);
            // invalidate the view
            mView.invalidate();
            // remove all views
            ((ViewGroup) mView.getParent()).removeAllViews();

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
        } catch (Exception e) {
            Log.d("Error2", e.toString());
        }
    }


}