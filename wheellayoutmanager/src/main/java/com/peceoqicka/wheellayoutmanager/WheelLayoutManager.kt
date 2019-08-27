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
class WheelLayoutManager(private val visibleCount: Int, private val infinity: Boolean = false) :
    RecyclerView.LayoutManager() {
    //通过visibleCount计算求得的一屏能显示的item高度
    private var requiredItemHeight: Int = 0
    //使第一个item垂直于整个控件居中对齐的上边距
    private var requiredMarginTop: Int = 0
    //使第一个item垂直于整个控件居中对齐需要的补充Item数量
    private var requiredSpaceCount: Int = 0
    private var posLayoutHead: Int = 0
    private var posLayoutTail: Int = 0
    private var negLayoutHead: Int = 0
    private var negLayoutTail: Int = 0
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
        requiredSpaceCount = (visibleCount - 1) / 2
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

    private fun calculateParams() {
        requiredItemHeight = height / visibleCount
        requiredMarginTop = requiredItemHeight * requiredSpaceCount
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        //<0 :手指往下滑  >0 :手指往上滑
        if (state.itemCount == 0) {
            return 0
        }
        calculateParams()
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
        if (isInfiniteScrollEnabled(itemCount)) {
            val negLenThreshold = (requiredSpaceCount + 1) * requiredItemHeight
            val posLenThreshold = childrenHeight - negLenThreshold
            val mod = scrollOffsetY % requiredItemHeight
            if (scrollOffsetY > posLenThreshold) {
                scrollOffsetY = -negLenThreshold + mod
            } else if (scrollOffsetY <= -negLenThreshold) {
                scrollOffsetY = posLenThreshold - mod
            }
        } else {
            val overflowHeight = childrenHeight - height
            if (scrollOffsetY < 0) {
                scrollOffsetY = 0
            } else if (scrollOffsetY > overflowHeight + requiredMarginTop * 2) {
                scrollOffsetY = if (overflowHeight > 0) overflowHeight + requiredMarginTop * 2 else lastOffsetY
            }
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            removeAndRecycleAllViews(recycler)
            return
        }
        calculateParams()
        //暂时分离和回收全部有效的Item
        detachAndScrapAttachedViews(recycler)

        if (state.itemCount > ((visibleCount + 1) / 2)) {
            canScrollVertically = true
        }
        layoutChildren(recycler, state)
    }

    /**
     * 布局所有子View,如果是无限循环模式,则需要负序列布局
     */
    private fun layoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val (headIndex, tailIndex) = getLayoutRange(state.itemCount)
        posLayoutHead = if (headIndex >= 0) headIndex else 0
        posLayoutTail = if (tailIndex >= 0) tailIndex else 0
        if (headIndex >= 0 && tailIndex >= 0) {
            for (i in (headIndex..tailIndex)) {
                val child = getItemView(recycler, i)
                val decoratedWidth = getDecoratedMeasuredWidth(child)
                val childTop = getLayoutTop(i)
                val childBottom = childTop + getDecoratedMeasuredHeight(child)
                layoutDecorated(child, 0, childTop, decoratedWidth, childBottom)
            }
        }
        if (isInfiniteScrollEnabled(state.itemCount)) {
            val (negHeadIndex, negTailIndex) = getNegativeLayoutRange(state.itemCount)
            negLayoutHead = if (negHeadIndex >= 0) negHeadIndex else 0
            negLayoutTail = if (negTailIndex >= 0) negTailIndex else 0
            if (negHeadIndex >= 0 && negTailIndex >= 0) {
                for (i in (negTailIndex downTo negHeadIndex)) {
                    val child = getItemView(recycler, i)
                    val decoratedWidth = getDecoratedMeasuredWidth(child)
                    val childTop = getNegativeLayoutTop(i, state.itemCount)
                    val childBottom = childTop + getDecoratedMeasuredHeight(child)
                    layoutDecorated(child, 0, childTop, decoratedWidth, childBottom)
                }
            }
        }

        val removalList = ArrayList<RecyclerView.ViewHolder>()
        removalList.addAll(recycler.scrapList)
        removalList.forEach { holder ->
            removeView(holder.itemView)
            recycler.recycleView(holder.itemView)
        }
    }

    private fun getItemView(recycler: RecyclerView.Recycler, position: Int): View {
        val child = recycler.getViewForPosition(position)
        addView(child)
        if (child.layoutParams.height <= 0) {
            val calibratedLayoutParams = child.layoutParams
            calibratedLayoutParams.height = requiredItemHeight
            child.layoutParams = calibratedLayoutParams
        }
        measureChild(child, 0, 0)
        return child
    }

    private fun isLayoutInVisibleArea(top: Int, bottom: Int): Boolean {
        return (top in (0..height)) || (bottom in (0..height))
    }

    /**
     * 获取指定Item在特定scrollOffsetY值时的顶部位置
     *
     * @param index Item在适配器中的Index值
     */
    private fun getLayoutTop(index: Int): Int {
        return index * requiredItemHeight + requiredMarginTop - scrollOffsetY
    }

    /**
     * 获取指定Item在特定scrollOffsetY值时的顶部位置(负序列定位规则)
     *
     * @param index Item在适配器中的Index值
     * @param itemCount 适配器中的itemCount
     */
    private fun getNegativeLayoutTop(index: Int, itemCount: Int): Int {
        return (requiredSpaceCount - 1) * requiredItemHeight - scrollOffsetY - (itemCount - 1 - index) * requiredItemHeight
    }

    private fun getLayoutRange(itemCount: Int): Pair<Int, Int> {
        var headIndex = -1
        var tailIndex = -1
        for (i in (0 until itemCount)) {
            val childTop = getLayoutTop(i)
            val childBottom = childTop + requiredItemHeight
            if ((childTop in 0..height) || childBottom in 0..height) {
                headIndex = i
                break
            }
        }
        for (i in (headIndex + 1 until itemCount)) {
            val childTop = getLayoutTop(i)
            if (childTop > height) {
                tailIndex = i - 1
                break
            }
        }
        if (tailIndex < 0) {
            tailIndex = itemCount - 1
        }
        return headIndex to tailIndex
    }

    private fun getNegativeLayoutRange(itemCount: Int): Pair<Int, Int> {
        var headIndex = -1
        var tailIndex = -1
        for (i in (itemCount - 1 downTo 0)) {
            val childTop = getNegativeLayoutTop(i, itemCount)
            val childBottom = childTop + requiredItemHeight
            if (isLayoutInVisibleArea(childTop, childBottom)) {
                tailIndex = i
                break
            }
        }

        for (i in (tailIndex - 1 downTo 0)) {
            val childTop = getNegativeLayoutTop(i, itemCount)
            val childBottom = childTop + requiredItemHeight
            if (childBottom < 0) {
                headIndex = i + 1
                break
            }
        }
        if (headIndex < 0) {
            headIndex = 0
        }
        return headIndex to tailIndex
    }

    private fun isInfiniteScrollEnabled(itemCount: Int): Boolean {
        return (visibleCount < itemCount) && infinity
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        when (state) {
            RecyclerView.SCROLL_STATE_DRAGGING -> {
                draggingStartListener?.invoke()
                stopScrollAnimation()
            }
            RecyclerView.SCROLL_STATE_IDLE -> {
                createScrollAnimation()
            }
        }
    }

    private fun createScrollAnimation() {
        //如果是无限循环模式，则在判断选中位置时需要考虑scrollOffset分界点的问题
        if (isInfiniteScrollEnabled(itemCount)) {
            val negLenThreshold = (requiredSpaceCount + 1) * requiredItemHeight
            val posLenThreshold = itemCount * requiredItemHeight - negLenThreshold
            val criticalValue = -requiredItemHeight / 2
            var targetPosition = -1
            var minDistance = Int.MAX_VALUE
            if (scrollOffsetY > -negLenThreshold && scrollOffsetY < criticalValue) {
                //正负序列混合布局区域
                for (i in (negLayoutTail downTo negLayoutHead)) {
                    val distance = Math.abs(getRequiredScrollOffset(i) - scrollOffsetY)
                    if (distance < minDistance) {
                        minDistance = distance
                        targetPosition = i
                    }
                }
                if (targetPosition < 0) {
                    targetPosition = 0
                }
            } else if (scrollOffsetY in criticalValue..posLenThreshold) {
                //纯正序列布局区域
                for (i in (posLayoutHead..posLayoutTail)) {
                    val distance = Math.abs(getRequiredScrollOffset(i) - scrollOffsetY)
                    if (distance < minDistance) {
                        minDistance = distance
                        targetPosition = i
                    }
                }
            }
            startScrollAnimation(targetPosition, itemCount)
        } else {
            startScrollAnimation(findClosestItemPosition(), itemCount)
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
        return if (scrollOffsetY >= -requiredItemHeight / 2) {
            targetPosition * requiredItemHeight
        } else {
            -(itemCount - targetPosition) * requiredItemHeight
        }
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

    private fun fixSelection(positionStart: Int, changeLength: Int) {
        val positionEnd = positionStart + Math.abs(changeLength)
        var newPosition = selectedPosition
        if (changeLength > 0) {
            //添加元素
            if (selectedPosition >= positionEnd) {
                newPosition += Math.abs(changeLength)
            }
        } else if (changeLength < 0) {
            //删除元素
            if (selectedPosition in (positionStart until positionEnd)) {
                newPosition = if (itemCount - 1 - positionEnd > positionStart) {
                    positionEnd
                } else {
                    positionStart - 1
                }
            } else if (selectedPosition >= positionEnd) {
                newPosition -= Math.abs(changeLength)
            }
        }
        //修正由于Item数量产生变化而scrollOffsetY越界没有重新计算的问题
        updateScrollOffsetY(0, 0, itemCount)
        scrollToPosition(newPosition)
    }

    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        super.onItemsAdded(recyclerView, positionStart, itemCount)
        fixSelection(positionStart, itemCount)
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount)
        fixSelection(positionStart, -itemCount)
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        super.onItemsChanged(recyclerView)
        selectedPosition = 0
        updateScrollOffsetY(0, 0, itemCount)
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