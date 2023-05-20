package io.gatehill.buildclerk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val supervisedDefaultCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
