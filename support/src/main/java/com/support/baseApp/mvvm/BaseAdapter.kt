package com.support.baseApp.mvvm

import androidx.recyclerview.widget.RecyclerView
import com.support.utills.Log
import com.support.widgets.BaseViewHolder
import java.util.ArrayList

abstract class BaseAdapter<ITEM_TYPE>() :
    RecyclerView.Adapter<BaseViewHolder<ITEM_TYPE>>() {
    private val TAG: String = "BaseAdapter"
    private val selectionList: MutableList<String> = ArrayList()

    companion object {
        const val PAYLOAD_SELECTION_MODE: String = "is_selection_mode"
    }

    fun setSelected(key: String) {
        if (isSelectable(key)) {
            if (selectionList.contains(key)) {
                Log.e(TAG, "error: item already selected")
                return
            }
            selectionList.add(key)
            notifyItemChanged(
                getIndex(key),
                PAYLOAD_SELECTION_MODE
            )
        }
    }

    fun clearSelection(topic: String) {
        if (!selectionList.contains(topic)) {
            Log.e(TAG, "error: item already cleared")
            return
        }
        selectionList.remove(topic)
        notifyItemChanged(
            getIndex(topic),
            PAYLOAD_SELECTION_MODE
        )
    }

    fun clearSelection() {
        for (topicId in selectionList) {
            val itemId = getIndex(topicId)
            if (itemId != -1) {
                notifyItemChanged(itemId, PAYLOAD_SELECTION_MODE)
            } else {
                Log.e(TAG, "error: item not found $topicId")
            }
        }
        selectionList.clear()
    }

    fun selectAll() {
        val allKeys = getAllKeys()
        selectionList.clear()
        selectionList.addAll(allKeys)
        notifyItemRangeChanged(0, allKeys.size, PAYLOAD_SELECTION_MODE)
    }

    fun isSelected(topic: String): Boolean {
        return selectionList.contains(topic)
    }

    fun isSelectionMode(): Boolean {
        return selectionList.isNotEmpty()
    }

    fun getSelections(): MutableList<String> {
        return selectionList
    }

    open fun getIndex(itemKey: String): Int {
        return -1
    }

    open fun getAllKeys(): List<String> {
        return listOf()
    }

    open fun isSelectable(key: String): Boolean {
        return true
    }
}