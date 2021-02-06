package com.support.utills;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;

public class ViewUtils {
    private static String TAG = "ViewUtils";

    public static View getViewFromLayout(Activity mActivity, int mContainerID, int mLayoutID) {
        FrameLayout activityContainer = mActivity.findViewById(mContainerID);
        activityContainer.removeAllViews();
        View vi = mActivity.getLayoutInflater().inflate(mLayoutID, null);
        activityContainer.addView(vi);
        return activityContainer;
    }

    public static View getViewFromLayout(Activity mActivity, FrameLayout activityContainer, int mLayoutID) {
        activityContainer.removeAllViews();
        View vi = mActivity.getLayoutInflater().inflate(mLayoutID, null);
        activityContainer.addView(vi);
        return activityContainer;
    }

    public static View getViewFromLayout(Activity mActivity, int mLayoutID) {
        return mActivity.getLayoutInflater().inflate(mLayoutID, null, false);
    }

    public static void getSmoothAnimation(Activity activity) {
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public static PopupWindow getPopup(Activity mInstance, PopupWindow popupWindow, View contentView, View anchorView) {
        if (popupWindow == null) {
            popupWindow = new PopupWindow(mInstance);
        }
        popupWindow.setFocusable(true);
        popupWindow.setContentView(contentView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(5F);
        }
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.showAsDropDown(anchorView); // where u want show on view click event popupwindow.showAsDropDown(view, x, y);
        return popupWindow;
    }
}
