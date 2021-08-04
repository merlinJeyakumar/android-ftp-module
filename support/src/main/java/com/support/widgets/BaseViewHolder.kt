package com.support.widgets

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.support.utills.Log
import org.jetbrains.anko.toast
import java.io.File

abstract class BaseViewHolder<M>(
    private val selectedList: List<String>,
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    fun toast(resId: Int) {
        itemView.context.toast(resId)
    }

    fun toast(text: String) {
        itemView.context.toast(text)
    }

    abstract fun bind(position: Int, item: M)

    open fun bind(
        position: Int,
        item: M,
        payload: List<Any>
    ) {
    }

    fun getSelectionList(): List<String> {
        return selectedList
    }

    fun isSelected(text: String): Boolean {
        return getSelectionList().contains(text)
    }
}