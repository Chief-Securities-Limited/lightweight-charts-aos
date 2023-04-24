package com.tradingview.lightweightcharts.example.app.viewmodel

import android.app.Application
import android.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.enums.SeriesType
import com.tradingview.lightweightcharts.api.series.models.CandlestickData
import com.tradingview.lightweightcharts.api.series.models.HistogramData
import com.tradingview.lightweightcharts.api.series.models.LineData
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.example.app.model.Data
import com.tradingview.lightweightcharts.example.app.repository.StaticRepository
import kotlinx.coroutines.launch
import kotlin.random.Random

class CustomChartViewModel(application: Application) : AndroidViewModel(application) {

    private val staticRepository = StaticRepository()
    private val colorGreen = Color.argb(204, 0, 150, 136).toIntColor()
    private val colorRed = Color.argb(204, 255, 82, 82).toIntColor()

    val chart1SeriesData = MutableLiveData<Data>()
    val chart2SeriesData = MutableLiveData<Data>()
    val chart3SeriesData = MutableLiveData<Data>()

    fun loadData() {
        viewModelScope.launch {
            val barData = staticRepository.getRealTimeEmulationSeriesData().subList(0,50)

            val histogramData = mutableListOf<SeriesData>()
            val lineData = mutableListOf<SeriesData>()
            barData.forEach {
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

            chart1SeriesData.postValue(Data(barData, SeriesType.CANDLESTICK))
            chart2SeriesData.postValue(Data(histogramData, SeriesType.HISTOGRAM))
            chart3SeriesData.postValue(Data(lineData, SeriesType.LINE))
        }
    }

}