package com.support.supportBaseClass

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.support.inline.orElse
import org.jetbrains.anko.layoutInflater

abstract class BaseMaterialDialog<B : ViewBinding>(
    context: Context,
    private val bindingFactory: (LayoutInflater) -> B
) : MaterialAlertDialogBuilder(context) {

    private var alertDialog: AlertDialog? = null
    private var _binding: ViewBinding = bindingFactory.invoke(context.layoutInflater)
    protected val binding: B by lazy { _binding as B }

    override fun create(): AlertDialog {
        setView(binding.root)
        return super.create()
    }

    override fun setView(view: View?): MaterialAlertDialogBuilder {
        super.setView(binding.root).let {
            return it.also {
                onCreate(binding)
            }
        }
    }

    override fun show(): AlertDialog {
        return super.show().also {
            this.alertDialog = it
        }
    }

    fun dismiss() {
        this.alertDialog?.dismiss()
    }

    open fun isShowing(): Boolean {
        return alertDialog?.isShowing.orElse { false }
    }

    fun setCancellable(boolean: Boolean): AlertDialog {
        super.setCancelable(boolean)
        return this.alertDialog!!
    }

    abstract fun onCreate(binding: B)
}