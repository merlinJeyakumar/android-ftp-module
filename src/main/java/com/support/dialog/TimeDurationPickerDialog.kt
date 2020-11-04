package com.support.dialog

import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.support.R
import com.support.widgets.duration_picker.TimeDurationPicker
import org.jetbrains.anko.toast

class TimeDurationPickerDialog(
    private val activity: FragmentActivity,
    private val timeDurationPickerListener: TimeDurationPickerListener? = null
) : AppCompatDialog(activity) {

    fun showDialog(): TimeDurationPickerDialog {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.setContentView(R.layout.d_time_picker)
        this.show()
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val timeDurationPicker = findViewById<TimeDurationPicker>(R.id.timeDurationPicker)!!

        findViewById<MaterialButton>(R.id.mbtnNegative)?.setOnClickListener {
            timeDurationPickerListener?.onResult(false, null, 0)
            dismiss()
        }
        findViewById<MaterialButton>(R.id.mbtnPositive)?.setOnClickListener {
            if (timeDurationPicker.duration == 0L) {
                activity.toast("enter duration")
            } else {
                timeDurationPickerListener?.onResult(
                    true,
                    timeDurationPicker,
                    timeDurationPicker.duration
                )
                dismiss()
            }
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

abstract class TimeDurationPickerListener {
    abstract fun onResult(
        isPositive: Boolean,
        timeDurationPicker: TimeDurationPicker?,
        duration: Long
    )
}