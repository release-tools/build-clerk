package io.gatehill.buildclerk.plugins

import java.util.ServiceLoader

object PluginLocator {
    /**
     * Use the ServiceLoader to find the given implementation.
     **/
    inline fun <reified T : Any> findImplementation(implName: String): T {
        val loader = ServiceLoader.load(T::class.java)
        val implClass = loader.find { implName == it::class.java.getAnnotation(Plugin::class.java)?.name }

        return implClass ?: throw IllegalStateException(
            "No implementation of ${T::class.java.canonicalName} found with ${Plugin::class.java.canonicalName} annotation named '$implName'"
        )
    }
}
