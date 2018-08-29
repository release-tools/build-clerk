package io.gatehill.buildclerk.parser.inject

object InstanceFactoryLocator {
    lateinit var instanceFactory: InstanceFactory

    fun <T : Any> instance(clazz: Class<T>) = instanceFactory.instance(clazz)

    inline fun <reified T : Any> instance() = instance(T::class.java)
}
