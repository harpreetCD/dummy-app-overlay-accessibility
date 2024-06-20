package com.cd.testoverlay2;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class YourAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Heavy processing here
                switch (event.getEventType()) {
                    case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                    case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                        Log.d("YourAccessibilityService", "Touch event detected: " + event.getEventType());
                        break;
                    default:
                        break;
                }
            }
        }).start();
    }

    @Override
    public void onInterrupt() {
        // Handle interrupt events here
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START
                | AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;

        setServiceInfo(info);

        Log.d("YourAccessibilityService", "Accessibility Service connected");
    }

    @Override
    public boolean onGesture(int gestureId) {
        // Handle gestures here
        Log.d("YourAccessibilityService", "Gesture detected: " + gestureId);
        return super.onGesture(gestureId);
    }
}