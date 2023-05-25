package com.tradingview.lightweightcharts.example.app.customcharts

import android.view.MotionEvent
import android.view.ViewGroup
import com.tradingview.lightweightcharts.api.series.models.LogicalRange
import com.tradingview.lightweightcharts.example.app.widget.ChartsToolsLayout
import com.tradingview.lightweightcharts.view.gesture.TouchDelegate

class CustomLightChartsSyncHelper {

    private val touchChartsID = TouchChartsID()
    private val chartsViewLayoutWrapperMap = mutableMapOf<Int, ChartsViewWrapper>()

    private val visibleLogicalRangeChangeListener = object :
        VisibleLogicalRangeChangeListener {
        override fun onVisibleLogicalRangeChange(chartsView: CustomLightChartsView, params: LogicalRange?) {
            params?.also { logicalRange ->
                chartsViewLayoutWrapperMap.entries.forEach {
                    if (it.key != chartsView.hashCode()) {
                        it.value.updateVisibleLogicalRange(logicalRange)
                    }
                }
            }
        }
    }

    fun addChartsView(vararg ChartsViews: CustomLightChartsView) {
        ChartsViews.forEach { chartsView ->
            val hashCode = chartsView.hashCode()
            if (!chartsViewLayoutWrapperMap.containsKey(hashCode)) {
                chartsViewLayoutWrapperMap[hashCode] = ChartsViewWrapper(
                    touchChartsID,
                    chartsView,
                    visibleLogicalRangeChangeListener,
                )
            }
        }
    }

    fun removeChartsView(layout: ChartsToolsLayout) {
        val hashCode = layout.hashCode()
        chartsViewLayoutWrapperMap[hashCode]?.also {
            it.unSubscribeAll()
        }
        chartsViewLayoutWrapperMap.remove(hashCode)
    }

    private class ChartsViewWrapper(
        val touchChartsID: TouchChartsID,
        val chartsView: CustomLightChartsView,
        private val visibleLogicalRangeChangeListener: VisibleLogicalRangeChangeListener) {

        val onVisibleLogicalRangeChanged = fun(logicalRange: LogicalRange?) {
            if (touchChartsID.code == chartsView.hashCode()) {
                visibleLogicalRangeChangeListener.onVisibleLogicalRangeChange(chartsView, logicalRange)
            }
        }

        val touchDelegate = object : TouchDelegate {
            override fun beforeTouchEvent(view: ViewGroup) {
            }

            override fun onTouchEvent(view: ViewGroup, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        touchChartsID.code = chartsView.hashCode()
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {

                    }
                }
                return false
            }
        }

        init {
            chartsView.addTouchDelegate(touchDelegate)
            chartsView.subscribeTimeScaleVisibleLogicalRangeChange(onVisibleLogicalRangeChanged)
        }

        fun updateVisibleLogicalRange(logicalRange: LogicalRange) {
            chartsView.api.timeScale.setVisibleLogicalRange(logicalRange)
        }

        fun unSubscribeAll() {
            chartsView.removeTouchDelegate(touchDelegate)
            chartsView.unSubscribeTimeScaleVisibleLogicalRangeChange(onVisibleLogicalRangeChanged)
        }
    }

    private interface VisibleLogicalRangeChangeListener {
        fun onVisibleLogicalRangeChange(chartsView: CustomLightChartsView, params: LogicalRange?)
    }

    private data class TouchChartsID(
        var code: Int = 0
    )
}