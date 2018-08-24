package com.gatehill.buildclerk.api.util

/**
 * Shorten a commit hash for display.
 */
fun toShortCommit(commitHash: String) = if (commitHash.length > 8) {
    commitHash.substring(0, 8)
} else {
    commitHash
}
