package com.support.dialog

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.support.R

class NumberPicker(
    private val activity: FragmentActivity,
    private val pickerListener: NumberPickerListener? = null,
    private val defaultValue: Int = 1
) : AppCompatDialog(activity) {

    fun showDialog(): NumberPicker {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(R.layout.d_number_picker)
        this.show()
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val numberPicker =
            findViewById<com.shawnlin.numberpicker.NumberPicker>(R.id.number_picker)!!

        numberPicker.minValue = 1
        numberPicker.maxValue = 6
        numberPicker.value = defaultValue

        findViewById<MaterialButton>(R.id.mbtnNegative)?.setOnClickListener {
            pickerListener?.onResult(false, 0)
            dismiss()
        }
        findViewById<MaterialButton>(R.id.mbtnPositive)?.setOnClickListener {
            pickerListener?.onResult(
                true,
                numberPicker.value
            )
            dismiss()
        }
    }

    override fun show() {
        if (!activity.isFinishing) {
            super.show()
        }
    }

    override fun dismiss() {
        if (!activity.isFinishing) {
            super.dismiss()
        }
    }

    override fun hide() {
        if (!activity.isFinishing) {
            super.hide()
        }
    }
}

abstract class NumberPickerListener {
    abstract fun onResult(
        isPositive: Boolean,
        count: Int
    )
}