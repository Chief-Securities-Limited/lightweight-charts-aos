package com.tradingview.lightweightcharts.example.app.view.charts

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.options.enums.TrackingModeExitMode
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.ChartOptions
import com.tradingview.lightweightcharts.api.options.models.CrosshairLineOptions
import com.tradingview.lightweightcharts.api.options.models.CrosshairOptions
import com.tradingview.lightweightcharts.api.options.models.HistogramSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.LineSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.PriceScaleMargins
import com.tradingview.lightweightcharts.api.options.models.TrackingModeOptions
import com.tradingview.lightweightcharts.api.options.models.timeScaleOptions
import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.enums.CrosshairMode
import com.tradingview.lightweightcharts.api.series.enums.LastPriceAnimationMode
import com.tradingview.lightweightcharts.api.series.enums.LineWidth
import com.tradingview.lightweightcharts.api.series.enums.SeriesType
import com.tradingview.lightweightcharts.api.series.models.LogicalRange
import com.tradingview.lightweightcharts.api.series.models.PriceFormat
import com.tradingview.lightweightcharts.api.series.models.PriceScaleId
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.example.app.R
import com.tradingview.lightweightcharts.example.app.view.pager.NestedScrollDelegate
import com.tradingview.lightweightcharts.example.app.viewmodel.CustomChartViewModel
import com.tradingview.lightweightcharts.example.app.widget.ChartsToolsLayout
import com.tradingview.lightweightcharts.example.app.widget.ChartsViewSyncHelper
import com.tradingview.lightweightcharts.runtime.messaging.LogLevel
import com.tradingview.lightweightcharts.view.ChartsView
import com.tradingview.lightweightcharts.view.gesture.TouchDelegate
import kotlinx.android.synthetic.main.layout_bar_chart_fragment.charts_view
import kotlinx.android.synthetic.main.layout_chart_fragment.charts_view
import kotlinx.android.synthetic.main.layout_multiple_chart_fragment.btn_chart_title_1
import kotlinx.android.synthetic.main.layout_multiple_chart_fragment.charts_cross_hair_layout_1
import kotlinx.android.synthetic.main.layout_multiple_chart_fragment.charts_cross_hair_layout_2
import kotlinx.android.synthetic.main.layout_multiple_chart_fragment.charts_cross_hair_layout_3
import kotlinx.android.synthetic.main.layout_multiple_chart_fragment.charts_view_1
import kotlinx.android.synthetic.main.layout_multiple_chart_fragment.charts_view_2
import kotlinx.android.synthetic.main.layout_multiple_chart_fragment.charts_view_3
import kotlinx.android.synthetic.main.layout_multiple_chart_fragment.view.charts_view_1

class CustomChartFragment : Fragment() {

    private val TAG = "CustomChartFragment"

    private val viewModelProvider get() = ViewModelProvider(this)
    private lateinit var viewModel: CustomChartViewModel

    private val chartsViewSyncHelper = ChartsViewSyncHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = viewModelProvider[CustomChartViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_multiple_chart_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChartsView()
        observe()
        viewModel.loadData()
    }

    private fun initChartsView() {

        val options = ChartOptions(
            timeScale = timeScaleOptions {
                fixLeftEdge = true
                fixRightEdge = true
                lockVisibleTimeRangeOnResize = true
            },
            crosshair = CrosshairOptions(
                mode = CrosshairMode.NORMAL,
                vertLine = CrosshairLineOptions(
                    visible = false
                )
            ),
            trackingMode = TrackingModeOptions(exitMode = TrackingModeExitMode.ON_TOUCH_END)
        )

        charts_view_1.api.applyOptions(options)
        charts_view_2.api.applyOptions(options)
        charts_view_3.api.applyOptions(options)

        charts_view_1.subscribeOnChartStateChange {
            if (it is ChartsView.State.Ready) {
                chartsViewSyncHelper.addChartsView(charts_cross_hair_layout_1)
            }
        }

        charts_view_2.subscribeOnChartStateChange {
            if (it is ChartsView.State.Ready) {
                chartsViewSyncHelper.addChartsView(charts_cross_hair_layout_2)
            }
        }

        charts_view_3.subscribeOnChartStateChange {
            if (it is ChartsView.State.Ready) {
                chartsViewSyncHelper.addChartsView(charts_cross_hair_layout_3)
            }
        }
    }

    private fun observe() {
        viewModel.chart1SeriesData.observe(viewLifecycleOwner) { data ->
            val apiDelegate = charts_view_1.api
            when (data.type) {
                SeriesType.CANDLESTICK -> {
                    apiDelegate.addCandlestickSeries(
                        options = CandlestickSeriesOptions(),
                        onSeriesCreated = { series ->
                            series.setData(data.list)
                            addMarkers(charts_cross_hair_layout_1, data.list)
                        }
                    )
                }

                else -> {}
            }
        }

        viewModel.chart2SeriesData.observe(viewLifecycleOwner) { data ->
            val apiDelegate = charts_view_2.api
            when (data.type) {
                SeriesType.HISTOGRAM -> {
                    apiDelegate.addHistogramSeries(
                        options = HistogramSeriesOptions(
                            color = Color.parseColor("#26a69a").toIntColor(),
                            priceFormat = PriceFormat.priceFormatBuiltIn(
                                type = PriceFormat.Type.VOLUME,
                                precision = 1,
                                minMove = 1f,
                            ),
                            priceScaleId = PriceScaleId(""),
                            scaleMargins = PriceScaleMargins(
                                top = 0.8f,
                                bottom = 0f,
                            )
                        ),
                        onSeriesCreated = { api ->
                            api.setData(data.list)
                        }
                    )
                }

                else -> {}
            }
        }

        viewModel.chart3SeriesData.observe(viewLifecycleOwner) { data ->
            val apiDelegate = charts_view_3.api
            when (data.type) {
                SeriesType.LINE -> {
                    apiDelegate.addLineSeries(
                        options = LineSeriesOptions(
                            color = Color.rgb(0, 120, 255).toIntColor(),
                            lineWidth = LineWidth.TWO,
                            crosshairMarkerVisible = false,
                            lastValueVisible = false,
                            priceLineVisible = false,
                            lastPriceAnimation = LastPriceAnimationMode.CONTINUOUS
                        ),
                        onSeriesCreated = { api ->
                            api.setData(data.list)
                        }
                    )
                }

                else -> {}
            }
        }
    }

    private fun addMarkers(layout: ChartsToolsLayout, dataList: List<SeriesData>) {
        val markers = mutableListOf<Time>()
//        markers.add(dataList[20].time)
//        markers.add(dataList[21].time)
//        markers.add(dataList[22].time)
//        markers.add(dataList[23].time)
//        markers.add(dataList[26].time)
//        markers.add(dataList[29].time)
//        markers.add(dataList[31].time)
//        markers.add(dataList[33].time)
        markers.add(dataList[36].time)
        layout.setMarkers(markers, object :
            ChartsToolsLayout.MarkersAdapter {

            val cacheViews = mutableListOf<View>()

            override fun getMarkerView(time: Time): View {

                if (cacheViews.isNotEmpty()) {
                    return cacheViews.removeFirst()
                }

                return View(context).apply {
                    this.setBackgroundResource(R.mipmap.ic_launcher_round)
                }
            }

            override fun releaseMarkerView(time: Time, view: View) {
                cacheViews.add(view)
            }
        })
    }

}