package me.gingerninja.authenticator.core.auth.password

class PasswordAuthException(val reason: Reason) : RuntimeException("Reason: $reason") {
    enum class Reason {
        WRONG_PASSWORD
    }
}