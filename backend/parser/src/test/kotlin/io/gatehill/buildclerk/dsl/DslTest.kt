package io.gatehill.buildclerk.dsl

import io.gatehill.buildclerk.parser.Parser
import io.gatehill.buildclerk.parser.config.ParserSettings
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.nio.file.Paths

class DslTest {
    @Test
    fun `parse configuration`() {
        val rules = Paths.get(DslTest::class.java.getResource("/full.kts").toURI()).toString()

        val parserSettings = ParserSettings(
            mapOf(
                "RULES_FILE" to rules,
                "RULES_PARSE_ON_STARTUP" to "false"
            )
        )

        val config = Parser(parserSettings).parse()
        config.body(config)

        assertNotNull(config.bodyHolder.buildPassed)
        assertNotNull(config.bodyHolder.buildFailed)
        assertNotNull(config.bodyHolder.branchStartsPassing)
        assertNotNull(config.bodyHolder.branchStartsFailing)
        assertNotNull(config.bodyHolder.pullRequestModified)
        assertNotNull(config.bodyHolder.pullRequestMerged)
        assertNotNull(config.bodyHolder.repository)
    }
}
