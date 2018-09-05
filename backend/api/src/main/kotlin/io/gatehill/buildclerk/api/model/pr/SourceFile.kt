package io.gatehill.buildclerk.api.model.pr

/**
 *
 * @author pete
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
