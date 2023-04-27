package com.tradingview.lightweightcharts.example.app.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.lifecycle.ViewModelProvider
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.options.enums.TrackingModeExitMode
import com.tradingview.lightweightcharts.api.options.models.ChartOptions
import com.tradingview.lightweightcharts.api.options.models.CrosshairLineOptions
import com.tradingview.lightweightcharts.api.options.models.CrosshairOptions
import com.tradingview.lightweightcharts.api.options.models.PriceScaleOptions
import com.tradingview.lightweightcharts.api.options.models.TrackingModeOptions
import com.tradingview.lightweightcharts.api.options.models.priceScaleMargins
import com.tradingview.lightweightcharts.api.options.models.timeScaleOptions
import com.tradingview.lightweightcharts.api.series.enums.CrosshairMode
import com.tradingview.lightweightcharts.api.series.enums.LineStyle
import com.tradingview.lightweightcharts.api.series.enums.SeriesMarkerPosition
import com.tradingview.lightweightcharts.api.series.enums.SeriesMarkerShape
import com.tradingview.lightweightcharts.api.series.models.CandlestickData
import com.tradingview.lightweightcharts.api.series.models.HistogramData
import com.tradingview.lightweightcharts.api.series.models.MouseEventParams
import com.tradingview.lightweightcharts.api.series.models.SeriesMarker
import com.tradingview.lightweightcharts.api.series.models.Time
import com.tradingview.lightweightcharts.example.app.R
import com.tradingview.lightweightcharts.example.app.extension.setupSeries
import com.tradingview.lightweightcharts.example.app.model.KLineGroupType
import com.tradingview.lightweightcharts.example.app.model.KLineTimeType
import com.tradingview.lightweightcharts.example.app.model.MOCK_CHART_TYPE_LIST
import com.tradingview.lightweightcharts.example.app.model.MockChartType
import com.tradingview.lightweightcharts.example.app.view.pager.NestedScrollDelegate
import com.tradingview.lightweightcharts.example.app.viewmodel.MockDataViewModel
import com.tradingview.lightweightcharts.example.app.viewmodel.MockDataViewModel.Companion.K_LINE_MAIN_SERIES
import com.tradingview.lightweightcharts.example.app.viewmodel.MockDataViewModel.Companion.MAVOL_SERIES
import com.tradingview.lightweightcharts.example.app.widget.ChartsToolsLayout
import com.tradingview.lightweightcharts.example.app.widget.ChartsViewSyncHelper
import com.tradingview.lightweightcharts.example.app.widget.SyncCrosshairMoveListener
import com.tradingview.lightweightcharts.view.ChartsView
import kotlinx.android.synthetic.main.activity_mock_data_charts.btn_fullscreen
import kotlinx.android.synthetic.main.activity_mock_data_charts.charts_tools_layout_1
import kotlinx.android.synthetic.main.activity_mock_data_charts.charts_tools_layout_2
import kotlinx.android.synthetic.main.activity_mock_data_charts.charts_tools_layout_3
import kotlinx.android.synthetic.main.activity_mock_data_charts.charts_view_1
import kotlinx.android.synthetic.main.activity_mock_data_charts.charts_view_2
import kotlinx.android.synthetic.main.activity_mock_data_charts.charts_view_3
import kotlinx.android.synthetic.main.activity_mock_data_charts.rg_k_line
import kotlinx.android.synthetic.main.activity_mock_data_charts.rg_k_line_group_type
import kotlinx.android.synthetic.main.activity_mock_data_charts.toolbar
import kotlinx.android.synthetic.main.activity_mock_data_charts.tv_charts_view_1_y_axis_average
import kotlinx.android.synthetic.main.activity_mock_data_charts.tv_charts_view_1_y_axis_max
import kotlinx.android.synthetic.main.activity_mock_data_charts.tv_charts_view_1_y_axis_min
import kotlinx.android.synthetic.main.activity_mock_data_charts.tv_charts_view_2_y_axis_max
import kotlinx.android.synthetic.main.activity_mock_data_charts.tv_cross_plank


class MockDataChartsActivity : AppCompatActivity() {

    private lateinit var viewModel: MockDataViewModel

