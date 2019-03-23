package io.gatehill.buildclerk.api.model.pr

/**
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
data class SourceFile(
    val path: String,
    val changeType: FileChangeType
)

enum class FileChangeType {
    ADDED,
    MODIFIED,
    DELETED
}
