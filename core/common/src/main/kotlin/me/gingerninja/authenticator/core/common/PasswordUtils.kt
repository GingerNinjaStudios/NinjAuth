package me.gingerninja.authenticator.core.common

fun String.toDatabaseByteArray() = toByteArray(Charsets.UTF_8)

fun CharSequence.toDatabaseByteArray() = toString().toByteArray(Charsets.UTF_8)