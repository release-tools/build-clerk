package io.gatehill.buildclerk.plugins

/**
 * @author pete
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Plugin(
    val name: String
)
