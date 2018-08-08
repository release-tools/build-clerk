package com.gatehill.buildbouncer.parser.inject

interface InstanceFactory {
    fun <T : Any> instance(clazz: Class<T>): T
}
