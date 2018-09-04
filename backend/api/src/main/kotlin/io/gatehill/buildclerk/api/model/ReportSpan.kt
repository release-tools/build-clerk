package io.gatehill.buildclerk.api.model

data class ReportSpan(
    val dataPoints: Int,
    val successful: Int,
    val failed: Int,
    val passRate: Double
)
