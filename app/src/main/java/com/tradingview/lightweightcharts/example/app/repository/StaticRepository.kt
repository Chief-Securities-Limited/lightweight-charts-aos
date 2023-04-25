package com.tradingview.lightweightcharts.example.app.repository

import com.tradingview.lightweightcharts.api.options.models.PriceLineOptions
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.models.CandlestickData
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.example.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StaticRepository {
    suspend fun getBarChartSeriesData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listBarChartSeriesBarData()
        }
    }

    suspend fun getCustomPriceFormatterSeriesData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listCustomPriceFormatterSeriesLineData()
        }
    }

    suspend fun getCustomThemesSeriesData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listCustomThemesSeriesLineData()
        }
    }

    suspend fun getFloatingTooltipSeriesData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listFloatingTooltipSeriesLineData()
        }
    }

    suspend fun getPriceLinesWithTitlesSeriesData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listPriceLinesWithTitlesSeriesLineData()
        }
    }

    suspend fun getRealTimeEmulationSeriesData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listRealTimeEmulationSeriesCandlestickData()
        }
    }

    suspend fun getVolumeStudyAreaData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listVolumeStudyAreaData()
        }
    }

    suspend fun getVolumeStudySeriesData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listVolumeStudySeriesData()
        }
    }

    suspend fun getSeriesMarkersSeriesData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listSeriesMarkersSeriesData()
        }
    }

    suspend fun getPriceLineOptions(): PriceLineOptions {
        return withContext(Dispatchers.IO) {
            return@withContext priceLineOptions()
        }
    }

    suspend fun getListAreaSeriesData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            return@withContext listAreaSeriesData()
        }
    }

    suspend fun getMinuteTimeKLineData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            val timestamp = 1560211200L
            val data = listRealTimeEmulationSeriesCandlestickData()
            val newData = mutableListOf<SeriesData>()
            for (i in 0 until data.size) {
                val candlestickData = data[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 60),
                    open = candlestickData.open,
                    high = candlestickData.high,
                    low = candlestickData.low,
                    close = candlestickData.close
                ))
            }
            newData
        }
    }

    suspend fun getFiveDayKLineData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            val timestamp = 1560211200L
            val data = listRealTimeEmulationSeriesCandlestickData()
            val newData = mutableListOf<SeriesData>()
            for (i in 0 until data.size) {
                val candlestickData = data[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 5 * 24 * 60 * 60),
                    open = candlestickData.open,
                    high = candlestickData.high,
                    low = candlestickData.low,
                    close = candlestickData.close
                ))
            }
            newData
        }
    }

    suspend fun getDayKLineData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            val timestamp = 1560211200L
            val data = listRealTimeEmulationSeriesCandlestickData()
            val newData = mutableListOf<SeriesData>()
            for (i in 0 until data.size) {
                val candlestickData = data[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 24 * 60 * 60),
                    open = candlestickData.open,
                    high = candlestickData.high,
                    low = candlestickData.low,
                    close = candlestickData.close
                ))
            }
            newData
        }
    }

    suspend fun getWeekKLineData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            val timestamp = 1560211200L
            val data = listRealTimeEmulationSeriesCandlestickData()
            val newData = mutableListOf<SeriesData>()
            for (i in 0 until data.size) {
                val candlestickData = data[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 7 * 24 * 60 * 60),
                    open = candlestickData.open,
                    high = candlestickData.high,
                    low = candlestickData.low,
                    close = candlestickData.close
                ))
            }
            newData
        }
    }

    suspend fun getMonthLineData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            val timestamp = 1560211200L
            val data = listRealTimeEmulationSeriesCandlestickData()
            val newData = mutableListOf<SeriesData>()
            for (i in 0 until data.size) {
                val candlestickData = data[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 7 * 24 * 60 * 60),
                    open = candlestickData.open,
                    high = candlestickData.high,
                    low = candlestickData.low,
                    close = candlestickData.close
                ))
            }
            newData
        }
    }

    suspend fun getYearLineData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            val timestamp = 1560211200L
            val data = listRealTimeEmulationSeriesCandlestickData()
            val newData = mutableListOf<SeriesData>()
            for (i in 0 until data.size) {
                val candlestickData = data[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 7 * 24 * 60 * 60),
                    open = candlestickData.open,
                    high = candlestickData.high,
                    low = candlestickData.low,
                    close = candlestickData.close
                ))
            }
            newData
        }
    }
}