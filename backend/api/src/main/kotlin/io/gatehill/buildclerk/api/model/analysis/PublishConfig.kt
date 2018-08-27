package io.gatehill.buildclerk.api.model.analysis

import io.gatehill.buildclerk.api.util.Color

data class PublishConfig(
    val channelName: String,
    val color: Color
)
