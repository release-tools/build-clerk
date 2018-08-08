package com.gatehill.buildbouncer

object InstanceFactoryLocator {
    lateinit var instanceFactory: InstanceFactory

    inline fun <reified T : Any> instance() = instanceFactory.instance(T::class.java)
}

interface InstanceFactory {
    fun <T : Any> instance(clazz: Class<T>): T
}
