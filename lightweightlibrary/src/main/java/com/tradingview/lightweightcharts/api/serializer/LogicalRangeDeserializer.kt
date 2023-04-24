package com.tradingview.lightweightcharts.api.serializer

import com.google.gson.JsonElement
import com.tradingview.lightweightcharts.api.series.models.LogicalRange

class LogicalRangeDeserializer : Deserializer<LogicalRange>() {
    override fun deserialize(json: JsonElement): LogicalRange {
        val from = json.asJsonObject["from"].asFloat
        val to = json.asJsonObject["to"].asFloat
        return LogicalRange(from, to)
    }
}