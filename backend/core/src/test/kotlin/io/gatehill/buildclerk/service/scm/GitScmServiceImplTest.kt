package io.gatehill.buildclerk.service.scm

import com.nhaarman.mockitokotlin2.mock
import io.gatehill.buildclerk.api.config.Settings
import io.gatehill.buildclerk.api.model.pr.FileChangeType
import io.gatehill.buildclerk.service.CommandExecutorService
import io.gatehill.buildclerk.service.scm.bitbucket.BitbucketApiClientBuilder
import io.gatehill.buildclerk.service.scm.bitbucket.BitbucketOperationsService
import io.gatehill.buildclerk.service.scm.bitbucket.BitbucketScmServiceImpl
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.junit.Assert
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
                "GIT_REPO_REMOTE_URL" to "https://github.com/release-tools/build-clerk.git"
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
                "GIT_REPO_REMOTE_URL" to "git@github.com:release-tools/build-clerk.git"
            )
        )

        cloneAndVerifyLocal(settings, localDir)
    }
    /**
     * Test a branch diff using two short commit hashes.
     */
    @Test
    fun `diff files between branches`() = withTempRepo { git ->
        // on master
        createAndCommitFile(git, "test1.txt", "First change")
        val masterHead = resolve("HEAD").name.substring(0, 10)

        git.checkout()
            .setName("feature/1")
            .setCreateBranch(true)
            .call()

        createAndCommitFile(git, "test2.txt", "Second change")
        val featureBranchHead = resolve("HEAD").name.substring(0, 10)

        val service = BitbucketScmServiceImpl(
            Settings.Repository(
                mapOf(
                    "GIT_REPO_LOCAL_DIR" to directory.toString()
                )
            ),
            CommandExecutorService(),
            BitbucketApiClientBuilder(),
            BitbucketOperationsService(mock())
        )

        val modifiedFiles = service.listModifiedFiles(
            oldCommit = masterHead,
            newCommit = featureBranchHead
        )

        Assert.assertEquals(1, modifiedFiles.size)

        val modifiedFile = modifiedFiles.first()
        Assert.assertEquals("test2.txt", modifiedFile.path)
        Assert.assertEquals(FileChangeType.ADDED, modifiedFile.changeType)
    }

    private fun withTempRepo(block: Repository.(git: Git) -> Unit) {
        val localDir = Files.createTempDirectory("gitrepo").toFile()
        localDir.deleteOnExit()

        Git.init().setDirectory(localDir).call().use { git ->
            block(git.repository, git)
        }
    }

    private fun createAndCommitFile(
        git: Git,
        fileName: String,
        content: String
    ) {
        val file = File(git.repository.workTree, fileName)
        file.writeText(content)

        git.add()
            .addFilepattern(fileName)
            .call()

        git.commit()
            .setAuthor("Build Clerk", "buildclerk@example.com")
            .setMessage("Committing test file $fileName")
            .call()
    }

    private fun cloneAndVerifyLocal(settings: Settings.Repository, localDir: File) {
        val service = GitScmServiceImpl(settings, mock())
        service.clone()

        assertTrue("Local repo should exist", localDir.exists())
        assertTrue("Bare repo should be populated", localDir.list().isNotEmpty())
    }
}
