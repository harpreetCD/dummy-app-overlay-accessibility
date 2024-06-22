package com.cd.testoverlay2;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!hasUsageStatsPermission(this)) {
            requestUsageStatsPermission();
        } else {
            // Permission is granted, proceed with your functionality
        }


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(this)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:" + getPackageName()));
//                startActivityForResult(intent, 1234);
//            } else {
//                startOverlayService();
//            }
//        } else {
//            startOverlayService();
//        }
        startService();

//        Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
//        startActivityForResult(intent, 789);

        Button openAppBtn = findViewById(R.id.openBtn);

        openAppBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.JindoBlu.FourPlayers");
                startActivityForResult( launchIntent, 1111 );
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String foregroundApp = UsageStatsHelper.getForegroundApp(MainActivity.this);
                Log.d(TAG, "foregroundApp: " + foregroundApp);
                handler.postDelayed(this, 10000);
            }
        }, 10000);
    }

    private boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1234) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    startOverlayService();
                }
            }
        }
        if (requestCode == 1111) {
            Log.d(MainActivity.class.getSimpleName(), "onActivityResult: " + resultCode );
        }
//        if(requestCode == 789) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(new Intent(this, YourAccessibilityService.class));
//            } else {
//                startService(new Intent(this, YourAccessibilityService.class));
//            }
//        }
    }

    private void startOverlayService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, OverlayService.class));
        } else {
            startService(new Intent(this, OverlayService.class));
        }
    }

    public void startService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check if the user has already granted
            // the Draw over other apps permission
            if(Settings.canDrawOverlays(this)) {
                // start the service based on the android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(new Intent(this, ForegroundService.class));
                } else {
                    startService(new Intent(this, ForegroundService.class));
                }
            }
        }else{
            startService(new Intent(this, ForegroundService.class));
        }
    }
}