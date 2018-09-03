package io.gatehill.buildclerk.api.util

/**
 * Shorten a commit hash for display.
 */
fun toShortCommit(commitHash: String) = shortenCommit(commitHash, 8)

fun shortenCommit(commitHash: String, length: Int): String {
    return if (commitHash.length > length) {
        commitHash.substring(0, length)
    } else {
        commitHash
    }
}
