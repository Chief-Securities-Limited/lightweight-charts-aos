package com.tradingview.lightweightcharts.example.app.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi
import com.tradingview.lightweightcharts.api.series.models.LogicalRange
import com.tradingview.lightweightcharts.api.series.models.MouseEventParams
import com.tradingview.lightweightcharts.api.series.models.PriceScaleId
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.example.app.R
import com.tradingview.lightweightcharts.view.ChartsView

class ChartsToolsLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val TAG = ChartsToolsLayout::class.java.simpleName

    private var chartsView: ChartsView? = null
    private var verticalView = View(context)
    private var crosshairLabelView = TextView(context)
    private var hasAddCrossHairView = false

    private val chartsViewSize = ChartsViewSize()

    private var crossHairWidth: Int
    private var crossHairHeight = 0

    private var markersAdapter: MarkersAdapter? = null
    private val markerTimeList = mutableListOf<Time>()
    private val curMarkersMap = mutableMapOf<Time, View>()
    private var hasSubscribeMarkersHandle = false
    private var markersSize: Size
    private var markersEnable = false

    private val onVisibleLogicalRangeChanged = fun(params: LogicalRange?) {
        if (!markersEnable) return
        updateMarkers()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        Log.d("dispatchTouchEvent", "${ev.y} : ${ev.rawY}")

        chartsView?.also { chartsView ->
            var height = ((chartsView.height) * 1F / chartsView.width) * (chartsViewSize.getWidth())

            height = chartsView.getContentHeight().toFloat()
            crosshairLabelView.y = ev.y
            val pointY = ev.y / chartsView.height * height

            Log.d("crosshairLabelView", "height: $height")
            Log.d("crosshairLabelView", "moveY: $pointY")
            seriesApi?.coordinateToPrice(pointY) {
                Log.d("crosshairLabelView", "moveValue: $it")
                crosshairLabelView.text = it?.toString()
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Log.d("onInterceptTouchEvent", "ChartsToolsLayout")
        return super.onInterceptTouchEvent(ev)
    }

    init {
        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            for (i in 0 until childCount) {
                val childView = getChildAt(i)
                if (childView is ChartsView) {
                    chartsView = childView
                }
            }

            chartsView?.api?.timeScale?.width {
                chartsViewSize.setTimeWidth(it)
                val height = (chartsView!!.height * 1F / chartsView!!.width) * chartsViewSize.getWidth()
                chartsViewSize.priceHeight = height
            }

            chartsView?.api?.timeScale?.height {
                Log.d("timeScaleHeight", "${it}")
            }

            chartsView?.api?.priceScale(PriceScaleId.LEFT)?.width {
                chartsViewSize.setPriceWidth(it)
                val height = (chartsView!!.height * 1F / chartsView!!.width) * chartsViewSize.getWidth()
                chartsViewSize.priceHeight = height
            }

            chartsView?.api?.timeScale?.subscribeSizeChange {
                Log.d("subscribeSizeChange", "${it}")
            }
        }

        val density = resources.displayMetrics.density
        crossHairWidth = (density * 1).toInt()
        markersSize = Size((density * 15).toInt(), (density * 15).toInt())
    }

    fun getChartsView(): ChartsView? {
        return chartsView
    }

    fun setMarkers(markerTimeList: List<Time>) {
        curMarkersMap.entries.forEach {
            removeView(it.value)
            markersAdapter?.releaseMarkerView(it.key, it.value)
        }
        curMarkersMap.clear()
        this.markerTimeList.clear()
        this.markerTimeList.addAll(markerTimeList)

        if (!hasSubscribeMarkersHandle) {
            hasSubscribeMarkersHandle = true
            chartsView?.api?.timeScale?.subscribeVisibleLogicalRangeChange(onVisibleLogicalRangeChanged)
        }
        markersEnable = true
        updateMarkers()
    }

    fun setSetMarkersAdapter(markersAdapter: MarkersAdapter) {
        this.markersAdapter = markersAdapter
    }

    fun clearMarkers() {
        markersEnable = false
        curMarkersMap.entries.forEach {
            removeView(it.value)
            markersAdapter?.releaseMarkerView(it.key, it.value)
        }
        curMarkersMap.clear()
        this.markerTimeList.clear()
        hasSubscribeMarkersHandle = false
        chartsView?.api?.timeScale?.unsubscribeVisibleLogicalRangeChange(onVisibleLogicalRangeChanged)
    }

    fun showCustomCrossHair(params: MouseEventParams) {
        val time = params.time
        if (time == null) {
            verticalView.isVisible = false
            return
        }


        Log.d("crosshairLabelView", "pointY: ${params.point?.y}")
        initCrossHairView()
        verticalView.isVisible = true
        chartsView?.also { chartsView ->
            chartsView.api.timeScale.timeToCoordinate(time) {
                if (it != null) {
                    verticalView.x = it / chartsViewSize.getWidth() * chartsView.width

//                    val height = (chartsView.height * 1F / chartsView.width) * chartsViewSize.getWidth()
//                    crosshairLabelView.y = params.point!!.y / height * chartsView.height
//                    val prices = params.seriesPrices?.first()?.prices
//
//                    Log.d("crosshairLabelView", "${prices?.toString()}")
//                    seriesApi?.coordinateToPrice(crosshairLabelView.y) {
//                        crosshairLabelView.text = it?.toString()
//                    }

                    seriesApi?.coordinateToPrice(params.point?.y ?: 0F) {
                        Log.d("crosshairLabelView", "pointValue: $it")
//                        crosshairLabelView.text = it?.toString()
                    }
                }
            }
        }
    }

    private var seriesApi: SeriesApi? = null
    fun setSeriesApi(seriesApi: SeriesApi?) {
        if (seriesApi != null) {
            this.seriesApi = seriesApi
        }
    }

    fun hideCustomCrossHair() {
        verticalView.isVisible = false
    }

    private fun initCrossHairView() {
        chartsView?.also { chartsView ->
            if (crossHairHeight == 0) {
                val bottom = resources.displayMetrics.density * 20
                crossHairHeight = (chartsView.height - bottom).toInt()
            }

            if (!hasAddCrossHairView) {
                hasAddCrossHairView = true
                verticalView.setBackgroundResource(R.color.colorPrimary)
                addView(verticalView, LayoutParams(
                    crossHairWidth,
                    crossHairHeight
                ))
                crosshairLabelView.setBackgroundResource(R.color.colorPrimary)
                addView(crosshairLabelView, LayoutParams(
                    150,
                    50
                ))
            }
        }
    }

    private fun updateMarkers() {
        val adapter = this.markersAdapter
        if (adapter == null || markerTimeList.isEmpty()) {
            return
        }

        chartsView?.api?.timeScale?.also { timeScale ->
            markerTimeList.forEach { time ->
                if (!markersEnable) return@also
                val currentTimeMillis = System.currentTimeMillis()
                timeScale.timeToCoordinate(time) {
                    if (!markersEnable) return@timeToCoordinate
                    it?.also {
                        Log.d(TAG, "timeToCoordinate Time: ${it}")
                        if (it < 0 || it > chartsViewSize.getTimeWidth()) {
                            curMarkersMap[time]?.also { view ->
                                curMarkersMap.remove(time)
                                removeView(view)
                                adapter.releaseMarkerView(time, view)
                            }
                            return@timeToCoordinate
                        }

                        val markerView = if (curMarkersMap.containsKey(time)) {
                            curMarkersMap[time]
                        } else {
                            val markerView = adapter.getMarkerView(time)
                            addView(markerView, LayoutParams(markersSize.width, markersSize.height))
                            curMarkersMap[time] = markerView
                            markerView
                        }
                        if (markerView != null) {
                            markerView.x = it / chartsViewSize.getWidth() * width - markerView.width / 2
                            markerView.y = height - resources.displayMetrics.density * 60
                        }

                        Log.d(TAG, "Cost Time: ${System.currentTimeMillis() - currentTimeMillis}")
                    }
                }
            }
        }
    }

    interface MarkersAdapter {
        fun getMarkerView(time: Time): View

        fun releaseMarkerView(time: Time, view: View)
    }
}

private class ChartsViewSize {
    private var timeWidth = 0F
    private var priceWidth = 0F

    var priceHeight = 0F

    fun setTimeWidth(timeWidth: Float) {
        this.timeWidth = timeWidth
    }

    fun getTimeWidth(): Float {
        return this.timeWidth
    }

    fun setPriceWidth(priceWidth: Float) {
        this.priceWidth = priceWidth
    }

    fun getPriceWidth(): Float {
        return this.priceWidth
    }

    fun getWidth(): Float {
        return timeWidth + priceWidth
    }

}