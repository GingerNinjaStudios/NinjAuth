package me.gingerninja.authenticator.codegen

object Base32 {
    private val BASE32_ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray()

    private val BASE32_CHAR_MAP: Map<Char, Int> = buildMap {
        BASE32_ARRAY.forEachIndexed { index, c ->
            put(c, index)
        }
    }

    fun decode(data: String): ByteArray {
        val raw = data.uppercase().let {
            val paddingIdx = it.indexOf('=')
            if (paddingIdx > -1) {
                data.substring(0, paddingIdx)
            } else {
                data
            }
        }

        val n = raw.length

        val bytes = ByteArray(n * 5 / 8)
        var buffer = 0
        var remBits = 0
        var i = 0
        for (c in raw.toCharArray()) {
            require(BASE32_CHAR_MAP.containsKey(c)) { "Invalid character: $c" }
            buffer = buffer shl 5
            buffer = buffer or (BASE32_CHAR_MAP[c]!! and 31)
            remBits += 5
            if (remBits >= 8) {
                bytes[i++] = (buffer shr remBits - 8).toByte()
                remBits -= 8
            }
        }
        return bytes
    }

    fun isValid(data: String, paddingAllowed: Boolean = false) =
        data.all { BASE32_CHAR_MAP.containsKey(it) || (it == '=' && paddingAllowed) }

}