// Based on UVarInt.kt (https://github.com/erwin-kok/multiformat/blob/main/src/main/kotlin/org/erwinkok/multiformat/util/UVarInt.kt)
// Originally Copyright (c) 2022 Erwin Kok under the terms of the BSD-3-Clause license.
// Simplified by A-SIT Plus in 2024

package at.asitplus.io


/**
 * Unsigned variable-length integer supporting values up to 2^63 - 1.
 */
data class UVarInt(private val number: ULong) {
    init { require(number <= Long.MAX_VALUE.toULong()) }
    /**
     * Convenience constructor to create an object from an unsigned int. To create larger UVarInts, use [fromByteArray].
     */
    constructor(number: UInt) : this(number.toULong())

    override fun toString() = "0x"+number.toString(16)

    /** Returns the ULong value of this UVarInt. */
    fun toULong(): ULong = number

    /**
     * Encodes this number's value into a ByteArray using varint encoding.
     */
    fun encodeToByteArray(): ByteArray {
        var acc = number
        var i = 0
        val res = mutableListOf<Byte>()
        while (acc >= 0x80u) {
            res += (((acc and 0x7Fu) or 0x80u).toByte())
            acc = (acc.toLong() ushr 7).toULong()
            i++
        }
        return (res + (acc and 0x7Fu).toByte()).toByteArray()
    }

    companion object {
        /**
         * Maximum number of bytes representing a UVarInt in this encoding,
         * supporting values up to 2^63 - 1.
         */
        const val MAX_BYTES = 9L

        /**
         * Decodes a varint-encoded ByteArray into a UVarInt.
         * @throws NumberFormatException on illegal input (e.g. values larger than 2^63 - 1 or non-minimal encodings)
         */
        @Throws(NumberFormatException::class)
        @Deprecated(message="Specify explicitly whether you want to consume the entire byte array" +
                "(use fromByteArrayStrict or fromByteArrayPermissive)")
        fun fromByteArray(bytes: ByteArray): UVarInt = fromByteArrayPermissive(bytes).first

        /**
         * Decodes the entirety of [bytes] into an [UVarInt].
         * @throws NumberFormatException on illegal input (e.g. values larger than 2^63 - 1 or non-minimal encodings)
         * @throws IllegalArgumentException if [bytes] contains trailing bytes after the [UVarInt]
         */
        fun fromByteArrayStrict(bytes: ByteArray): UVarInt =
            decode(bytes).let {
                require(it.second == bytes.size) {
                    "UVarInt decoding only consumed ${it.second} bytes, but ${bytes.size} were provided."
                }
                UVarInt(it.first)
            }

        /**
         * Decodes the leading UVarInt from [bytes].
         * Returns the UVarInt and the number of leading bytes consumed.
         * @throws NumberFormatException on illegal input (e.g. values larger than 2^63 - 1 or non-minimal encodings)
         */
        fun fromByteArrayPermissive(bytes: ByteArray): Pair<UVarInt,Int> =
            decode(bytes).let { Pair(UVarInt(it.first),it.second) }

        /** Decodes a varint-encoded leading ByteArray */
        private fun decode(encoded: ByteArray): Pair<ULong,Int> {
            var value = 0uL
            encoded.forEachIndexed { i, byte ->
                val s = (i*7)
                val uByte = byte.toUByte()
                if ((i == 9 && uByte >= 0x80u) || i > MAX_BYTES) {
                    // this is the 9th and last byte we're willing to read, but it
                    // signals there's more (1 in MSB).
                    // or this is the >= 10th byte, and for some reason we're still here.
                    throw NumberFormatException("varints larger than uint63 not supported")
                }
                if (uByte < 0x80u) {
                    if (uByte == 0u.toUByte() && s > 0) {
                        throw NumberFormatException("varint not minimally encoded")
                    }
                    return Pair(value or (uByte.toULong() shl s), i+1)
                }
                value = value or ((uByte and 0x7fu).toULong() shl s)
            }
            throw NumberFormatException("varint is not terminated (overruns array)")
        }
    }
}

/**
 * Extension function to decode this ByteArray to an [UVarInt] using [UVarInt.fromByteArray]
 * @throws NumberFormatException on illegal input (e.g. values larger than 2^63 - 1 or non-minimal encodings)
 */
@Throws(NumberFormatException::class)
fun ByteArray.varIntDecode() = UVarInt.fromByteArray(this)
