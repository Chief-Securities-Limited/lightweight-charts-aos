package com.tradingview.lightweightcharts.example.app.extension

import com.tradingview.lightweightcharts.api.delegates.ChartApiDelegate
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi
import com.tradingview.lightweightcharts.api.options.models.AreaSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.BarSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.BaselineSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.HistogramSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.LineSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.SeriesOptionsCommon
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.models.TimeRange
import com.tradingview.lightweightcharts.example.app.model.ChartSeriesModel

fun ChartApiDelegate.removeSeriesEx(seriesApi: SeriesApi?, onSeriesDeleted: () -> Unit) {
    seriesApi?.also {
        try {
            this.removeSeries(it, onSeriesDeleted)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun ChartApiDelegate.setupSeries(chartSeriesModel: ChartSeriesModel) {
    val seriesApi = chartSeriesModel.seriesApi
    if (!chartSeriesModel.isShow) {
        removeSeriesEx(chartSeriesModel.seriesApi) {}
        chartSeriesModel.seriesApi = null
        return
    }
    val list = chartSeriesModel.getList()
    val toTime = if (list.size > 50) list.get(49).time else list.last().time

    if (seriesApi != null) {
        seriesApi.setData(list)
        timeScale.setVisibleRange(TimeRange(
            from = list.first().time,
            to = toTime
        ))
    } else {
        createSeries(chartSeriesModel.getSeriesOptions(), list) {
            removeSeriesEx(chartSeriesModel.seriesApi) {}
            chartSeriesModel.seriesApi = it
            timeScale.setVisibleRange(TimeRange(
                from = list.first().time,
                to = toTime
            ))
        }
    }
}

fun ChartApiDelegate.createSeries(options: SeriesOptionsCommon, list: List<SeriesData>, onSeriesCreated: (api: SeriesApi) -> Unit) {
    when (options) {
        is CandlestickSeriesOptions -> {
            addCandlestickSeries(options) {
                it.setData(list)
                onSeriesCreated(it)
            }
        }

        is LineSeriesOptions -> {
            addLineSeries(options) {
                it.setData(list)
                onSeriesCreated(it)
            }
        }

        is AreaSeriesOptions -> {
            addAreaSeries(options) {
                it.setData(list)
                onSeriesCreated(it)
            }
        }

        is BarSeriesOptions -> {
            addBarSeries(options) {
                it.setData(list)
                onSeriesCreated(it)
            }
        }

        is HistogramSeriesOptions -> {
            addHistogramSeries(options) {
                it.setData(list)
                onSeriesCreated(it)
            }
        }

        is BaselineSeriesOptions -> {
            addBaselineSeries(options) {
                it.setData(list)
                onSeriesCreated(it)
            }
        }
    }
}