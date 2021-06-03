package com.support.supportBaseClass

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog

abstract class MBaseDialog(
    activity: Activity
) : AlertDialog(activity) {

    abstract fun getView(): View
    abstract fun prepareUi(view:View)

    override fun onCreate(savedInstanceState: Bundle?) {
        val view = getView()
        setView(getView())
        super.onCreate(savedInstanceState)

        prepareUi(view)
    }
}