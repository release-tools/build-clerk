package io.gatehill.buildclerk.api.model.message

data class MessageAction(
    val name: String? = null,
    val value: String? = null,
    val type: String? = null,
    val text: String? = null,
    val style: String? = null
)
