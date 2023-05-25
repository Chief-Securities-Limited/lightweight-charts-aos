package com.tradingview.lightweightcharts.example.app.customcharts

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi
import com.tradingview.lightweightcharts.api.options.models.AreaSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.BarSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.BaselineSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.HistogramSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.LineSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.SeriesOptionsCommon
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.models.LogicalRange
import com.tradingview.lightweightcharts.api.series.models.MouseEventParams
import com.tradingview.lightweightcharts.api.series.models.PriceScaleId
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.view.ChartsView

class CustomLightChartsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ChartsView(context, attrs, defStyleAttr, defStyleRes) {

    private val seriesMap = mutableMapOf<String, SeriesDataStatus>()
    private val timeScaleVisibleLogicalRangeChangeList: MutableList<((LogicalRange?) -> Unit)> = mutableListOf()
    private val subscribeCrosshairMoveList: MutableList<((MouseEventParams) -> Unit)> = mutableListOf()

    var mainSeriesId = ""
    private val chartsSize = ChartsSize()

    init {
        subscribeOnChartStateChange {
            when (it) {
                is State.Ready -> {
                    initChartsSize()
                }
            }
        }
        initChartsView()
    }

    private fun initChartsView() {
        api.subscribeCrosshairMove { mouseEventParams ->
            subscribeCrosshairMoveList.forEach {
                it.invoke(mouseEventParams)
            }
        }

        api.timeScale.subscribeVisibleLogicalRangeChange { logicalRange ->
            timeScaleVisibleLogicalRangeChangeList.forEach {
                it.invoke(logicalRange)
            }
        }

//        api.timeScale.subscribeSizeChange {
//            chartsSize.timeWidth = it.width
//            chartsSize.timeHeight = it.height
//            chartsSize.setChartHeight(this.getContentHeight().toFloat())
//
//            Log.d("initChartsView", "$mainSeriesId $chartsSize")
//            api.priceScale(PriceScaleId.LEFT).width { priceWidth ->
//                chartsSize.leftPriceWidth = priceWidth
//                Log.d("initChartsView", "$mainSeriesId $chartsSize")
//            }
//            api.priceScale(PriceScaleId.RIGHT).width { priceWidth ->
//                chartsSize.leftPriceWidth = priceWidth
//                Log.d("initChartsView", "$mainSeriesId $chartsSize")
//            }
//        }
    }

    fun setSeriesData(
        seriesId: String,
        seriesOptions: SeriesOptionsCommon,
        dataList: List<SeriesData>,
        onComplete: (() -> Unit)? = null
    ) {
        val dataStatus = getSeriesDataStatus(seriesId)

        dataStatus.seriesData.clear()
        dataStatus.seriesData.addAll(dataList)

        if (dataStatus.seriesApi != null) {
            dataStatus.seriesApi?.setData(dataList)
            onComplete?.invoke()
        } else if (!dataStatus.isCreating) {
            dataStatus.isCreating = true
            createSeries(seriesOptions) { seriesApi ->
                seriesMap[seriesId]?.also {
                    it.isCreating = false
                    seriesApi.setData(it.seriesData)
                    it.seriesApi = seriesApi
                }
                onComplete?.invoke()
            }
        }
    }

