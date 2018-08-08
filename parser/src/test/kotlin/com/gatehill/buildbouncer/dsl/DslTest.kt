package com.gatehill.buildbouncer.dsl

import com.gatehill.buildbouncer.parser.Parser
import org.junit.Assert
import org.junit.Test
import java.nio.file.Paths

class DslTest {
    @Test
    fun testConfig() {
        val rules = Paths.get(DslTest::class.java.getResource("/simple.kts").toURI())
        val config = Parser().parse(rules)
        config.body(config)

        Assert.assertNotNull(config.bodyHolder.buildPassed)
        Assert.assertNotNull(config.bodyHolder.buildFailed)
        Assert.assertNotNull(config.bodyHolder.branchStartsPassing)
        Assert.assertNotNull(config.bodyHolder.branchStartsFailing)
        Assert.assertNotNull(config.bodyHolder.repository)
    }
}
