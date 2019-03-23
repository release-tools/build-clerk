package io.gatehill.buildclerk.api.model

data class BranchNotification(
    val userId: String,
    val channel: String,
    val branch: String
)
