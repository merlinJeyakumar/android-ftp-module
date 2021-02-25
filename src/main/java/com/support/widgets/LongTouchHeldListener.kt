package com.support.widgets

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

open class LongTouchHeldListener(
    private val LONG_PRESS_TIME: Int = 300,
    private val LONG_SELECTION_PRESS_TIME: Int = 700
) : View.OnTouchListener {
    private val TAG: String = "LongTouchHeldListener"
    private var disposable: Disposable? = null
    private var isLongPressed: Boolean = false
    private var countDownInt: Int = 0
    private var gestureDetector = GestureDetector(SingleTapConfirm())


    var countDown = Single.create<Int> {
        countDownInt++
        it.onSuccess(countDownInt)
    }
        .delay(1, TimeUnit.MILLISECONDS)
        .repeat()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                disposable = countDown.subscribe {
                    when (it) {
                        LONG_PRESS_TIME -> {
                            Log.e(TAG, "longPressed")
                            onLongTouch()
                        }
                        LONG_SELECTION_PRESS_TIME -> {
                            Log.e(TAG, "itemSelected")
                            onSelectionLongTouch()
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                disposable?.dispose()
                countDownInt = 0
            }
            MotionEvent.ACTION_CANCEL -> {
                disposable?.dispose()
                countDownInt = 0
            }
        }
        return true
    }

    inner class SingleTapConfirm : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent?): Boolean {
            onTouch()
            return true
        }
    }

    open fun onSelectionLongTouch() {}
    open fun onLongTouch() {}
    open fun onTouch() {}
}