    fun removeSeries(seriesId: String) {
        seriesMap[seriesId]?.seriesApi?.also {
            try {
                api.removeSeries(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        seriesMap.remove(seriesId)
    }

    fun getMainSeries(): SeriesDataStatus? {
        return seriesMap[mainSeriesId]
    }

    fun subscribeTimeScaleVisibleLogicalRangeChange(onVisibleLogicalRangeChange: (params: LogicalRange?) -> Unit) {
        if (!timeScaleVisibleLogicalRangeChangeList.contains(onVisibleLogicalRangeChange)) {
            timeScaleVisibleLogicalRangeChangeList.add(onVisibleLogicalRangeChange)
        }
    }

    fun unSubscribeTimeScaleVisibleLogicalRangeChange(onVisibleLogicalRangeChange: (params: LogicalRange?) -> Unit) {
        timeScaleVisibleLogicalRangeChangeList.remove(onVisibleLogicalRangeChange)
    }

    fun subscribeCrosshairMove(onCrosshairMoved: (params: MouseEventParams) -> Unit) {
        if (!subscribeCrosshairMoveList.contains(onCrosshairMoved)) {
            subscribeCrosshairMoveList.add(onCrosshairMoved)
        }
    }

    fun unSubscribeCrosshairMove(onCrosshairMoved: (params: MouseEventParams) -> Unit) {
        subscribeCrosshairMoveList.remove(onCrosshairMoved)
    }

    fun getChartsSize(): ChartsSize {
        if (chartsSize.timeWidth == 0F || chartsSize.getChartHeight() == 0F) {
            initChartsSize()
        }
        return chartsSize
    }

    fun getRealWidth(): Int {
        return width - paddingStart - paddingEnd
    }

    private fun initChartsSize() {
        api.timeScale.width {
            chartsSize.timeWidth = it
        }
        api.timeScale.height {
            chartsSize.timeHeight = it
        }
        api.priceScale(PriceScaleId.LEFT).width { priceWidth ->
            chartsSize.leftPriceWidth = priceWidth
        }
        api.priceScale(PriceScaleId.RIGHT).width { priceWidth ->
            chartsSize.leftPriceWidth = priceWidth
        }
        chartsSize.setChartHeight(getContentHeight().toFloat())
    }

    private fun getSeriesDataStatus(seriesId: String): SeriesDataStatus {
        var dataStatus = seriesMap[seriesId]
        if (dataStatus == null) {
            dataStatus = SeriesDataStatus(seriesId, false)
            seriesMap[seriesId] = dataStatus
        }
        return dataStatus
    }

    private fun createSeries(options: SeriesOptionsCommon, onSeriesCreated: (seriesApi: SeriesApi) -> Unit) {
        when (options) {
            is CandlestickSeriesOptions -> {
                api.addCandlestickSeries(options) {
                    onSeriesCreated(it)
                }
            }

            is LineSeriesOptions -> {
                api.addLineSeries(options) {
                    onSeriesCreated(it)
                }
            }

            is AreaSeriesOptions -> {
                api.addAreaSeries(options) {
                    onSeriesCreated(it)
                }
            }

            is BarSeriesOptions -> {
                api.addBarSeries(options) {
                    onSeriesCreated(it)
                }
            }

            is HistogramSeriesOptions -> {
                api.addHistogramSeries(options) {
                    onSeriesCreated(it)
                }
            }

            is BaselineSeriesOptions -> {
                api.addBaselineSeries(options) {
                    onSeriesCreated(it)
                }
            }
        }
    }

    interface MarkersAdapter {
        fun getMarkerView(time: Time): View

        fun releaseMarkerView(time: Time, view: View)
    }
}

data class SeriesDataStatus(
    val seriesID: String,
    var isCreating: Boolean,
    val seriesData: MutableList<SeriesData> = mutableListOf(),
    var seriesApi: SeriesApi? = null
)

data class ChartsSize(
    var timeWidth: Float = 0F,
    var timeHeight: Float = 0F,
    var leftPriceWidth: Float = 0F,
    var rightPriceWidth: Float = 0F,
    private var chartHeight: Float = 0F,
) {
    fun getChartWidth(): Float {
        return timeWidth + leftPriceWidth + rightPriceWidth
    }

    fun getChartHeight(): Float {
        return chartHeight
    }

    fun setChartHeight(chartHeight: Float) {
        this.chartHeight = chartHeight
    }

    override fun toString(): String {
        return "ChartsSize(timeWidth=$timeWidth, timeHeight=$timeHeight, leftPriceWidth=$leftPriceWidth, rightPriceWidth=$rightPriceWidth, chartHeight=$chartHeight)"
    }



}
