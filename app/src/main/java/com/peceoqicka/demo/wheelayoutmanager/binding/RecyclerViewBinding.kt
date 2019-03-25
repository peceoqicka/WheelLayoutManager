package com.peceoqicka.demo.wheelayoutmanager.binding

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView

/**
 * <pre>
 *      author  :   peceoqicka
 *      time    :   2019/3/11
 *      version :   1.0
 *      desc    :
 * </pre>
 */
@BindingAdapter("app:adapter")
fun setRecyclerViewAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>?) {
    if (adapter != null) {
        recyclerView.adapter = adapter
    }
}

@BindingAdapter("android:layoutManager")
fun setRecyclerViewLayoutManager(recyclerView: RecyclerView, layoutManager: RecyclerView.LayoutManager) {
    recyclerView.layoutManager = layoutManager
}

@BindingAdapter("app:itemDecoration")
fun addItemDecoration(recyclerView: RecyclerView, itemDecoration: RecyclerView.ItemDecoration?) {
    if (itemDecoration != null) {
        recyclerView.addItemDecoration(itemDecoration)
    }
}