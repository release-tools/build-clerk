package com.gatehill.buildclerk.dao.inmem

import com.gatehill.buildclerk.api.dao.BuildReportDao
import com.gatehill.buildclerk.api.model.BuildReport

class InMemoryBuildReportDaoImpl : BuildReportDao {
    private val store = mutableListOf<BuildReport>()

    override fun readStore(): MutableList<BuildReport> = store
}
