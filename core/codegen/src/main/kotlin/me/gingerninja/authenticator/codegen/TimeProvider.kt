package me.gingerninja.authenticator.codegen

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

interface TimeProvider {
    fun getCurrentTime(): Instant
}

object DefaultTimeProvider : TimeProvider {
    override fun getCurrentTime() = Clock.System.now()
}