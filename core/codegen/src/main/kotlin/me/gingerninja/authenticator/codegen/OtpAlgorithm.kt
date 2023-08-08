package me.gingerninja.authenticator.codegen

enum class OtpAlgorithm(internal val value: String) {
    SHA1("HmacSHA1"),
    SHA256("HmacSHA256"),
    SHA512("HmacSHA512")
}