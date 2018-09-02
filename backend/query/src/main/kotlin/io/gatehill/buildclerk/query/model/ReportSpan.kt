package io.gatehill.buildclerk.query.model

data class ReportSpan(
    val dataPoints: Int,
    val successful: Int,
    val failed: Int,
    val passRate: Double
)
