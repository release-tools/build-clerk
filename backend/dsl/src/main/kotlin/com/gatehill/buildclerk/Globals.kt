package com.gatehill.buildclerk

/**
 * Shorten a commit hash for display.
 */
fun toShortCommit(commitHash: String) = commitHash.substring(0, 8)
