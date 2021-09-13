package com.support.baseApp.mvvm

import androidx.recyclerview.widget.RecyclerView
import com.support.utills.Log
import com.support.widgets.BaseViewHolder
import java.util.ArrayList

abstract class BaseAdapter<ITEM_TYPE,SELECTION_TYPE> :
    RecyclerView.Adapter<BaseViewHolder<ITEM_TYPE,SELECTION_TYPE>>() {
    private val TAG: String = "BaseAdapter"
    private val selectionList: MutableList<SELECTION_TYPE> = ArrayList()

    companion object {
        const val PAYLOAD_SELECTION_MODE: String = "is_selection_mode"
    }

    fun setSelected(key: SELECTION_TYPE) {
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

    fun clearSelection(updateAll: Boolean = false) {
        if (updateAll) {
            for (topicId in selectionList) {
                val itemId = getIndex(topicId)
                if (itemId != -1) {
                    notifyItemChanged(itemId, PAYLOAD_SELECTION_MODE)
                } else {
                    Log.e(TAG, "error: item not found $topicId")
                }
            }
        }else{
            notifyDataSetChanged()
        }
        selectionList.clear()
    }

    fun selectAll() {
        val allKeys = getAllKeys()
        selectionList.clear()
        selectionList.addAll(allKeys)
        notifyItemRangeChanged(0, allKeys.size, PAYLOAD_SELECTION_MODE)
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

    abstract fun getList():List<ITEM_TYPE>

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