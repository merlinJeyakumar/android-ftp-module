package com.support.widgets

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.jetbrains.anko.toast

abstract class BaseViewHolder<M>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    fun toast(resId: Int) {
        itemView.context.toast(resId)
    }

    fun toast(text: String) {
        itemView.context.toast(text)
    }

    abstract fun bind(position: Int, item: M)
}