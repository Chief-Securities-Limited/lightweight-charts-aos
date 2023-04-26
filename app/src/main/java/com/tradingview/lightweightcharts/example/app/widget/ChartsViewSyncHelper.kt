package com.tradingview.lightweightcharts.example.app.widget

import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import com.tradingview.lightweightcharts.api.series.models.LogicalRange
import com.tradingview.lightweightcharts.api.series.models.MouseEventParams
import com.tradingview.lightweightcharts.view.gesture.TouchDelegate

class ChartsViewSyncHelper {

    private val touchChartsHashCode = TouchChartsHashCode()
    private val chartsViewLayoutWrapperMap = mutableMapOf<Int, ChartsViewWrapper>()
    private var syncCrosshairMoveListener: SyncCrosshairMoveListener? = null

    private val visibleLogicalRangeChangeListener = object : VisibleLogicalRangeChangeListener {
        override fun onVisibleLogicalRangeChange(layout: ChartsToolsLayout, params: LogicalRange?) {
            params?.also { logicalRange ->
                chartsViewLayoutWrapperMap.entries.forEach {
                    if (it.key != layout.hashCode()) {
                        it.value.updateVisibleLogicalRange(logicalRange)
                    }
                }
            }
        }
    }

    private val crosshairMoveListener = object : CrosshairMoveListener {
        override fun onCrosshairMove(layout: ChartsToolsLayout, params: MouseEventParams) {
            chartsViewLayoutWrapperMap.entries.forEach {
                it.value.updateCrosshair(params)
            }
            syncCrosshairMoveListener?.onSyncCrosshairMove(params)
        }
    }

    fun addChartsView(vararg layouts: ChartsToolsLayout) {
        layouts.forEach { layout ->
            val hashCode = layout.hashCode()
            if (!chartsViewLayoutWrapperMap.containsKey(hashCode)) {
                chartsViewLayoutWrapperMap[hashCode] = ChartsViewWrapper(
                    touchChartsHashCode,
                    layout,
                    visibleLogicalRangeChangeListener,
                    crosshairMoveListener
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

    fun setSyncCrosshairMoveListener(syncCrosshairMoveListener: SyncCrosshairMoveListener) {
        this.syncCrosshairMoveListener = syncCrosshairMoveListener
    }

    fun clear() {
        chartsViewLayoutWrapperMap.values.forEach {
            it.unSubscribeAll()
        }
        chartsViewLayoutWrapperMap.clear()
    }

    private class ChartsViewWrapper(
        val touchChartsHashCode: TouchChartsHashCode,
        val layout: ChartsToolsLayout,
        private val visibleLogicalRangeChangeListener: VisibleLogicalRangeChangeListener,
        private val crosshairMoveListener: CrosshairMoveListener) {

        private var lastCrosshairTime: Long? = -1L

        val onVisibleLogicalRangeChanged = fun(logicalRange: LogicalRange?) {
            if (touchChartsHashCode.code == layout.hashCode()) {
                visibleLogicalRangeChangeListener.onVisibleLogicalRangeChange(layout, logicalRange)
            }
        }

        val onCrosshairMove = fun(params: MouseEventParams) {
            val time = params.time?.date?.time
            if (touchChartsHashCode.code == layout.hashCode() && lastCrosshairTime != time) {
                lastCrosshairTime = time
                crosshairMoveListener.onCrosshairMove(layout, params)
            }
        }

        val touchDelegate = object : TouchDelegate {
            override fun beforeTouchEvent(view: ViewGroup) {}

            override fun onTouchEvent(view: ViewGroup, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        touchChartsHashCode.code = layout.hashCode()
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        lastCrosshairTime = -1L
                    }
                }
                return false
            }
        }

        init {
            layout.getChartsView()?.also {
                it.addTouchDelegate(touchDelegate)
                it.api.subscribeCrosshairMove(onCrosshairMove)
                it.api.timeScale.subscribeVisibleLogicalRangeChange(onVisibleLogicalRangeChanged)
            }
        }

        fun updateVisibleLogicalRange(logicalRange: LogicalRange) {
            layout.getChartsView()?.api?.timeScale?.setVisibleLogicalRange(logicalRange)
        }

        fun updateCrosshair(params: MouseEventParams) {
            layout.showCustomCrossHair(params)
        }

        fun unSubscribeAll() {
            layout.getChartsView()?.also {
                it.removeTouchDelegate(touchDelegate)
                it.api.timeScale.unsubscribeVisibleLogicalRangeChange(onVisibleLogicalRangeChanged)
                it.api.unsubscribeCrosshairMove(onCrosshairMove)
            }
        }
    }

    private interface VisibleLogicalRangeChangeListener {
        fun onVisibleLogicalRangeChange(layout: ChartsToolsLayout, params: LogicalRange?)
    }

    private interface CrosshairMoveListener {
        fun onCrosshairMove(layout: ChartsToolsLayout, params: MouseEventParams)
    }

    private data class TouchChartsHashCode(
        var code: Int = 0
    )
}

interface SyncCrosshairMoveListener {
    fun onSyncCrosshairMove(params: MouseEventParams)
}



