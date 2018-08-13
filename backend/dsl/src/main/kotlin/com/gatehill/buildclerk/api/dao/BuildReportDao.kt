package com.gatehill.buildclerk.api.dao

import com.gatehill.buildclerk.api.model.BuildReport

/**
 * Stores build reports and provides access to build metadata.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface BuildReportDao {
    fun readStore(): MutableList<BuildReport>
}
