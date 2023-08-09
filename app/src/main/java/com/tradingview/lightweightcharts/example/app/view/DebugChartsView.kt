package com.tradingview.lightweightcharts.example.app.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.tradingview.lightweightcharts.runtime.messaging.LogLevel
import com.tradingview.lightweightcharts.view.ChartsView

class DebugChartsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ChartsView(context, attrs, defStyleAttr, defStyleRes) {
    override val logLevel: LogLevel = LogLevel.DEBUG

//    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
//        return super.onInterceptTouchEvent(ev)
//    }

    //    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
//        return super.dispatchTouchEvent(ev)
//    }
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        Log.d("dispatchTouchEvent", "$this")
        if (action == MotionEvent.ACTION_MOVE) {
            // 获取手指在当前控件的位置
            val x = event.x
            val y = event.y

            // 判断手指是否移出当前控件的范围
            if (x < 0F || y < 0F || x > width || y > height) {
                // 手指移出当前控件范围，取消触摸事件
//                super.dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, x, y, 0));
                return false
            }
        }
        // 处理其他触摸事件
        return super.dispatchTouchEvent(event)
    }

}