package com.gatehill.buildbouncer.parser.inject

object InstanceFactoryLocator {
    lateinit var instanceFactory: InstanceFactory

    inline fun <reified T : Any> instance() = instanceFactory.instance(T::class.java)
}
