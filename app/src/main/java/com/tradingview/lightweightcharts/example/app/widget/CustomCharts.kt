package com.tradingview.lightweightcharts.example.app.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.tradingview.lightweightcharts.view.ChartsView

class CustomCharts @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ChartsView(context, attrs, defStyleAttr, defStyleRes) {

    private var chartsView: CustomCharts? = null

    fun setReferenceCharts(chartsView: CustomCharts) {
        this.chartsView = chartsView
    }

    fun receiveOutsideTouchEvent(ev: MotionEvent, viewWidth: Int, viewHeight: Int) {
        val motionEvent = MotionEvent.obtain(ev)
        motionEvent.setLocation(ev.x * width / viewWidth, ev.y * height / viewHeight)
        super.dispatchTouchEvent(motionEvent)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        chartsView?.receiveOutsideTouchEvent(ev, width, height)
        return super.dispatchTouchEvent(ev)
    }

}