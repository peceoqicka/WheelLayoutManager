package com.peceoqicka.demo.wheelayoutmanager.index

import android.databinding.BaseObservable
import android.databinding.Bindable
import com.peceoqicka.demo.wheelayoutmanager.BR

/**
 * <pre>
 *      author  :   peceoqicka
 *      time    :   2019/3/11
 *      version :   1.0
 *      desc    :
 * </pre>
 */
class ItemViewModel : BaseObservable() {
    var value: Int = 0
        set(value) {
            field = value;notifyPropertyChanged(BR.value)
        }
        @Bindable
        get
    var isSelected: Boolean = false
        set(value) {
            field = value;notifyPropertyChanged(BR.selected)
        }
        @Bindable
        get

    override fun toString(): String {
        return "[value : $value]"
    }
}