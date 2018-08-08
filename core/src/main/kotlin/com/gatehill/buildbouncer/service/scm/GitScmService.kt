package com.gatehill.buildbouncer.service.scm

import com.gatehill.buildbouncer.config.Settings
import com.gatehill.buildbouncer.service.CommandExecutorService
import org.apache.logging.log4j.LogManager
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryState
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class GitScmService(
    private val commandExecutorService: CommandExecutorService
) : ScmService {
    private val logger = LogManager.getLogger(ScmService::class.java)

    override fun revertCommit(commit: String, branchName: String) {
        logger.info("Reverting commit $commit in branch $branchName")

        withRepo {
            Git(this).use { git ->
                cleanCheckout(git, branchName)
                revertCommit(git, commit)

                if (repositoryState != RepositoryState.SAFE) {
                    throw IllegalStateException("Repository state is: $repositoryState")
                }

                if (Settings.Repository.pushChanges) {
                    logger.info("Pushing changes to remote")
                    git.push().call()
                } else {
                    logger.info("Skipped pushing changes to remote")
                }
            }
        }
    }

    override fun lockBranch(branchName: String) {
        TODO("locking branches is not implemented")
    }

    private fun cleanCheckout(git: Git, branchName: String) {
        logger.info("Performing clean checkout of branch $branchName")

        git.clean()
            .setCleanDirectories(true)
            .setForce(true)
            .call()

        git.fetch().call()

        git.checkout()
            .setName(branchName)
            .call()

        git.pull()

        if (git.repository.repositoryState != RepositoryState.SAFE) {
            throw IllegalStateException("Repository state is: ${git.repository.repositoryState}")
        }
    }

    private fun revertCommit(git: Git, commit: String) {
        val ref = git.repository.findRef(commit)
        val revCommit = git.repository.parseCommit(ref.objectId)

        if (revCommit.parentCount == 1) {
            git.revert().include(revCommit).call()
        } else {
            // jgit doesn't support reverting commits with multiple parents (e.g. merge commits)
            commandExecutorService.exec(
                command = "git revert $commit --mainline 1",
                workingDir = Settings.Repository.localDir
            )
        }
    }

    private fun withRepo(block: Repository.() -> Unit) {
        FileRepositoryBuilder()
            .setGitDir(Settings.Repository.localDir)
            .build()
            .use(block)
    }
}
