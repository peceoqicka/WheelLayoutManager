package com.peceoqicka.demo.wheelayoutmanager.index

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.support.v7.widget.RecyclerView
import com.peceoqicka.demo.wheelayoutmanager.BR
import com.peceoqicka.wheellayoutmanager.WheelLayoutManager

/**
 * <pre>
 *      author  :   peceoqicka
 *      time    :   2019/3/11
 *      version :   1.0
 *      desc    :
 * </pre>
 */
class ViewModel : BaseObservable() {
    lateinit var yearLayoutManager: WheelLayoutManager
    lateinit var monthLayoutManager: WheelLayoutManager
    lateinit var dayLayoutManager: WheelLayoutManager
    lateinit var yearItemDecoration: RecyclerView.ItemDecoration
    lateinit var monthItemDecoration: RecyclerView.ItemDecoration
    lateinit var dayItemDecoration: RecyclerView.ItemDecoration

    var yearAdapter: ItemAdapter? = null
        set(value) {
            field = value;notifyPropertyChanged(BR.yearAdapter)
        }
        @Bindable
        get
    var monthAdapter: ItemAdapter? = null
        set(value) {
            field = value;notifyPropertyChanged(BR.monthAdapter)
        }
        @Bindable
        get
    var dayAdapter: ItemAdapter? = null
        set(value) {
            field = value;notifyPropertyChanged(BR.dayAdapter)
        }
        @Bindable
        get

    var yearDisplay: Int = 0
        set(value) {
            field = value;notifyPropertyChanged(BR.yearDisplay)
        }
        @Bindable
        get
    var monthDisplay: Int = 0
        set(value) {
            field = value;notifyPropertyChanged(BR.monthDisplay)
        }
        @Bindable
        get
    var dayDisplay: Int = 0
        set(value) {
            field = value;notifyPropertyChanged(BR.dayDisplay)
        }
        @Bindable
        get
}