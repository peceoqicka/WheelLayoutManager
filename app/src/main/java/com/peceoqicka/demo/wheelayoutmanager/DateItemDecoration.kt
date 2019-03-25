package com.peceoqicka.demo.wheelayoutmanager

import android.graphics.Canvas
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import com.peceoqicka.wheellayoutmanager.WheelLayoutManager

/**
 * <pre>
 *      author  :   peceoqicka
 *      time    :   2019/3/14
 *      version :   1.0
 *      desc    :
 * </pre>
 */
class DateItemDecoration(
    highlightBackgroundColor: Int,
    highlightLineWidth: Int,
    highlightLineColor: Int,
    private val enableHighlightMarker: Boolean = false,
    private val highlightMarkerWidth: Int = 0,
    highlightMarkerColor: Int = 0,
    private val enableHintText: Boolean = false,
    private val hintText: String = "",
    private val hintTextRightMargin: Int = 0,
    hintTextSize: Int = 1,
    hintTextColor: Int = 0
) :
    RecyclerView.ItemDecoration() {
    private val highlightMarkerPaint = Paint()
    private val highlightLinePaint = Paint()
    private val highlightBgPaint = Paint()
    private val hintTextPaint = Paint()
    private var hintTextDrawingOffsetY: Int = 0

    init {

        highlightMarkerPaint.apply {
            color = highlightMarkerColor
            style = Paint.Style.FILL
        }
        highlightLinePaint.apply {
            color = highlightLineColor
            strokeWidth = highlightLineWidth.toFloat()
            isAntiAlias = true
        }
        highlightBgPaint.apply {
            color = highlightBackgroundColor
            style = Paint.Style.FILL
        }
        hintTextPaint.apply {
            color = hintTextColor
            textSize = hintTextSize.toFloat()
            isAntiAlias = true
            hintTextDrawingOffsetY = -(fontMetricsInt.bottom - fontMetricsInt.top) / 2 - fontMetricsInt.top
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val layoutManager = parent.layoutManager as? WheelLayoutManager ?: return
        val highlightLineTop = layoutManager.selectionTop + 0f
        val highlightLineBottom = highlightLineTop + layoutManager.itemHeight + 0f

        val parentWidth = parent.width + 0f
        c.drawRect(0f, highlightLineTop, parentWidth, highlightLineBottom, highlightBgPaint)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val layoutManager = parent.layoutManager as? WheelLayoutManager ?: return
        val highlightLineTop = layoutManager.selectionTop + 0f
        val highlightLineBottom = highlightLineTop + layoutManager.itemHeight + 0f

        val parentWidth = parent.width + 0f
        val parentHeight = parent.height + 0f
        c.drawLine(0f, 0f, parentWidth, 0f, highlightLinePaint)
        c.drawLine(0f, highlightLineTop, parentWidth, highlightLineTop, highlightLinePaint)
        c.drawLine(0f, highlightLineBottom, parentWidth, highlightLineBottom, highlightLinePaint)
        c.drawLine(0f, parentHeight, parentWidth, parentHeight, highlightLinePaint)

        if (enableHighlightMarker) {
            c.drawRect(
                0f, highlightLineTop, highlightMarkerWidth.toFloat(),
                highlightLineBottom, highlightMarkerPaint
            )
        }
        if (enableHintText) {
            val textY = highlightLineTop + layoutManager.itemHeight / 2 + hintTextDrawingOffsetY
            val textX = parent.width - hintTextPaint.measureText(hintText) - hintTextRightMargin
            c.drawText(hintText, textX, textY, hintTextPaint)
        }
    }
}