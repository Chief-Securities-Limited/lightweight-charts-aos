package com.tradingview.lightweightcharts.example.app.customcharts

import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.plus
import androidx.core.view.isVisible
import androidx.core.view.marginStart
import com.tradingview.lightweightcharts.api.series.models.MouseEventParams
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.example.app.R

class CustomLightChartsCrossHairLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val chartsViewList = mutableListOf<CustomLightChartsView>()

    private val subscribeCrosshairMoveList: MutableList<((Time?) -> Unit)> = mutableListOf()

    // cross hair
    private val verticalView = View(context)
    private val horizontalView = View(context)
    private var crosshairLabelView = TextView(context)
    private var hasAddCrossHairView = false
    private var crossHairRunning = false

    private val onCrosshairMove = fun(params: MouseEventParams) {
        crossHairRunning = params.time != null
        if (crossHairRunning) {
            updateCrossVerticalHair(params)
        } else {
            hideCrossHair()
        }
        subscribeCrosshairMoveList.forEach {
            it.invoke(params.time)
        }
    }

    private val onLayoutChangeListener = View.OnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
        chartsViewList.forEach {
            it.unSubscribeCrosshairMove(onCrosshairMove)
        }
        chartsViewList.clear()
        doFindCustomLightChartsView(this)
        chartsViewList.forEach {
            it.subscribeCrosshairMove(onCrosshairMove)
        }
    }

    init {
        addOnLayoutChangeListener(onLayoutChangeListener)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

            }

            else -> {
                if (crossHairRunning) updateHorizontalView(ev)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    fun subscribeCrosshairMove(onCrosshairMoved: (params: Time?) -> Unit) {
        if (!subscribeCrosshairMoveList.contains(onCrosshairMoved)) {
            subscribeCrosshairMoveList.add(onCrosshairMoved)
        }
    }

    fun unSubscribeCrosshairMove(onCrosshairMoved: (params: Time?) -> Unit) {
        subscribeCrosshairMoveList.remove(onCrosshairMoved)
    }

    private fun hideCrossHair() {
        verticalView.isVisible = false
        horizontalView.isVisible = false
        crosshairLabelView.isVisible = false
    }

    private fun updateHorizontalView(ev: MotionEvent) {
        if (!crossHairRunning) {
            return
        }

        initCrossHairView()

        crosshairLabelView.y = ev.y - crosshairLabelView.height / 2
        horizontalView.y = ev.y

        val chartsView = chartsViewList.firstOrNull {
            val viewY = it.getLocationPointOnScreen().y
            viewY < ev.rawY && ev.rawY < (viewY + it.height)
        }

        crosshairLabelView.isVisible = chartsView != null
        horizontalView.isVisible = chartsView != null

        chartsView?.also {
            val location = IntArray(2)
            it.getLocationOnScreen(location)
            val viewY = location[1]
            val chartsY = ev.rawY - viewY
            val chartHeight = chartsView.getChartsSize().getChartHeight()
            val seriesApi = chartsView.getMainSeries()?.seriesApi

            if (seriesApi != null) {
                val chartPointY = chartsY / chartsView.height * chartHeight
                seriesApi.coordinateToPrice(chartPointY) {
                    crosshairLabelView.text = it?.toString()
                }
            }
        }
    }

    private fun updateCrossVerticalHair(params: MouseEventParams) {
        val time = params.time ?: return
        initCrossHairView()
        verticalView.isVisible = true
        chartsViewList.firstOrNull()?.also { chartsView ->
            chartsView.api.timeScale.timeToCoordinate(time) {
                if (it != null) {
                    val chartsViewX = it / chartsView.getChartsSize().getChartWidth() * chartsView.getRealWidth()
                    val layoutToChartsViewDistance = chartsView.getLocationPointOnScreen().x - getLocationPointOnScreen().x
                    verticalView.x = chartsViewX + layoutToChartsViewDistance + chartsView.paddingStart
                }
            }
        }
    }

    private fun initCrossHairView() {
        if (!hasAddCrossHairView) {
            hasAddCrossHairView = true

            val bottom = resources.displayMetrics.density * 26
            val crossHairHeight = (this.height - bottom).toInt()
            val crossHairWidth = (resources.displayMetrics.density * 1).toInt()

            verticalView.setBackgroundResource(R.color.colorPrimary)
            addView(verticalView, LayoutParams(
                crossHairWidth,
                crossHairHeight
            ))
            crosshairLabelView.setBackgroundResource(R.color.colorPrimary)
            addView(crosshairLabelView, LayoutParams(
                LayoutParams.WRAP_CONTENT,
                50
            ))
            horizontalView.setBackgroundResource(R.color.colorPrimary)
            addView(horizontalView, LayoutParams(
                LayoutParams.MATCH_PARENT,
                crossHairWidth
            ))
        }
    }

    private fun doFindCustomLightChartsView(view: View) {
        if (view is CustomLightChartsView) {
            chartsViewList.add(view)
        } else if (view is ViewGroup) {
            val childCount = view.childCount
            for (i in 0 until childCount) {
                val childView = view.getChildAt(i)
                if (childView != null) {
                    doFindCustomLightChartsView(childView)
                }
            }
        }
    }
}

private fun View.getLocationPointOnScreen(): Point {
    val location = IntArray(2)
    getLocationOnScreen(location)
    return Point(location[0], location[1])
}
