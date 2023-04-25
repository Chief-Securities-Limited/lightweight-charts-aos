package com.tradingview.lightweightcharts.example.app.view.pager

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.PriceScaleOptions
import com.tradingview.lightweightcharts.api.options.models.priceLineOptions
import com.tradingview.lightweightcharts.example.app.R
import com.tradingview.lightweightcharts.example.app.viewmodel.MockDataViewModel
import com.tradingview.lightweightcharts.example.app.viewmodel.RealTimeEmulationViewModel
import com.tradingview.lightweightcharts.example.app.viewmodel.ViewPagerViewModel
import com.tradingview.lightweightcharts.view.ChartsView
import kotlinx.android.synthetic.main.activity_view_pager.*
import kotlinx.android.synthetic.main.fragment_charts.view.*
import kotlinx.coroutines.flow.collect

class ViewPagerActivity : AppCompatActivity() {

    class ChartsViewHolder(
        private val view: View,
        private val activity: AppCompatActivity
    ): RecyclerView.ViewHolder(view) {
        private val viewModel: RealTimeEmulationViewModel =
            ViewModelProvider(activity).get(RealTimeEmulationViewModel::class.java)

        private lateinit var series: SeriesApi

        fun bind() {
            view.charts_view.addTouchDelegate(NestedScrollDelegate(activity))
            view.charts_view.api.applyOptions {
                rightPriceScale = PriceScaleOptions(
                    borderVisible = false,
                    visible = false
                )
            }
            view.charts_view.subscribeOnChartStateChange { state ->
                //Do not add new series when ViewHolder is rebinding
                if (state is ChartsView.State.Ready && ::series.isInitialized.not()) {
//                    viewModel.seriesData.observe(activity) { data ->
//                        view.charts_view.api.addAreaSeries { areaSeries ->
//                            series = areaSeries
//                            series.setData(data.list)
//                            view.charts_view.api.timeScale.fitContent()
//                        }
//                    }

                    viewModel.seriesData.observe(activity) { data ->
                        view.charts_view.api.addCandlestickSeries(
                            options = CandlestickSeriesOptions(),
                            onSeriesCreated = { series ->
                                series.setData(data.list)
                                activity.lifecycleScope.launchWhenResumed {
                                    viewModel.seriesFlow.collect(series::update)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_view_pager)
        view_pager.layoutManager = LinearLayoutManager(this)
        view_pager.adapter = object : RecyclerView.Adapter<ChartsViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ChartsViewHolder {
                val layout = layoutInflater.inflate(R.layout.fragment_charts, parent, false)
                return ChartsViewHolder(layout, this@ViewPagerActivity)
            }

            override fun getItemViewType(position: Int): Int {
                //We should hold the instance of ChartsView as long as possible
                //Every page will create its own ChartsView
                return position
            }

            override fun onBindViewHolder(holder: ChartsViewHolder, position: Int) {
                holder.bind()
            }

            override fun getItemCount(): Int = 30
        }

//        TabLayoutMediator(tab_layout, view_pager) { tab, position ->
//            tab.text = "Chart ${position + 1}"
//            view_pager.setCurrentItem(tab.position, true)
//        }.attach()
    }
}