    private val chartsViewSyncHelper = ChartsViewSyncHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val params = window.attributes
            params.flags = params.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            window.attributes = params
        }

        setContentView(R.layout.activity_mock_data_charts)
        viewModel = ViewModelProvider(this)[MockDataViewModel::class.java]
        setupUi()
        setupObserve()
    }

    override fun onDestroy() {
        MOCK_CHART_TYPE_LIST.forEach { type ->
            val chartModel = viewModel.getChartModel(type)
            chartModel.getAll().forEach {
                it.seriesApi = null
            }
        }
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun setupUi() {

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            toolbar.isVisible = false
            btn_fullscreen.text = "退出全屏"
        } else {
            toolbar.isVisible = true
            btn_fullscreen.text = "全屏"
        }

        val chartOptions = getChartOptions()
        MOCK_CHART_TYPE_LIST.forEach { type ->
            val chartsView = findChartsView(type)
            chartsView.api.applyOptions(chartOptions)
            chartsView.api.timeScale.fitContent()
            chartsView.addTouchDelegate(NestedScrollDelegate(this))

            chartsView.subscribeOnChartStateChange {
                if (it is ChartsView.State.Ready) {
                    chartsViewSyncHelper.addChartsView(findChartsViewLayout(type))
                }
            }
        }

        rg_k_line.setOnCheckedChangeListener { group, checkedId ->
            val type = when (checkedId) {
                R.id.rb_minute_time -> KLineTimeType.MINUTE_TIME
                R.id.rb_five_day -> KLineTimeType.FIVE_DAY
                R.id.rb_day_k_line -> KLineTimeType.DAY_K_LINE
                R.id.rb_week_k_line -> KLineTimeType.WEEK_K_LINE
                R.id.rb_month_k_line -> KLineTimeType.MONTH_K_LINE
                R.id.rb_year_k_line -> KLineTimeType.YEAR_K_LINE
                else -> null
            }

            type?.also {
                viewModel.selectKlineTimeType(it)
            }
        }

        charts_tools_layout_1.setSetMarkersAdapter(object : ChartsToolsLayout.MarkersAdapter {
            val cacheViews = mutableListOf<View>()
            override fun getMarkerView(time: Time): View {
                if (cacheViews.isNotEmpty()) {
                    return cacheViews.removeFirst()
                }
                return View(this@MockDataChartsActivity).apply {
                    this.setBackgroundResource(R.mipmap.ic_launcher_round)
                }
            }

            override fun releaseMarkerView(time: Time, view: View) {
                cacheViews.add(view)
            }
        })

        chartsViewSyncHelper.setSyncCrosshairMoveListener(object : SyncCrosshairMoveListener {

            private var lastCrosshairTime: Long? = -1L

            override fun onSyncCrosshairMove(params: MouseEventParams) {
                tv_cross_plank.isVisible = params.time != null

                if (params.time?.date?.time == lastCrosshairTime) {
                    return
                }
                lastCrosshairTime = params.time?.date?.time
                params.time?.also { time ->
                    val chartModel = viewModel.getChartModel(MockChartType.K_LINE)
                    val seriesData = chartModel.get(K_LINE_MAIN_SERIES)?.findSeriesData(time)
                    if (seriesData is CandlestickData) {
                        val text = "開盤：${seriesData.open} 收盤：${seriesData.close} 最高：${seriesData.high}  最低：${seriesData.low}"
                        tv_cross_plank.text = text
                    }

                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (vibrator.hasVibrator()) {
                        if (vibrator.hasVibrator()) {
                            val pattern = longArrayOf(0, 30)
                            vibrator.vibrate(pattern, -1)
                        }
                    }
                }
            }
        })


        rg_k_line_group_type.setOnCheckedChangeListener { group, checkedId ->
            val type = when (checkedId) {
                R.id.rb_k_line_group_type_only -> KLineGroupType.ONLY_K_LINE
                R.id.rb_k_line_group_type_ema -> KLineGroupType.EMA
                else -> null
            }
            type?.also {
                viewModel.selectKLineGroupType(it)
            }
        }

        charts_view_1.api.timeScale.subscribeVisibleTimeRangeChange { timeRange ->
            timeRange?.also {
                val chartModel = viewModel.getChartModel(MockChartType.K_LINE)
                chartModel.get(K_LINE_MAIN_SERIES)?.also { chartSeriesModel ->
                    val list = chartSeriesModel.findSeriesData(timeRange)
                    val maxData = list.maxByOrNull { (it as CandlestickData).high }
                    val minData = list.minByOrNull { (it as CandlestickData).low }

                    if (maxData != null && minData != null) {
                        val maxCandlestickData = maxData as CandlestickData
                        val minCandlestickData = minData as CandlestickData
                        tv_charts_view_1_y_axis_max.text = maxCandlestickData.high.toString()
                        tv_charts_view_1_y_axis_min.text = minCandlestickData.low.toString()
                        tv_charts_view_1_y_axis_average.text = ((maxCandlestickData.high + minCandlestickData.low) / 2).toString()

                        val markers = mutableListOf<SeriesMarker>()
                        markers.add(SeriesMarker(
                            time = maxCandlestickData.time,
                            text = maxCandlestickData.high.toString(),
                            position = SeriesMarkerPosition.ABOVE_BAR,
                            shape = SeriesMarkerShape.ARROW_DOWN
                        ))

                        markers.add(SeriesMarker(
                            time = minCandlestickData.time,
                            text = minCandlestickData.low.toString(),
                            position = SeriesMarkerPosition.BELOW_BAR,
                            shape = SeriesMarkerShape.ARROW_UP
                        ))

                        markers.sortBy { it.time?.date?.time }
                        chartSeriesModel.seriesApi?.setMarkers(markers)
                    }
                }
            }
        }

        charts_view_2.api.timeScale.subscribeVisibleTimeRangeChange { timeRange ->
            timeRange?.also {
                val chartModel = viewModel.getChartModel(MockChartType.MAVOL)
                chartModel.get(MAVOL_SERIES)?.also {
                    val list = it.findSeriesData(timeRange)
                    val max = list.maxOfOrNull { (it as HistogramData).value }

                    if (max != null) {
                        tv_charts_view_2_y_axis_max.text = max.toInt().toString()
                    }
                }
            }
        }

        btn_fullscreen.setOnClickListener {
            val orientation = resources.configuration.orientation
            if (orientation != Configuration.ORIENTATION_LANDSCAPE) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }


    }

    private fun setupObserve() {
        charts_view_1.subscribeOnChartStateChange {
            if (it is ChartsView.State.Ready) {
                setupObserveChartsData(MockChartType.K_LINE)
            }
        }

        charts_view_2.subscribeOnChartStateChange {
            if (it is ChartsView.State.Ready) {
                setupObserveChartsData(MockChartType.MAVOL)
            }
        }

        charts_view_3.subscribeOnChartStateChange {
            if (it is ChartsView.State.Ready) {
                setupObserveChartsData(MockChartType.LINE)
            }
        }
    }

    private fun setupObserveChartsData(type: MockChartType) {
        viewModel.observeChartsData(type, this@MockDataChartsActivity) { chartModel ->
            val apiDelegate = findChartsView(type).api
            chartModel.getAll().forEach { chartSeriesModel ->
                apiDelegate.setupSeries(chartSeriesModel)
            }

            val chartsViewLayout = findChartsViewLayout(type)
            val customMarkers = chartModel.getCustomMarkers()
            if (customMarkers.isEmpty()) {
                chartsViewLayout.clearMarkers()
            } else {
                chartsViewLayout.setMarkers(customMarkers)
            }
        }
    }

    private fun findChartsView(type: MockChartType): ChartsView {
        return when (type) {
            MockChartType.K_LINE -> charts_view_1
            MockChartType.MAVOL -> charts_view_2
            MockChartType.LINE -> charts_view_3
        }
    }

    private fun findChartsViewLayout(type: MockChartType): ChartsToolsLayout {
        return when (type) {
            MockChartType.K_LINE -> charts_tools_layout_1
            MockChartType.MAVOL -> charts_tools_layout_2
            MockChartType.LINE -> charts_tools_layout_3
        }
    }

    private fun getChartOptions(): ChartOptions {
        return ChartOptions(
            timeScale = timeScaleOptions {
                fixLeftEdge = true
//                fixRightEdge = true
                lockVisibleTimeRangeOnResize = true
                timeVisible = true
            },
            crosshair = CrosshairOptions(
                mode = CrosshairMode.NORMAL,
                vertLine = CrosshairLineOptions(
                    visible = false
                ),
                horzLine = CrosshairLineOptions(
                    style = LineStyle.SOLID,
                    labelVisible = true,
                    color = resources.getColor(R.color.colorPrimary).toIntColor()
                )
            ),
            leftPriceScale = PriceScaleOptions(
                visible = false
            ),
            rightPriceScale = PriceScaleOptions(
                visible = false
            ),
            trackingMode = TrackingModeOptions(exitMode = TrackingModeExitMode.ON_TOUCH_END)
        )
    }
}