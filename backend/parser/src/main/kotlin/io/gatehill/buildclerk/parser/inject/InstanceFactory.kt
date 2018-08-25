package io.gatehill.buildclerk.parser.inject

interface InstanceFactory {
    fun <T : Any> instance(clazz: Class<T>): T
}
