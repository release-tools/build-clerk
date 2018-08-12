package com.gatehill.buildclerk.service

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class CommandExecutorService {
    fun exec(command: String, workingDir: File): String {
        return try {
            val parts = command.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                    .directory(workingDir)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .start()

            proc.waitFor(5, TimeUnit.MINUTES)
            proc.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            throw RuntimeException("Error executing command: $command", e)
        }
    }
}
