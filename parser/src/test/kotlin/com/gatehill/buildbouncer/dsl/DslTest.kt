package com.gatehill.buildbouncer.dsl

import org.junit.Test
import java.nio.file.Paths

class DslTest {
    @Test
    private fun testConfig() {
        val rules = Paths.get(DslTest::class.java.getResource("/simple.kts").toURI())

        // TODO
//        Parser().parse(rules)
    }
}
