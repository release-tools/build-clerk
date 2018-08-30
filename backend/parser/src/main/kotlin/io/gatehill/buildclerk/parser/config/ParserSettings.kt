package io.gatehill.buildclerk.parser.config

import io.gatehill.buildclerk.api.config.EnvironmentSettings
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Settings for configuration rules parser.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class ParserSettings(env: Map<String, String>? = null) : EnvironmentSettings(env) {
    val configFile: Path by lazy {
        getenv("RULES_FILE")?.let { Paths.get(it) }
            ?: throw IllegalStateException("Missing rules file")
    }

    val parseOnStartup: Boolean by lazy { getenv("RULES_PARSE_ON_STARTUP")?.toBoolean() != false }
}
