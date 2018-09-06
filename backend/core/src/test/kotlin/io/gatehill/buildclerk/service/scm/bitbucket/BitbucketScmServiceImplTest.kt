package io.gatehill.buildclerk.service.scm.bitbucket

import com.nhaarman.mockitokotlin2.mock
import io.gatehill.buildclerk.api.config.Settings
import io.gatehill.buildclerk.api.model.pr.FileChangeType
import io.gatehill.buildclerk.service.CommandExecutorService
import io.gatehill.buildclerk.service.support.IntegrationTest
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.experimental.categories.Category
import java.io.File
import java.nio.file.Files

/**
 * Tests for Bitbucket SCM implementation.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Category(IntegrationTest::class)
class BitbucketScmServiceImplTest {
    @Test
    fun lockBranch() {
        val service = BitbucketScmServiceImpl(
            Settings.Repository(),
            CommandExecutorService(),
            BitbucketApiClientBuilder(),
            BitbucketOperationsService(mock())
        )
        service.lockBranch("test")
    }

    @Test
    fun branchDiff() = withTempRepo { git ->
        // on master
        createAndCommitFile(git, "test1.txt", "First change")
        val masterHead = resolve("HEAD").name

        git.checkout()
            .setName("feature/1")
            .setCreateBranch(true)
            .call()

        createAndCommitFile(git, "test2.txt", "Second change")
        val featureBranchHead = resolve("HEAD").name

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

        assertEquals(1, modifiedFiles.size)

        val modifiedFile = modifiedFiles.first()
        assertEquals("test2.txt", modifiedFile.path)
        assertEquals(FileChangeType.ADDED, modifiedFile.changeType)
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
}
