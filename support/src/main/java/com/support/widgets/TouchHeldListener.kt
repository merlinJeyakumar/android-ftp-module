package com.support.widgets

import android.view.MotionEvent
import android.view.View

open class TouchHeldListener : View.OnTouchListener {
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP || event?.action == MotionEvent.ACTION_CANCEL) {
            onTouchCancelled()
        } else {
            onTouch()
        }
        return false
    }

    open fun onTouch() {}

    open fun onTouchCancelled() {}
}