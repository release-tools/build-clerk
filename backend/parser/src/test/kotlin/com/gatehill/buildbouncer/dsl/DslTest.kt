package com.gatehill.buildbouncer.dsl

import com.gatehill.buildbouncer.parser.Parser
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Paths

class DslTest {
    @Test
    fun testConfig() {
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
