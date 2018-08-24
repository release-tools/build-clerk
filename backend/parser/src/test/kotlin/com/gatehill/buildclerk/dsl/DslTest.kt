package com.gatehill.buildclerk.dsl

import com.gatehill.buildclerk.parser.Parser
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.nio.file.Paths

class DslTest {
    @Test
    fun `parse configuration`() {
        val rules = Paths.get(DslTest::class.java.getResource("/full.kts").toURI())
        val config = Parser().parse(rules)
        config.body(config)

        assertNotNull(config.bodyHolder.buildPassed)
        assertNotNull(config.bodyHolder.buildFailed)
        assertNotNull(config.bodyHolder.branchStartsPassing)
        assertNotNull(config.bodyHolder.branchStartsFailing)
        assertNotNull(config.bodyHolder.pullRequestMerged)
        assertNotNull(config.bodyHolder.repository)
    }
}
