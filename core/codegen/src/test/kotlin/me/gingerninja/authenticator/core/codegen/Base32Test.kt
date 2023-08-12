package me.gingerninja.authenticator.core.codegen

import me.gingerninja.authenticator.core.codegen.Base32
import org.junit.Assert.*

import org.junit.Test

/**
 * Tests [Base32] functions.
 */
class Base32Test {

    @Test
    fun testDecode() {
        val decoded = Base32.decode("JBSWY3DPEB3W64TMMQQQ====").decodeToString()
        assertEquals("Hello world!", decoded)
    }

    @Test
    fun testIsValidWithPadding() {
        assertEquals(true, Base32.isValid("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567=", paddingAllowed = true))
    }

    @Test
    fun testIsValidWithoutPadding() {
        assertEquals(false, Base32.isValid("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567=", paddingAllowed = false))
    }
}