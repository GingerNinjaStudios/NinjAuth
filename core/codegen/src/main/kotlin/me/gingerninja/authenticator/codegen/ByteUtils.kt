package me.gingerninja.authenticator.codegen

@OptIn(ExperimentalStdlibApi::class)
internal fun bytesToHex(bytes: ByteArray) = bytes.toHexString()

@OptIn(ExperimentalStdlibApi::class)
internal fun hexToBytes(hex: String) = hex.hexToByteArray()