package me.gingerninja.authenticator.core.codegen

import kotlin.time.Clock
import kotlin.time.Instant

interface TimeProvider {
    fun getCurrentTime(): Instant
}

object DefaultTimeProvider : TimeProvider {
    override fun getCurrentTime() = Clock.System.now()
}