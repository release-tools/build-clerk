package com.gatehill.buildbouncer.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val jsonMapper by lazy { ObjectMapper().registerKotlinModule() }
