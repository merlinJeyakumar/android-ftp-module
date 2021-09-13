package com.support.widgets

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.support.utills.Log
import org.jetbrains.anko.toast
import java.io.File

abstract class BaseViewHolder<M,SELECTION_TYPE>(
    private val selectedList: List<SELECTION_TYPE>,
    itemView: View
) : RecyclerView.ViewHolder(itemView) {

    val context: Context get() = itemView.context

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

    fun getSelectionList(): List<SELECTION_TYPE> {
        return selectedList
    }

    fun isSelected(text: SELECTION_TYPE): Boolean {
        return getSelectionList().contains(text)
    }

    fun isSelectionMode(): Boolean {
        return getSelectionList().isNotEmpty()
    }
}