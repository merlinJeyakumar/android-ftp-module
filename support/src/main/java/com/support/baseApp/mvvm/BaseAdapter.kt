package com.support.baseApp.mvvm

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.support.utills.Log
import com.support.widgets.BaseViewHolder
import java.util.*

abstract class BaseAdapter<ITEM_TYPE, SELECTION_TYPE> :
    RecyclerView.Adapter<BaseViewHolder<ITEM_TYPE, SELECTION_TYPE>>() {
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

    fun clearSelection(key: SELECTION_TYPE) {
        if (!selectionList.contains(key)) {
            Log.e(TAG, "error: item already cleared")
            return
        }
        selectionList.remove(key)
        notifyItemChanged(
            getIndex(key),
            PAYLOAD_SELECTION_MODE
        )
    }

    fun clearSelection(notifyAll: Boolean = false) {
        if (!notifyAll) {
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

    fun isSelected(key: SELECTION_TYPE): Boolean {
        return selectionList.contains(key)
    }

    fun isSelectionMode(): Boolean {
        return selectionList.isNotEmpty()
    }

    fun getSelections(): MutableList<SELECTION_TYPE> {
        return selectionList
    }

    fun getList(): List<ITEM_TYPE>{
        return asyncListDiffer.currentList
    }

    override fun getItemCount(): Int = fileList.size

    open fun getIndex(itemKey: SELECTION_TYPE): Int {
        return -1
    }

    open fun getAllKeys(): List<SELECTION_TYPE> {
        return listOf()
    }

    open fun isSelectable(key: SELECTION_TYPE): Boolean {
        return true
    }

    private val asyncDiffCallback: DiffUtil.ItemCallback<ITEM_TYPE>
        get() = object : DiffUtil.ItemCallback<ITEM_TYPE>() {
            override fun areItemsTheSame(
                oldItem: ITEM_TYPE,
                newItem: ITEM_TYPE
            ): Boolean {
                return isSameItem(oldItem, newItem)
            }

            override fun areContentsTheSame(
                oldItem: ITEM_TYPE,
                newItem: ITEM_TYPE
            ): Boolean {
                return isSameContent(oldItem, newItem)
            }
        }

    private var asyncListDiffer: AsyncListDiffer<ITEM_TYPE> =
        AsyncListDiffer(this, asyncDiffCallback)
    open val fileList: List<ITEM_TYPE> get() = asyncListDiffer.currentList

    open fun submitList(list: List<ITEM_TYPE>) {
        asyncListDiffer.submitList(list)
    }

    abstract fun isSameItem(oldItem: ITEM_TYPE, newItem: ITEM_TYPE):Boolean
    abstract fun isSameContent(oldItem: ITEM_TYPE, newItem: ITEM_TYPE): Boolean
}