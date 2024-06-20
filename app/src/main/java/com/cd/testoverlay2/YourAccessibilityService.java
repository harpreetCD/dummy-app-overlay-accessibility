package com.cd.testoverlay2;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class YourAccessibilityService extends AccessibilityService {

    private static final String TAG = "YourAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getPackageName().toString().equals("co.news.hub")) {

            AccessibilityNodeInfo source = event.getSource();

            if (source != null) {
                String pcnResponse = fetchResponse(source);
                Log.d(TAG, pcnResponse);
            }
        }
//        final AccessibilityNodeInfo textNodeInfo = findTextViewNode(getRootInActiveWindow());
//
//        if (textNodeInfo == null) return;
//
//        Rect rect = new Rect();
//
//        textNodeInfo.getBoundsInScreen(rect);
//
//        Log.i(TAG, "The TextView Node: " + rect.toString());
    }

    private String fetchResponse(AccessibilityNodeInfo accessibilityNodeInfo) {

        String fetchedResponse = "";
        if (accessibilityNodeInfo != null) {
            for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo child = accessibilityNodeInfo.getChild(i);
                if (child != null) {

                    CharSequence text = child.getText();

                    if (text != null
                            && child.getClassName().equals(
                            Button.class.getName())) {

                        // dismiss dialog by performing action click in normal
                        // cases
                        if((text.toString().toLowerCase().equals("ok") || text
                                .toString().toLowerCase()
                                .equals("cancel"))) {

                            child.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            return fetchedResponse;

                        }

                    } else if (text != null
                            && child.getClassName().equals(
                            TextView.class.getName())) {

                        // response of normal cases
                        if (text.toString().length() > 10) {
                            fetchedResponse = text.toString();
                        }

                    } else if (child.getClassName().equals(
                            ScrollView.class.getName())) {

                        // when response comes as phone then response can be
                        // retrived from subchild
                        for (int j = 0; j < child.getChildCount(); j++) {

                            AccessibilityNodeInfo subChild = child.getChild(j);
                            CharSequence subText = subChild.getText();

                            if (subText != null
                                    && subChild.getClassName().equals(
                                    TextView.class.getName())) {

                                // response of different cases
                                if (subText.toString().length() > 10) {
                                    fetchedResponse = subText.toString();
                                }

                            }

                            else if (subText != null
                                    && subChild.getClassName().equals(
                                    Button.class.getName())) {

                                // dismiss dialog by performing action click in
                                // different
                                // cases
                                if ((subText.toString().toLowerCase()
                                        .equals("ok") || subText
                                        .toString().toLowerCase()
                                        .equals("cancel"))) {
                                    subChild.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    return fetchedResponse;
                                }

                            }
                        }
                    }
                }
            }
        }
        return fetchedResponse;
    }
    public AccessibilityNodeInfo findTextViewNode(AccessibilityNodeInfo nodeInfo) {

        //I highly recommend leaving this line in! You never know when the screen content will
        //invalidate a node you're about to work on, or when a parents child will suddenly be gone!
        //Not doing this safety check is very dangerous!
        if (nodeInfo == null) return null;

//        if(nodeInfo.getText()!=null) {
            Log.v(YourAccessibilityService.class.getSimpleName(), nodeInfo.toString());
//        }
        //Notice that we're searching for the TextView's simple name!
        //This allows us to find AppCompat versions of TextView as well
        //as 3rd party devs well names subclasses... though with perhaps
        //a few poorly named unintended stragglers!
        if (nodeInfo.getClassName().toString().contains(TextView.class.getSimpleName())) {
            return nodeInfo;
        }

        //Do other work!

        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            AccessibilityNodeInfo result = findTextViewNode(nodeInfo.getChild(i));

            if (result != null) return result;
        }

        return null;
    }

    @Override
    public void onInterrupt() {
        // Required method
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility Service Connected");
    }
}