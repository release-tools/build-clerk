package com.gatehill.buildclerk.api.util

class Color(val hexCode: String) {
    companion object {
        fun of(hexCode: String) = Color(hexCode)
        val BLACK = of("#000000")
        val RED = of("#ff0000")
        val GREEN = of("#00ff00")
    }
}