package com.peceoqicka.demo.wheelayoutmanager

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.peceoqicka.demo.wheelayoutmanager.databinding.ActivityMainBinding
import com.peceoqicka.demo.wheelayoutmanager.index.ItemAdapter
import com.peceoqicka.demo.wheelayoutmanager.index.ItemViewModel
import com.peceoqicka.demo.wheelayoutmanager.index.ViewModel
import com.peceoqicka.demo.wheelayoutmanager.util.color
import com.peceoqicka.demo.wheelayoutmanager.util.timer
import com.peceoqicka.wheellayoutmanager.WheelLayoutManager
import org.jetbrains.anko.dimen
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var bindModel: ViewModel
    private var currentYear: Int = 0
    private var currentMonth: Int = 0
    private var currentDay: Int = 0
    private var selectedMaxDayOfMonth: Int = 0
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this, R.layout.activity_main
        )
        binding.model = ViewModel().apply {
            yearLayoutManager = WheelLayoutManager(5).apply {
                selectionChangedListener = this@MainActivity::onYearSelectionChanged
                draggingStartListener = {
                    deselectIndex(yearAdapter)
                }
            }
            monthLayoutManager = WheelLayoutManager(5).apply {
                selectionChangedListener = this@MainActivity::onMonthSelectionChanged
                draggingStartListener = {
                    deselectIndex(monthAdapter)
                }
            }
            dayLayoutManager = WheelLayoutManager(5).apply {
                selectionChangedListener = this@MainActivity::onDaySelectionChanged
                draggingStartListener = {
                    deselectIndex(dayAdapter)
                }
            }

            yearItemDecoration = DateItemDecoration(
                color(R.color.hex_ffffff),
                dimen(R.dimen.px_1),
                color(R.color.hex_c6c6c6),
                enableHighlightMarker = true,
                highlightMarkerWidth = dimen(R.dimen.px_6),
                highlightMarkerColor = color(R.color.hex_db262e),
                enableHintText = true,
                hintText = getString(R.string.year),
                hintTextRightMargin = dimen(R.dimen.px_60),
                hintTextSize = dimen(R.dimen.px_30),
                hintTextColor = color(R.color.hex_db262e)
            )

            monthItemDecoration = DateItemDecoration(
                color(R.color.hex_ffffff),
                dimen(R.dimen.px_1),
                color(R.color.hex_c6c6c6),
                enableHintText = true,
                hintText = getString(R.string.month),
                hintTextRightMargin = dimen(R.dimen.px_92),
                hintTextSize = dimen(R.dimen.px_30),
                hintTextColor = color(R.color.hex_db262e)
            )

            dayItemDecoration = DateItemDecoration(
                color(R.color.hex_ffffff),
                dimen(R.dimen.px_1),
                color(R.color.hex_c6c6c6),
                enableHintText = true,
                hintText = getString(R.string.day),
                hintTextRightMargin = dimen(R.dimen.px_92),
                hintTextSize = dimen(R.dimen.px_30),
                hintTextColor = color(R.color.hex_db262e)
            )

            bindModel = this
        }
        getDateOfToday()
        loadData()
        selectCurrentDate()
    }

    private fun onYearSelectionChanged(position: Int) {
        val selectedValue = bindModel.yearAdapter?.getValue(position) ?: -1
        if (selectedValue > 0) {
            selectedYear = selectedValue
            onDayChanged()
            selectIndex(bindModel.yearAdapter, position)
        }
    }

    private fun onMonthSelectionChanged(position: Int) {
        val selectedValue = bindModel.monthAdapter?.getValue(position) ?: -1
        if (selectedValue > 0) {
            selectedMonth = selectedValue
            onDayChanged()
            selectIndex(bindModel.monthAdapter, position)
        }
    }

    private fun onDaySelectionChanged(position: Int) {
        val selectedValue = bindModel.dayAdapter?.getValue(position) ?: -1
        if (selectedValue > 0) {
            selectedDay = selectedValue
            selectIndex(bindModel.dayAdapter, position)
        }
    }

    /**
     * 因为年或月变化可能产生的当月最大天数变化
     */
    private fun onDayChanged() {
        val newMaxDayOfMonth = getMaxDayOfMonth(selectedYear, selectedMonth)
        if (newMaxDayOfMonth > selectedMaxDayOfMonth) {
            bindModel.dayAdapter?.addRange(selectedMaxDayOfMonth + 1, newMaxDayOfMonth)
        } else if (newMaxDayOfMonth < selectedMaxDayOfMonth) {
            val lastMaxDayOfMonth = selectedMaxDayOfMonth

            if (selectedDay > newMaxDayOfMonth) {
                selectValue(bindModel.dayLayoutManager, bindModel.dayAdapter, newMaxDayOfMonth)
            }
            bindModel.dayAdapter?.removeRange(newMaxDayOfMonth + 1, lastMaxDayOfMonth)
        }
        selectedMaxDayOfMonth = newMaxDayOfMonth
    }

    private fun getDateOfToday() {
        val calendar = Calendar.getInstance()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1
        currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        println("$currentYear-$currentMonth-$currentDay")
    }

    private fun getMaxDayOfMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun loadData() {
        val yearList = ObservableArrayList<ItemViewModel>()
        for (i in (0 until 10)) {
            val year = currentYear + i
            yearList.add(ItemViewModel().apply {
                this.value = year
            })
        }
        bindModel.yearAdapter = ItemAdapter(yearList)
        val monthList = ObservableArrayList<ItemViewModel>()
        for (i in (1..12)) {
            monthList.add(ItemViewModel().apply {
                this.value = i
            })
        }
        bindModel.monthAdapter = ItemAdapter(monthList)
        val dayList = ObservableArrayList<ItemViewModel>()
        for (i in (1..getMaxDayOfMonth(currentYear, currentMonth))) {
            dayList.add(ItemViewModel().apply {
                this.value = i
            })
        }
        bindModel.dayAdapter = ItemAdapter(dayList)
    }

    private fun selectDate(year: Int, month: Int, day: Int) {
        selectValue(bindModel.yearLayoutManager, bindModel.yearAdapter, year)
        selectValue(bindModel.monthLayoutManager, bindModel.monthAdapter, month)
        selectValue(bindModel.dayLayoutManager, bindModel.dayAdapter, day)
    }

    private fun selectValue(layoutManager: WheelLayoutManager, adapter: ItemAdapter?, value: Int) {
        if (adapter != null) {
            val targetPosition = adapter.findPositionOf(value)
            layoutManager.scrollToPosition(targetPosition)
        }
    }

    private fun selectCurrentDate() {
        timer(100) {
            selectedMaxDayOfMonth = getMaxDayOfMonth(selectedYear, selectedMonth)
            selectDate(currentYear, currentMonth, currentDay)
        }
    }

    private fun selectIndex(adapter: ItemAdapter?, index: Int) {
        adapter?.selectItem(index)
    }

    private fun deselectIndex(adapter: ItemAdapter?) {
        adapter?.deselectItem()
    }
}
