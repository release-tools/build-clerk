package com.gatehill.buildclerk.parser

import com.gatehill.buildclerk.dsl.ConfigBlock
import java.nio.file.Path
import javax.script.ScriptEngineManager

/**
 * Parses a config file.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Parser {
    private val engine by lazy { ScriptEngineManager().getEngineByExtension("kts")!! }

    fun parse(rulesFile: Path): ConfigBlock {
        val config = engine.eval(rulesFile.toFile().reader()) as? ConfigBlock
                ?: throw IllegalStateException("No configuration defined")

        config.body(config)
        return config
    }
}
