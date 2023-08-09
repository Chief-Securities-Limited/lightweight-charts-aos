package com.tradingview.lightweightcharts.example.app.repository

import com.tradingview.lightweightcharts.api.options.models.PriceLineOptions
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.models.CandlestickData
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.example.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

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

            val count = data.size / 5
            val interval = 24 * 60 * 60 / count

            for (i in 0 until data.size) {
                val candlestickData = data[i] as CandlestickData
                val add = if (i % 5 == 0) 1 else 0
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * interval),
                    open = candlestickData.open + add,
                    high = candlestickData.high + add,
                    low = candlestickData.low + add,
                    close = candlestickData.close + add
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
                val add = if (i % 3 == 0) 1 else 0
                val candlestickData = data[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 24 * 60 * 60),
                    open = candlestickData.open + add,
                    high = candlestickData.high + add,
                    low = candlestickData.low + add,
                    close = candlestickData.close + add
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
                val add = if (i % 2 == 0) 1 else 0
                val candlestickData = data[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 7 * 24 * 60 * 60),
                    open = candlestickData.open + add,
                    high = candlestickData.high + add,
                    low = candlestickData.low + add,
                    close = candlestickData.close + add
                ))
            }
            newData
        }
    }

    suspend fun getMonthLineData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            val data = listRealTimeEmulationSeriesCandlestickData()
            val newData = mutableListOf<SeriesData>()
            val timestamp = 1263096000L
            for (i in 0 until data.size) {
                val add = if (i % 4 == 0) 1 else 0
                val candlestickData = data[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 30 * 24 * 60 * 60L),
                    open = candlestickData.open + add,
                    high = candlestickData.high + add,
                    low = candlestickData.low + add,
                    close = candlestickData.close + add
                ))
            }
            newData
        }
    }

    suspend fun getYearLineData(): MutableList<SeriesData> {
        return withContext(Dispatchers.IO) {
            val data = listRealTimeEmulationSeriesCandlestickData()
            val newData = mutableListOf<SeriesData>()
            val timestamp = 1263096000L
            val subList = data.subList(0, 12)
            for (i in 0 until subList.size) {
                val add = if (i % 8 == 0) 1 else 0
                val candlestickData = subList[i] as CandlestickData
                newData.add(CandlestickData(
                    time = Time.Utc(timestamp + i * 12 * 30 * 24 * 60 * 60L),
                    open = candlestickData.open + add,
                    high = candlestickData.high + add,
                    low = candlestickData.low + add,
                    close = candlestickData.close + add
                ))
            }
            newData
        }
    }
}