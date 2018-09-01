package io.gatehill.buildclerk.query.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 *
 * @author pete
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Query(
    val query: String,
    val variables: String?
)
