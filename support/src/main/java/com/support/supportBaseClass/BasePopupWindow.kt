package com.support.supportBaseClass

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.support.inline.orElse
import org.jetbrains.anko.layoutInflater

abstract class BasePopupWindow<B : ViewBinding>(
    context: Context,
    bindingFactory: (LayoutInflater) -> B
) : PopupWindow() {
    private var _binding: ViewBinding = bindingFactory.invoke(context.layoutInflater)
    protected val binding: B by lazy { _binding as B }

    init {
        super.setContentView(binding.root)
        super.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT)
        super.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT)

        onCreate()
    }

    open fun onCreate() {
        isOutsideTouchable = true
        isFocusable = false
    }

    override fun setFocusable(focusable: Boolean) {
        super.setFocusable(false)
    }

    override fun setOutsideTouchable(touchable: Boolean) {
        super.setOutsideTouchable(true)
    }

    override fun setBackgroundDrawable(background: Drawable?) {
        super.setBackgroundDrawable(BitmapDrawable())
    }

    override fun isFocusable(): Boolean {
        return false
    }

    open fun show() {
        showAsDropDown(getAnchorView())
    }

    abstract fun getAnchorView():View
}