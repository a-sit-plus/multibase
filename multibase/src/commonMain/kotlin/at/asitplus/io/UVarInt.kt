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
        // we never encode anything larger than 2^63-1 (which fits in 9*7 bits)
        var res = ByteArray(MAX_BYTES)
        while (acc >= 0x80u) {
            res[i++] = (((acc and 0x7Fu) or 0x80u).toByte())
            acc = (acc.toLong() ushr 7).toULong()
            require(i < MAX_BYTES)
        }
        res[i++] = (acc and 0x7Fu).toByte()
        if (i == MAX_BYTES) return res
        else return res.copyOf(i)
    }

    companion object {
        /**
         * Maximum number of bytes representing a UVarInt in this encoding,
         * supporting values up to 2^63 - 1.
         */
        const val MAX_BYTES = 9

        /**
         * Decodes a varint-encoded ByteArray into a UVarInt.
         * @throws NumberFormatException on illegal input (e.g. values larger than 2^63 - 1 or non-minimal encodings)
         */
        @Throws(NumberFormatException::class)
        @Deprecated(message="Specify explicitly whether you want to consume the entire byte array" +
                "(use decodeFromByteArray or fromByteArrayPermissive)")
        fun fromByteArray(bytes: ByteArray): UVarInt = fromByteArrayPermissive(bytes).first

        /**
         * Decodes the entirety of [bytes], starting at index [startIndex], into an [UVarInt].
         * @throws NumberFormatException on illegal input (e.g. values larger than 2^63 - 1 or non-minimal encodings)
         * @throws NumberFormatException if [bytes] contains trailing bytes after the [UVarInt]
         * @see fromByteArrayPermissive
         */
        @Throws(NumberFormatException::class)
        fun fromByteArrayStrict(bytes: ByteArray, startIndex: Int = 0): UVarInt =
            decode(bytes, startIndex).let {
                if(it.second != bytes.size) {
                    throw NumberFormatException(
                        "UVarInt decoding only consumed ${it.second} bytes, but ${bytes.size} were provided.")
                }
                UVarInt(it.first)
            }

        /**
         * Decodes the leading UVarInt from [bytes], starting at index [startIndex].
         * Returns the UVarInt, and the first index in [bytes] past the UVarInt.
         * @throws NumberFormatException on illegal input (e.g. values larger than 2^63 - 1 or non-minimal encodings)
         * @see fromByteArrayStrict
         */
        @Throws(NumberFormatException::class)
        fun fromByteArrayPermissive(bytes: ByteArray, startIndex: Int = 0): Pair<UVarInt,Int> =
            decode(bytes, startIndex).let { Pair(UVarInt(it.first),it.second) }

        /** Decodes a varint-encoded leading ByteArray */
        private fun decode(encoded: ByteArray, startIndex: Int): Pair<ULong,Int> {
            require(startIndex < (Int.MAX_VALUE - MAX_BYTES))
            var value = 0uL
            var i = startIndex
            var s = 0
            val last = startIndex + MAX_BYTES
            while (true) {
                if (i >= encoded.size) {
                    throw NumberFormatException("varint is not terminated (overruns array)")
                }
                val uByte = encoded[i++].toUByte()
                if ((i == last && uByte >= 0x80u) || i > last) {
                    // this is the 9th and last byte we're willing to read, but it
                    // signals there's more (1 in MSB).
                    // or this is the >= 10th byte, and for some reason we're still here.
                    throw NumberFormatException("varints larger than uint63 not supported")
                }
                if (uByte < 0x80u) {
                    if (uByte == 0u.toUByte() && s > 0) {
                        throw NumberFormatException("varint not minimally encoded")
                    }
                    return Pair(value or (uByte.toULong() shl s), i)
                }
                value = value or ((uByte and 0x7fu).toULong() shl s)
                s += 7
            }
        }
    }
}

/**
 * Extension function to decode this ByteArray to an [UVarInt] using [UVarInt.fromByteArray]
 * @throws NumberFormatException on illegal input (e.g. values larger than 2^63 - 1 or non-minimal encodings)
 */
@Throws(NumberFormatException::class)
@Deprecated("Explicitly specify whether you want a leading UVarInt or the full byte array as UVarInt",
    ReplaceWith("decodeAsUVarInt()"))
fun ByteArray.varIntDecode() = UVarInt.fromByteArray(this)

@Throws(NumberFormatException::class)
/** @see UVarInt.fromByteArrayStrict */
fun ByteArray.decodeAsUVarInt(startIndex: Int = 0) =
    UVarInt.fromByteArrayStrict(this, startIndex)

@Throws(NumberFormatException::class)
/** @see UVarInt.fromByteArrayPermissive */
fun ByteArray.decodeLeadingUVarInt(startIndex: Int = 0) =
    UVarInt.fromByteArrayPermissive(this, startIndex)
