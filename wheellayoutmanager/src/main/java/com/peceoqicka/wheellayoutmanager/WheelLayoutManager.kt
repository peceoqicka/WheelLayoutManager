package com.peceoqicka.wheellayoutmanager

import android.animation.Animator
import android.animation.ValueAnimator
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * <pre>
 *      author  :   peceoqicka
 *      time    :   2019/3/11
 *      version :   1.0
 *      desc    :
 * </pre>
 */
class WheelLayoutManager(private val visibleCount: Int) : RecyclerView.LayoutManager() {
    //通过visibleCount计算求得的一屏能显示的item高度
    private var requiredItemHeight: Int = 0
    //使第一个item垂直于整个控件居中对齐的上边距
    private var requiredMarginTop: Int = 0
    private var scrollOffsetY: Int = 0
    private var canScrollVertically: Boolean = false
    private lateinit var scrollValueAnimator: ValueAnimator

    private var selectedPosition: Int = 0
        set(value) {
            field = value;selectionChangedListener?.invoke(field)
        }
    var selectionChangedListener: ((Int) -> Unit)? = null
    var draggingStartListener: (() -> Unit)? = null
    val selection: Int
        get() = selectedPosition
    /**
     * 居中的item的上边距
     */
    val selectionTop: Int
        get() = requiredMarginTop
    val itemHeight: Int
        get() = requiredItemHeight

    init {
        if (visibleCount % 2 != 1 || visibleCount < 1) {
            throw IllegalArgumentException("visible count can only be set to odd number")
        }
    }

    override fun isAutoMeasureEnabled(): Boolean {
        //不使用自动测量，不管是否使用自动测量，最终一样会执行LayoutManager的onMeasure方法
        return false
    }

