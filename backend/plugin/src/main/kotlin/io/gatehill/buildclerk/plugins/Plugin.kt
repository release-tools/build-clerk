package io.gatehill.buildclerk.plugins

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Plugin(
    val name: String
)
