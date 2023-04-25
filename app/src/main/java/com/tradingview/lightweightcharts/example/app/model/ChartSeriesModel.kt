package com.tradingview.lightweightcharts.example.app.model

import com.tradingview.lightweightcharts.api.interfaces.SeriesApi
import com.tradingview.lightweightcharts.api.options.models.SeriesOptionsCommon
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.models.Time

class ChartSeriesModel(
    var isShow: Boolean = true,
    private val seriesOptions: SeriesOptionsCommon
) {

    private val list = mutableListOf<SeriesData>()
    var seriesApi: SeriesApi? = null

    fun clear() {
        list.clear()
    }

    fun add(data: SeriesData) {
        list.add(data)
    }

    fun addAll(list: List<SeriesData>) {
        this.list.addAll(list)
    }

    fun getList(): List<SeriesData> {
        return list
    }

    fun getSeriesOptions(): SeriesOptionsCommon {
        return seriesOptions
    }

    fun findSeriesData(time: Time): SeriesData? {
        return list.find {
            it.time.date.time == time.date.time
        }
    }
}