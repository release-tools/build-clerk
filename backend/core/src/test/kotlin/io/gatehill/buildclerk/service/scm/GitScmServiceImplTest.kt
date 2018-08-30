package io.gatehill.buildclerk.service.scm

import com.nhaarman.mockitokotlin2.mock
import io.gatehill.buildclerk.api.config.Settings
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * Tests for `GitScmServiceImpl`.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class GitScmServiceImplTest {
    @Test
    fun `clone HTTPS repo`() {
        val localDir = Files.createTempDirectory("bare").toFile().apply { deleteOnExit() }

        val settings = Settings.Repository(
            mapOf(
                "GIT_REPO_LOCAL_DIR" to localDir.toString(),
                "GIT_REPO_REMOTE_URL" to "https://github.com/outofcoffee/build-clerk.git"
            )
        )

        cloneAndVerifyLocal(settings, localDir)
    }

    @Test
    fun `clone SSH repo`() {
        val localDir = Files.createTempDirectory("bare").toFile().apply { deleteOnExit() }

        val settings = Settings.Repository(
            mapOf(
                "GIT_REPO_LOCAL_DIR" to localDir.toString(),
                "GIT_REPO_REMOTE_URL" to "git@github.com:outofcoffee/build-clerk.git"
            )
        )

        cloneAndVerifyLocal(settings, localDir)
    }

    private fun cloneAndVerifyLocal(settings: Settings.Repository, localDir: File) {
        val service = GitScmServiceImpl(settings, mock())
        service.clone()

        assertTrue("Local repo should exist", localDir.exists())
        assertTrue("Bare repo should be populated", localDir.list().isNotEmpty())
    }
}
