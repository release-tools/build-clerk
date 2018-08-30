package io.gatehill.buildclerk.api.service

import io.gatehill.buildclerk.api.model.BuildSummary

interface BuildSummaryService {
    fun summarise(branchName: String): BuildSummary
}
