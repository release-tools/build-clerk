package io.gatehill.buildclerk.api

import java.time.ZonedDateTime

/**
 * Represents a dated recordable entity.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
interface Recorded {
    val count: Int
    val oldestDate: ZonedDateTime?
    val newestDate: ZonedDateTime?
}
