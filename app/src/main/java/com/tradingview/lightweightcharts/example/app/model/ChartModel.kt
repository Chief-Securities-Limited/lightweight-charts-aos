package com.tradingview.lightweightcharts.example.app.model

import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.models.Time

class ChartModel {

    private val chartDataMap = mutableMapOf<String, ChartSeriesModel>()
    private val customMarkers = mutableListOf<Time>()
    private var chartsVersion = 0

    fun put(key: String, chartSeriesModel: ChartSeriesModel) {
        chartDataMap[key] = chartSeriesModel
    }

    fun get(key: String): ChartSeriesModel? {
        return chartDataMap[key]
    }

    fun getAll(): List<ChartSeriesModel> {
        return mutableListOf<ChartSeriesModel>().apply {
            addAll(chartDataMap.values)
        }
    }

    fun getEntryList(): MutableSet<MutableMap.MutableEntry<String, ChartSeriesModel>> {
        return chartDataMap.entries
    }

    fun setupChartSeries(key: String, list: List<SeriesData>, isAppend: Boolean = false) {
        get(key)?.apply {
            if (!isAppend) {
                clear()
            }
            addAll(list)
        }
    }

    fun clear() {
        chartDataMap.clear()
    }

    fun clear(key: String) {
        get(key)?.clear()
    }

    fun getCustomMarkers(): List<Time> {
        return customMarkers
    }

    fun setCustomMarkers(markers: List<Time>) {
        customMarkers.clear()
        customMarkers.addAll(markers)
    }

    fun updateChartsVersion(newChartsVersion: Int) {
        if (this.chartsVersion == newChartsVersion) {
            return
        }
        this.chartsVersion = newChartsVersion
        chartDataMap.values.forEach {
            it.seriesApi = null
        }
    }

    fun getChartsVersion(): Int {
        return chartsVersion
    }
}