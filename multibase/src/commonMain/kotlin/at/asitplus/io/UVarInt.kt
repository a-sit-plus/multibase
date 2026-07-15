// Based on UVarInt.kt (https://github.com/erwin-kok/multiformat/blob/main/src/main/kotlin/org/erwinkok/multiformat/util/UVarInt.kt)
// Originally Copyright (c) 2022 Erwin Kok under the term of the BSD-3-Clause license.
// Simplified by A-SIT Plus in 2024

package at.asitplus.io


/**
 * Unsigned varint datatype, MAX_VALUE=[ULong.MAX_VALUE]
 */
class UVarInt private constructor(private val number: ULong) {
    /**
     * Convenience constructor to create an object from an unsigned int. To create larger UVarInts, use [fromByteArray].
     */
    constructor(number: UInt) : this(number.toULong())

    /**
     * returns the ULong value of this UVarInt
     */
    fun toULong(): ULong = number

    /**
     * encodes this number's value into a ByteArray using varint encoding
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
         * maximum number of bytes representing an uvarint in this encoding,
         * supporting a maximum value of 2^63 (uint63).
         */
        const val MAX_BYTES = 9L

        /**
         * decodes a varint-encoded ByteArray into a UVarInt
         * @throws NumberFormatException on illegal input (e.g. values larger than unit63, or non-minimal encoding)
         */
        @Throws(NumberFormatException::class)
        fun fromByteArray(bytes: ByteArray): UVarInt = UVarInt(decode(bytes))

        private fun decode(encoded: ByteArray): ULong {
            var value = 0uL
            var s = 0
            var i = 0
            while (true) {
                val uByte = encoded[i].toUByte()
                if ((i == 8 && uByte >= 0x80u) || i >= MAX_BYTES) {
                    // this is the 9th and last byte we're willing to read, but it
                    // signals there's more (1 in MSB).
                    // or this is the >= 10th byte, and for some reason we're still here.
                    throw NumberFormatException("varints larger than uint63 not supported")
                }
                if (uByte < 0x80u) {
                    if (uByte == 0u.toUByte() && s > 0) {
                        throw NumberFormatException("varint not minimally encoded")
                    }
                    return value or (uByte.toULong() shl s)
                }
                value = value or ((uByte and 0x7fu).toULong() shl s)
                s += 7
                i++
            }
        }
    }
}

/**
 * Extension function to decode this ByteArray to an [UVarInt] using [UVarInt.fromByteArray]
 * @throws NumberFormatException on illegal input (e.g. values larger than unit63, or non-minimal encoding)
 */
@Throws(NumberFormatException::class)
fun ByteArray.varIntDecode() = UVarInt.fromByteArray(this)

