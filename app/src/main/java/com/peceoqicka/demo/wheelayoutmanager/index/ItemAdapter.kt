package com.peceoqicka.demo.wheelayoutmanager.index

import android.databinding.ObservableArrayList
import com.peceoqicka.demo.wheelayoutmanager.R
import com.peceoqicka.demo.wheelayoutmanager.adapter.ObservableRecyclerViewAdapter
import com.peceoqicka.demo.wheelayoutmanager.databinding.ItemIndexYearBinding

/**
 * <pre>
 *      author  :   peceoqicka
 *      time    :   2019/3/11
 *      version :   1.0
 *      desc    :
 * </pre>
 */
class ItemAdapter(data: ObservableArrayList<ItemViewModel>) :
    ObservableRecyclerViewAdapter<ItemViewModel, ItemIndexYearBinding>(data) {
    private var lastSelected: Int = -1
    override fun getLayoutId(): Int = R.layout.item_index_year

    override fun onSetData(binding: ItemIndexYearBinding, data: ItemViewModel) {
        binding.model = data
    }

    fun selectItem(position: Int) {
        if (position in (0 until dataList.size)) {
            if (lastSelected >= 0 && lastSelected in (0 until dataList.size)) {
                dataList[lastSelected].isSelected = false
            }
            dataList[position].isSelected = true
            lastSelected = position
        }
    }

    fun deselectItem() {
        if (lastSelected in (0 until dataList.size)) {
            dataList[lastSelected].isSelected = false
            lastSelected = -1
        }
    }

    fun getValue(position: Int): Int {
        return if (position in (0 until dataList.size)) dataList[position].value else -1
    }

    fun findPositionOf(value: Int): Int {
        val model = dataList.find { it.value == value }
        return if (model != null) dataList.indexOf(model) else -1
    }

    fun addRange(startValue: Int, endValue: Int) {
        if (startValue > endValue) {
            return
        }

        val additionList = ObservableArrayList<ItemViewModel>()
        for (i in (startValue..endValue)) {
            additionList.add(ItemViewModel().apply {
                value = i
            })
        }
        moreItem(additionList)
    }

    fun removeRange(startValue: Int, endValue: Int) {
        if (startValue > endValue) {
            return
        }

        val startItem = dataList.find { it.value == startValue }
        val endItem = dataList.find { it.value == endValue }
        if (startItem != null && endItem != null) {
            val startIndex = dataList.indexOf(startItem)
            val endIndex = dataList.indexOf(endItem)
            dataList.subList(startIndex, endIndex + 1).clear()
        }
    }
}