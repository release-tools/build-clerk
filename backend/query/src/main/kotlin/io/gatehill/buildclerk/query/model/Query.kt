package io.gatehill.buildclerk.query.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Query(
    val query: String,
    val variables: String?
)
