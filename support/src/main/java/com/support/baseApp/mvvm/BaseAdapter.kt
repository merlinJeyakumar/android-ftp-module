package com.support.baseApp.mvvm

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.support.utills.Log
import com.support.widgets.BaseViewHolder
import java.util.*

abstract class BaseAdapter<ITEM_TYPE, SELECTION_TYPE>(diffCallback: DiffUtil.ItemCallback<ITEM_TYPE>) :
    ListAdapter<ITEM_TYPE, BaseViewHolder<ITEM_TYPE, SELECTION_TYPE>>(diffCallback) {


    private val TAG: String = "BaseAdapter"
    private val selectionList: MutableList<SELECTION_TYPE> = ArrayList()

    companion object {
        const val PAYLOAD_SELECTION_MODE: String = "is_selection_mode"
    }

    fun setSelected(key: SELECTION_TYPE) { //todo: selecting heppen for unselectable topic
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

    fun clearSelection(topic: SELECTION_TYPE) {
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

    fun clearSelection(notifyAll: Boolean = false) {
        if (notifyAll) {
            for (topicId in selectionList) {
                val itemId = getIndex(topicId)
                if (itemId != -1) {
                    notifyItemChanged(itemId, PAYLOAD_SELECTION_MODE)
                } else {
                    Log.e(TAG, "error: item not found $topicId")
                }
            }
        } else {
            notifyDataSetChanged()
        }
        selectionList.clear()
    }

    fun selectAll(notifyAll: Boolean = true) {
        val selectableKeys = mutableListOf<SELECTION_TYPE>()
        for (key in getAllKeys()) {
            if (isSelectable(key)) {
                selectableKeys.add(key)
            }
        }
        selectionList.clear()
        selectionList.addAll(selectableKeys)
        if (notifyAll) {
            notifyDataSetChanged()
        } else {
            for (selectableKey in selectableKeys) {
                val itemId = getIndex(selectableKey)
                if (itemId != -1) {
                    notifyItemChanged(itemId, PAYLOAD_SELECTION_MODE)
                }
            }
        }

    }

    fun isSelected(topic: SELECTION_TYPE): Boolean {
        return selectionList.contains(topic)
    }

    fun isSelectionMode(): Boolean {
        return selectionList.isNotEmpty()
    }

    fun getSelections(): MutableList<SELECTION_TYPE> {
        return selectionList
    }

    abstract fun getList(): List<ITEM_TYPE>

    open fun getIndex(itemKey: SELECTION_TYPE): Int {
        return -1
    }

    open fun getAllKeys(): List<SELECTION_TYPE> {
        return listOf()
    }

    open fun isSelectable(key: SELECTION_TYPE): Boolean {
        return true
    }
}