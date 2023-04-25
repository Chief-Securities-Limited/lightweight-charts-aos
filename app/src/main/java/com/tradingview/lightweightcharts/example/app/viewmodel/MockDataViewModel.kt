package com.tradingview.lightweightcharts.example.app.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.HistogramSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.LineSeriesOptions
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.enums.LineType
import com.tradingview.lightweightcharts.api.series.enums.LineWidth
import com.tradingview.lightweightcharts.api.series.models.CandlestickData
import com.tradingview.lightweightcharts.api.series.models.HistogramData
import com.tradingview.lightweightcharts.api.series.models.LineData
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.example.app.model.ChartModel
import com.tradingview.lightweightcharts.example.app.model.ChartSeriesModel
import com.tradingview.lightweightcharts.example.app.model.KLineGroupType
import com.tradingview.lightweightcharts.example.app.model.KLineTimeType
import com.tradingview.lightweightcharts.example.app.model.MockChartType
import com.tradingview.lightweightcharts.example.app.repository.StaticRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MockDataViewModel(application: Application) : AndroidViewModel(application) {

    private val staticRepository = StaticRepository()

    private val colorGreen = Color.argb(204, 0, 150, 136).toIntColor()
    private val colorRed = Color.argb(204, 255, 82, 82).toIntColor()

    private val seriesDataMap = mutableMapOf<MockChartType, MutableLiveData<ChartModel>>()
    private val kLineChartModel = ChartModel()
    private val mavolChartModel = ChartModel()
    private val lineChartModel = ChartModel()

    private var curKLineTimeType = KLineTimeType.MINUTE_TIME
    private var curKLineGroupType = KLineGroupType.ONLY_K_LINE

    init {
        kLineChartModel.put(K_LINE_MAIN_SERIES, ChartSeriesModel(true, CandlestickSeriesOptions(
            lastValueVisible = false,
        )))
        kLineChartModel.put(K_LINE_MAIN_SERIES_TRANSPARENT, ChartSeriesModel(true, CandlestickSeriesOptions(
            lastValueVisible = false,
            visible = false
        )))
        kLineChartModel.put(K_LINE_EMA_1, ChartSeriesModel(true, LineSeriesOptions(
            lineWidth = LineWidth.ONE,
            baseLineVisible = false,
            priceLineVisible = false,
            lastValueVisible = false,
            color = Color.GREEN.toIntColor()
        )))
        kLineChartModel.put(K_LINE_EMA_2, ChartSeriesModel(true, LineSeriesOptions(
            lineWidth = LineWidth.ONE,
            baseLineVisible = false,
            priceLineVisible = false,
            lastValueVisible = false,
            color = Color.BLUE.toIntColor()
        )))
        kLineChartModel.put(K_LINE_EMA_3, ChartSeriesModel(true, LineSeriesOptions(
            lineWidth = LineWidth.ONE,
            baseLineVisible = false,
            priceLineVisible = false,
            lastValueVisible = false,
            color = Color.RED.toIntColor()
        )))

        mavolChartModel.put(MAVOL_SERIES, ChartSeriesModel(true, HistogramSeriesOptions()))
        lineChartModel.put(LINE_SERIES, ChartSeriesModel(true, LineSeriesOptions()))

        seriesDataMap[MockChartType.K_LINE] = MutableLiveData<ChartModel>()
        seriesDataMap[MockChartType.MAVOL] = MutableLiveData<ChartModel>()
        seriesDataMap[MockChartType.LINE] = MutableLiveData<ChartModel>()
    }

    fun observeChartsData(type: MockChartType, owner: LifecycleOwner, observer: Observer<ChartModel>) {
        seriesDataMap[type]?.observe(owner, observer)
    }

    fun selectKlineTimeType(type: KLineTimeType) {
        this.curKLineTimeType = type
        loadData(type)
    }

    fun selectKLineGroupType(type: KLineGroupType) {
        this.curKLineGroupType = type
        val list = when (type) {
            KLineGroupType.ONLY_K_LINE -> KLINE_GROUP_TYPE_ONLY
            KLineGroupType.EMA -> KLINE_GROUP_TYPE_EMA
        }
        kLineChartModel.getEntryList().forEach {
            it.value.isShow = list.contains(it.key)
        }
        seriesDataMap[MockChartType.K_LINE]?.postValue(kLineChartModel)
    }

    fun getChartModel(type: MockChartType): ChartModel {
        return when (type) {
            MockChartType.K_LINE -> kLineChartModel
            MockChartType.MAVOL -> mavolChartModel
            MockChartType.LINE -> lineChartModel
        }
    }

    fun loadData(type: KLineTimeType) {
        viewModelScope.launch {
            val data = when (type) {
                KLineTimeType.MINUTE_TIME -> staticRepository.getMinuteTimeKLineData()
                KLineTimeType.FIVE_DAY -> staticRepository.getFiveDayKLineData()
                KLineTimeType.DAY_K_LINE -> staticRepository.getDayKLineData()
                KLineTimeType.WEEK_K_LINE -> staticRepository.getWeekKLineData()
                KLineTimeType.MONTH_K_LINE -> staticRepository.getMonthLineData()
                KLineTimeType.YEAR_K_LINE -> staticRepository.getYearLineData()
            }
            setupData(data)
        }
    }

    private fun setupData(list: List<SeriesData>) {
        val histogramData = mutableListOf<SeriesData>()
        val lineData = mutableListOf<SeriesData>()

        list.forEach {
            val candlestickData = it as CandlestickData

            histogramData.add(HistogramData(
                candlestickData.time,
                candlestickData.high * Random.nextInt(10),
                color = if (Random.nextBoolean()) colorGreen else colorRed
            ))

            lineData.add(LineData(
                candlestickData.time,
                candlestickData.high
            ))
        }

        setupKLine(list)
        setupMavol(histogramData)
        setupLine(lineData)
    }

    private fun setupKLine(series: List<SeriesData>, append: Boolean = false) {

        val kLineEma1List = mutableListOf<SeriesData>()
        val kLineEma2List = mutableListOf<SeriesData>()
        val kLineEma3List = mutableListOf<SeriesData>()

        series.forEach {
            val candlestickData = it as CandlestickData
            kLineEma1List.add(LineData(
                candlestickData.time,
                candlestickData.high * 1.01F
            ))
            kLineEma2List.add(LineData(
                candlestickData.time,
                candlestickData.low * 0.95F
            ))
            kLineEma3List.add(LineData(
                candlestickData.time,
                candlestickData.close * 1.01F
            ))
        }

        kLineChartModel.setupChartSeries(K_LINE_MAIN_SERIES, series, append)
        kLineChartModel.setupChartSeries(K_LINE_MAIN_SERIES_TRANSPARENT, series, append)
        kLineChartModel.setupChartSeries(K_LINE_EMA_1, kLineEma1List, append)
        kLineChartModel.setupChartSeries(K_LINE_EMA_2, kLineEma2List, append)
        kLineChartModel.setupChartSeries(K_LINE_EMA_3, kLineEma3List, append)

        kLineChartModel.setCustomMarkers(emptyList())
        if (curKLineTimeType == KLineTimeType.DAY_K_LINE) {
            val markers = mutableListOf<Time>()
            series.forEach {
                if (Random.nextInt(10) == 1) {
                    markers.add(it.time)
                }
            }
            kLineChartModel.setCustomMarkers(markers)
        }

        seriesDataMap[MockChartType.K_LINE]?.postValue(kLineChartModel)
    }

    private fun setupMavol(series: List<SeriesData>, append: Boolean = false) {
        mavolChartModel.setupChartSeries(MAVOL_SERIES, series, append)
        seriesDataMap[MockChartType.MAVOL]?.postValue(mavolChartModel)
    }

    private fun setupLine(series: List<SeriesData>, append: Boolean = false) {
        lineChartModel.setupChartSeries(LINE_SERIES, series, append)
        seriesDataMap[MockChartType.LINE]?.postValue(lineChartModel)
    }

    companion object {
        const val K_LINE_MAIN_SERIES = "K_LINE_MAIN_SERIES"
        const val K_LINE_MAIN_SERIES_TRANSPARENT = "K_LINE_MAIN_SERIES_TRANSPARENT"
        const val K_LINE_EMA_1 = "K_LINE_EMA_1"
        const val K_LINE_EMA_2 = "K_LINE_EMA_2"
        const val K_LINE_EMA_3 = "K_LINE_EMA_3"
        const val MAVOL_SERIES = "MAVOL_SERIES"
        const val LINE_SERIES = "LINE_SERIES"

        val KLINE_GROUP_TYPE_ONLY = listOf(K_LINE_MAIN_SERIES, K_LINE_MAIN_SERIES_TRANSPARENT)
        val KLINE_GROUP_TYPE_EMA = listOf(K_LINE_MAIN_SERIES, K_LINE_EMA_1, K_LINE_EMA_2, K_LINE_EMA_3)
    }

}