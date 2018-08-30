package io.gatehill.buildclerk.api.model

import io.gatehill.buildclerk.api.util.Color

data class BuildSummary(
    val message: String,
    val color: Color
)
