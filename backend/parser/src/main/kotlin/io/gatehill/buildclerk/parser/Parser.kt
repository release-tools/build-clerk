package io.gatehill.buildclerk.parser

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.gatehill.buildclerk.dsl.AbstractBaseBlock
import io.gatehill.buildclerk.dsl.ConfigBlock
import io.gatehill.buildclerk.parser.inject.InstanceFactoryLocator
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import javax.script.ScriptEngineManager

/**
 * Parses a config file.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class Parser {
    private val logger: Logger = LogManager.getLogger(Parser::class.java)
    private val engine by lazy { ScriptEngineManager().getEngineByExtension("kts")!! }

    private val configCache: Cache<Path, ConfigBlock> = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build()

    fun parse(rulesFile: Path): ConfigBlock {
        val config = configCache.get(rulesFile) { path ->
            logger.debug("Loading configuration from rules file: $path")
            engine.eval(path.toFile().reader()) as? ConfigBlock

        } ?: throw IllegalStateException("No 'config' block defined in: $rulesFile")

        config.scheduledTasks.clear()
        config.body(config)
        return config
    }

    /**
     * Instantiate the block of type `B`, configure it, then invoke the `body` on it.
     */
    inline fun <reified B : AbstractBaseBlock> invoke(
        noinline blockConfigurer: ((B) -> Unit)? = null,
        noinline body: (B.() -> Unit)?
    ) {
        body?.let {
            val block = InstanceFactoryLocator.instance<B>()
            blockConfigurer?.let { configurer -> configurer(block) }
            block.body()
        }
    }
}
