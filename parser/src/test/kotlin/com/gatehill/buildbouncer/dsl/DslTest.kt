package com.gatehill.buildbouncer.dsl

import com.gatehill.buildbouncer.api.model.Analysis
import com.gatehill.buildbouncer.api.model.BuildDetails
import com.gatehill.buildbouncer.api.model.BuildOutcome
import com.gatehill.buildbouncer.api.model.BuildStatus
import com.gatehill.buildbouncer.api.model.Scm
import com.gatehill.buildbouncer.parser.Parser
import org.apache.logging.log4j.LogManager
import org.junit.Test
import java.nio.file.Paths

class DslTest {
    @Test
    fun testConfig() {
        val rules = Paths.get(DslTest::class.java.getResource("/simple.kts").toURI())

        val outcome = BuildOutcome(
            name = "example",
            url = "job/example",
            build = BuildDetails(
                number = 1,
                status = BuildStatus.FAILED,
                fullUrl = "http://example.com/job/example/1",
                scm = Scm(
                    branch = "master",
                    commit = "c0ff33"
                )
            )
        )

        val analysis = Analysis("Test", LogManager.getLogger("Test"))
        Parser().parse(rules, outcome, analysis)
    }
}
