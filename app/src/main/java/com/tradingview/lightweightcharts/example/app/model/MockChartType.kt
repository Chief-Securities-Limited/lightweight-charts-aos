package com.tradingview.lightweightcharts.example.app.model

enum class MockChartType {
    K_LINE,
    MAVOL,
    LINE
}

enum class KLineTimeType {
    //    分時, 5日, 日K, 周K, 月K, 年K,
    MINUTE_TIME,
    FIVE_DAY,
    DAY_K_LINE,
    WEEK_K_LINE,
    MONTH_K_LINE,
    YEAR_K_LINE
}

enum class KLineGroupType {
    ONLY_K_LINE,
    EMA
}

val MOCK_CHART_TYPE_LIST = mutableListOf<MockChartType>(
    MockChartType.K_LINE,
    MockChartType.MAVOL,
    MockChartType.LINE,
)