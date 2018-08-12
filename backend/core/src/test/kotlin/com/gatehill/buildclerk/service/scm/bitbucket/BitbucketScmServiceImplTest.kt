package com.gatehill.buildclerk.service.scm.bitbucket

import com.gatehill.buildclerk.service.CommandExecutorService
import org.junit.Test

/**
 * Tests for Bitbucket SCM implementation.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class BitbucketScmServiceImplTest {
    @Test
    fun lockBranch() {
        val service = BitbucketScmServiceImpl(
                CommandExecutorService(),
                BitbucketApiClientBuilder(),
                BitbucketOperationsService()
        )
        service.lockBranch("test")
    }
}
