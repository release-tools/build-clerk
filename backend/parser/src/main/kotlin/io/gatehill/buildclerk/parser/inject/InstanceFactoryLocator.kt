package io.gatehill.buildclerk.parser.inject

object InstanceFactoryLocator {
    lateinit var instanceFactory: InstanceFactory

    inline fun <reified T : Any> instance() = instanceFactory.instance(T::class.java)
}