    override fun onMeasure(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        widthSpec: Int,
        heightSpec: Int
    ) {
        val heightMode = View.MeasureSpec.getMode(heightSpec)
        val heightSize = View.MeasureSpec.getSize(heightSpec)

        //将模式的强制设置为确定模式
        var calibratedHeightMode = heightMode
        if (heightMode == View.MeasureSpec.UNSPECIFIED || heightMode == View.MeasureSpec.AT_MOST) {
            calibratedHeightMode = View.MeasureSpec.EXACTLY
        }
        val calibratedHeightSpec =
            View.MeasureSpec.makeMeasureSpec(heightSize, calibratedHeightMode)

        super.onMeasure(recycler, state, widthSpec, calibratedHeightSpec)
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun canScrollVertically(): Boolean {
        return canScrollVertically
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        //<0 :向下滚动，即手指拖动页面往上滑  >0 :向上滚动，与前者相反
        //println("scrollVerticallyBy : $dy")
        if (state.itemCount == 0) {
            return 0
        }
        detachAndScrapAttachedViews(recycler)
        val lastOffset = scrollOffsetY
        updateScrollOffsetY(dy, lastOffset, state.itemCount)
        //重新布局
        layoutChildren(recycler, state)
        return if (lastOffset == scrollOffsetY) 0 else dy
    }

    private fun updateScrollOffsetY(dy: Int, lastOffsetY: Int, itemCount: Int) {
        scrollOffsetY += dy
        val childrenHeight = itemCount * requiredItemHeight
        val overflowHeight = childrenHeight - height
        if (scrollOffsetY < 0) {
            scrollOffsetY = 0
        } else if (scrollOffsetY > overflowHeight + requiredMarginTop * 2) {
            scrollOffsetY = if (overflowHeight > 0) overflowHeight + requiredMarginTop * 2 else lastOffsetY
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        requiredItemHeight = height / visibleCount
        requiredMarginTop = requiredItemHeight * 2

        if (state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        //暂时分离和回收全部有效的Item
        detachAndScrapAttachedViews(recycler)

        if (state.itemCount > ((visibleCount + 1) / 2)) {
            canScrollVertically = true
        }
        layoutChildren(recycler, state)
    }

    private fun layoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        for (i in (0 until state.itemCount)) {
            val childTop = i * requiredItemHeight + requiredMarginTop - scrollOffsetY
            val child = recycler.getViewForPosition(i)
            addView(child)
            if (child.layoutParams.height <= 0) {
                val calibratedLayoutParams = child.layoutParams
                calibratedLayoutParams.height = requiredItemHeight
                child.layoutParams = calibratedLayoutParams
            }
            measureChild(child, 0, 0)
            val decoratedWidth = getDecoratedMeasuredWidth(child)
            val childBottom = childTop + getDecoratedMeasuredHeight(child)

            if ((childTop < 0 && childBottom <= 0) || (childTop >= height && childBottom > height)) {
                removeAndRecycleView(child, recycler)
            } else {
                layoutDecorated(child, 0, childTop, decoratedWidth, childBottom)
            }
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        when (state) {
            RecyclerView.SCROLL_STATE_DRAGGING -> {
                draggingStartListener?.invoke()
                stopScrollAnimation()
            }
            RecyclerView.SCROLL_STATE_IDLE -> {
                startScrollAnimation(findClosestItemPosition(), itemCount)
            }
        }
    }

    private fun findClosestItemPosition(): Int {
        var estimatedPosition = -1
        var minDistance = Int.MAX_VALUE
        for (i in (0 until itemCount)) {
            val distance = Math.abs(getRequiredScrollOffset(i) - scrollOffsetY)
            if (distance < minDistance) {
                minDistance = distance
                estimatedPosition = i
            }
        }
        return estimatedPosition
    }

    /**
     * 获取指定position的Item选中时对应的ScrollOffset值
     */
    private fun getRequiredScrollOffset(targetPosition: Int): Int {
        return targetPosition * requiredItemHeight
    }

    private fun startScrollAnimation(position: Int, itemCount: Int) {
        if (position < 0) {
            return
        }
        stopScrollAnimation()
        val scrollDistance = (getRequiredScrollOffset(position) - scrollOffsetY)
        //既然定位都是Int，那动画值的变化也要用Int，用Float就会出现滑动停止后偏离几个像素的尴尬场面
        scrollValueAnimator = ValueAnimator.ofInt(0, scrollDistance).setDuration(300)
        scrollValueAnimator.addUpdateListener(ScrollAnimatorUpdateListener { deltaValue ->
            updateScrollOffsetY(deltaValue, scrollOffsetY, itemCount)
            requestLayout()
        })
        scrollValueAnimator.addListener(object : AnimatorListenerProxy() {
            override fun onAnimationEnd(animation: Animator) {
                selectedPosition = position
            }
        })
        scrollValueAnimator.start()
    }

    private fun stopScrollAnimation() {
        if (this::scrollValueAnimator.isInitialized) {
            scrollValueAnimator.pause()
        }
    }

    override fun scrollToPosition(position: Int) {
        if (position in 0 until itemCount) {
            val distance = getRequiredScrollOffset(position) - scrollOffsetY
            updateScrollOffsetY(distance, scrollOffsetY, itemCount)
            requestLayout()
            selectedPosition = position
        }
    }

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State, position: Int) {
        if (position in 0 until itemCount) {
            startScrollAnimation(position, state.itemCount)
        }
    }

    private class ScrollAnimatorUpdateListener(val valueUpdated: (Int) -> Unit) :
        ValueAnimator.AnimatorUpdateListener {
        private var lastValue: Int = 0
        override fun onAnimationUpdate(animation: ValueAnimator) {
            val currentValue = animation.animatedValue as Int
            if (currentValue != 0) {
                valueUpdated(currentValue - lastValue)
            }
            lastValue = currentValue
        }
    }

    private abstract class AnimatorListenerProxy : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {
        }

        override fun onAnimationEnd(animation: Animator) {
        }

        override fun onAnimationRepeat(animation: Animator) {
        }

        override fun onAnimationCancel(animation: Animator) {
        }
    }
}