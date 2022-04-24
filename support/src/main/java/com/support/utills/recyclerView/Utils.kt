package com.support.utills.recyclerView

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

object Utils {
    fun Context.getSmoothScroll(): LinearSmoothScroller {
        return object : LinearSmoothScroller(this) {
            override fun getVerticalSnapPreference(): Int {
                return LinearSmoothScroller.SNAP_TO_START
            }
        }
    }

    fun RecyclerView.scrollToPositionSmooth(int: Int) {
        this.layoutManager?.startSmoothScroll(this.context.getSmoothScroll().apply {
            targetPosition = int
        })
    }

    fun RecyclerView.scrollToPositionFast(position: Int) {
        this.layoutManager?.scrollToPosition(position)
    }
}