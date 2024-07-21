/* Originally created by Protocol labs, published at GitHub: https://github.com/changjiashuai/kotlin-multibase
under the terms of the MIT License.
Slightly tweaked to allow for multiplatform use in 2024 by A-SIT Plus GmbH

Copyright (c) 2018-2022 Protocol Labs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package at.asitplus.io

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.Sign
import io.matthewnelson.encoding.base16.Base16
import io.matthewnelson.encoding.base32.Base32Default
import io.matthewnelson.encoding.base32.Base32Hex
import io.matthewnelson.encoding.base64.Base64
import io.matthewnelson.encoding.base64.Base64ConfigBuilder
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import io.matthewnelson.encoding.core.Encoder.Companion.encodeToString
import io.matthewnelson.encoding.core.EncodingException


/**
 * Use this to manually encode/decode BaseN, but only if you know what you're doing!
 * Originally created by [CJS](mailto:changjiashuai@gmail.com) on 2018/7/14 without documentation and since -- even though
 * it is extremely versatile -- it performs poorly, we better keep it like that. After all, we don't want to tempt
 * folk to produce code that performs poorly.
 */
object BaseN {

    fun decode(alphabet: String, base: BigInteger, input: String): ByteArray {
        val bytes = decodeToBigInteger(alphabet, base, input).toByteArray()
        val stripSignByte = bytes.size > 1 && bytes[0].compareTo(0) == 0 && bytes[1] < 0
        var leadingZeros = 0
        var i = 0
        while (input[i] == alphabet[0]) {
            leadingZeros++
            i++
        }
        val tmp = ByteArray(bytes.size - (if (stripSignByte) 1 else 0) + leadingZeros)
        bytes.copyInto(
            tmp,
            startIndex = if (stripSignByte) 1 else 0,
            destinationOffset = leadingZeros,
            endIndex = tmp.size - leadingZeros
        )
        return tmp
    }

    fun encode(alphabet: String, base: BigInteger, input: ByteArray): String {
        var bi = BigInteger.fromByteArray(input, Sign.POSITIVE)
        val sb = StringBuilder()
        while (bi >= base) {
            //求余
            val mod = bi.mod(base)
            sb.insert(0, alphabet[mod.intValue()])
            bi = bi.subtract(mod).divide(base)
        }
        sb.insert(0, alphabet[bi.intValue()])
        //convert leading zeros.
        for (b in input) {
            if (b.compareTo(0) == 0) {
                sb.insert(0, alphabet[0])
            } else {
                break
            }
        }
        return sb.toString()
    }

    fun decodeToBigInteger(alphabet: String, base: BigInteger, input: String): BigInteger {
        var bi = BigInteger.ZERO
        for (i in input.length - 1 downTo 0) {
            val alphaIndex = alphabet.indexOf(input[i])
            if (alphaIndex == -1) {
                throw EncodingException("Illegal character " + input[i] + " at " + i)
            }
            bi = bi.add(BigInteger.fromLong(alphaIndex.toLong()).multiply(base.pow(input.length - 1 - i)))
        }
        return bi
    }
}


/**
 * [RFC4648](https://www.ietf.org/rfc/rfc4648.txt) Multibase encoder/decoder
 * Initially Created by [CJS](mailto:changjiashuai@gmail.com) on 2018/7/12.
 */
object MultiBase {
    /**
     * Multibase encoding identifier.
     * Each multibase-encoded string is identified by a [prefix], followed by data composed of [alphabet] characters.
     * The prefix is also part of the alphabet. See also [RFC4648](https://www.ietf.org/rfc/rfc4648.txt).
     */
    enum class Base(val prefix: Char, val alphabet: String) {
        BASE10('9', "0123456789"),
        BASE16('f', "0123456789abcdef"),
        BASE16_UPPER('F', "0123456789ABCDEF"),
        BASE32('b', "abcdefghijklmnopqrstuvwxyz234567"),
        BASE32_UPPER('B', "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"),
        BASE32_PAD('c', "abcdefghijklmnopqrstuvwxyz234567="),
        BASE32_PAD_UPPER('C', "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567="),
        BASE32_HEX('v', "0123456789abcdefghijklmnopqrstuvw"),
        BASE32_HEX_UPPER('V', "0123456789ABCDEFGHIJKLMNOPQRSTUVW"),
        BASE32_HEX_PAD('t', "0123456789abcdefghijklmnopqrstuvw="),
        BASE32_HEX_PAD_UPPER('T', "0123456789ABCDEFGHIJKLMNOPQRSTUVW="),
        BASE58_FLICKR('Z', "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ"),
        BASE58_BTC('z', "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"),
        BASE64('m', "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"),
        BASE64_URL('u', "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"),
        BASE64_PAD('M', "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="),
        BASE64_URL_PAD('U', "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_=");

        companion object {

            /**
             * Returns a [Base] matching the provided [prefix] or `null` if none was found.
             */
            fun lookup(prefix: Char): Base? {
                return entries.firstOrNull { it.prefix == prefix }
            }
        }
    }

    /**
     * Encodes the given [data] into the specified [base].
     */
    fun encode(base: Base, data: ByteArray): String {
        return when (base) {
            Base.BASE10 -> base.prefix + BaseN.encode(base.alphabet, BigInteger(10), data)
            Base.BASE16 -> base.prefix + data.encodeToString(Base16Lower)
            Base.BASE16_UPPER -> base.prefix + data.encodeToString(Base16Upper)
            Base.BASE32 -> base.prefix + data.encodeToString(Base32Lower)
            Base.BASE32_UPPER -> base.prefix + data.encodeToString(Base32Upper)
            Base.BASE32_PAD -> base.prefix + data.encodeToString(Base32Pad)
            Base.BASE32_PAD_UPPER -> base.prefix + data.encodeToString(Base32PadUpper)
            Base.BASE32_HEX -> base.prefix + data.encodeToString(Base32HexLower)
            Base.BASE32_HEX_UPPER -> base.prefix + data.encodeToString(Base32HexUpper)
            Base.BASE32_HEX_PAD -> base.prefix + data.encodeToString(Base32HexPadLower)
            Base.BASE32_HEX_PAD_UPPER -> base.prefix + data.encodeToString(Base32HexPadUpper)
            Base.BASE58_FLICKR -> base.prefix + BaseN.encode(base.alphabet, BigInteger(58), data)
            Base.BASE58_BTC -> base.prefix + BaseN.encode(base.alphabet, BigInteger(58), data)
            Base.BASE64 -> base.prefix + data.encodeToString(Base64NoPadding)
            Base.BASE64_URL -> base.prefix + data.encodeToString(Base64UrlNoPadding)
            Base.BASE64_PAD -> base.prefix + data.encodeToString(Base64)
            Base.BASE64_URL_PAD -> base.prefix + data.encodeToString(Base64UrlPadding)
        }
    }

    /**
     * Decodes the given multibase [data] into a ByteArray.
     * This method throws Exceptions for strings that are not valid multibase encodings.
     *
     * Returns `null` if the encoding is not supported (e.g. Base-256 Emoji).
     */
    @Throws(Throwable::class)
    fun decode(data: String): ByteArray? {
        val prefix = data[0]
        val rest = data.substring(1)
        return when (val base = Base.lookup(prefix)) {
            Base.BASE10 -> BaseN.decode(base.alphabet, BigInteger(10), rest)
            Base.BASE16 -> rest.decodeToByteArray(Base16Lower)
            Base.BASE16_UPPER ->  rest.decodeToByteArray(Base16Upper)
            Base.BASE32 -> rest.decodeToByteArray(Base32Lower)
            Base.BASE32_PAD -> rest.decodeToByteArray(Base32Pad)
            Base.BASE32_HEX_PAD -> rest.decodeToByteArray(Base32HexPadLower)
            Base.BASE32_UPPER -> rest.uppercase().decodeToByteArray(Base32Upper)
            Base.BASE32_PAD_UPPER -> rest.uppercase().decodeToByteArray(Base32Pad)
            Base.BASE32_HEX -> rest.decodeToByteArray(Base32HexLower)
            Base.BASE32_HEX_UPPER -> rest.uppercase().decodeToByteArray(Base32HexLower)
            Base.BASE32_HEX_PAD_UPPER -> rest.uppercase().decodeToByteArray(Base32HexPadLower)
            Base.BASE58_FLICKR -> BaseN.decode(base.alphabet, BigInteger(58), rest)
            Base.BASE58_BTC -> BaseN.decode(base.alphabet, BigInteger(58), rest)
            Base.BASE64 -> rest.decodeToByteArray(Base64NoPadding)
            Base.BASE64_URL -> rest.decodeToByteArray(Base64UrlNoPadding)
            Base.BASE64_PAD -> rest.decodeToByteArray(Base64)
            Base.BASE64_URL_PAD -> rest.decodeToByteArray(Base64UrlPadding)
            null -> null
        }
    }
}

/**
 * Decodes this string into a ByteArray.
 * This method throws Exceptions for strings that are not valid multibase encodings.
 *
 * Returns `null` if the encoding is not supported (e.g. Base-256 Emoji).
 */
fun String.multibaseDecode() = MultiBase.decode(this)

/**
 * Encodes this ByteArray into the specified [base].
 */
fun ByteArray.multibaseEncode(base: MultiBase.Base) = MultiBase.encode(base, this)


private val Base64UrlPadding = Base64(config = Base64ConfigBuilder().apply {
    lineBreakInterval = 0
    encodeToUrlSafe = true
    isLenient = false
    padEncoded = true
}.build())

private val Base64UrlNoPadding = Base64(config = Base64ConfigBuilder().apply {
    lineBreakInterval = 0
    encodeToUrlSafe = true
    isLenient = false
    padEncoded = false
}.build())


private val Base64 = Base64(config = Base64ConfigBuilder().apply {
    lineBreakInterval = 0
    encodeToUrlSafe = false
    isLenient = false
    padEncoded = true
}.build())

private val Base64NoPadding = Base64(config = Base64ConfigBuilder().apply {
    lineBreakInterval = 0
    encodeToUrlSafe = false
    isLenient = true
    padEncoded = false
}.build())


private val Base32Lower = Base32Default { encodeToLowercase = true; padEncoded = false }
private val Base32Upper = Base32Default { encodeToLowercase = false; padEncoded = false }

private val Base32Pad = Base32Default { isLenient = false; padEncoded = true; encodeToLowercase = true }
private val Base32PadUpper = Base32Default { isLenient = false; padEncoded = true;encodeToLowercase = false }

private val Base32HexPadLower = Base32Hex { isLenient = false; padEncoded = true;encodeToLowercase = true }
private val Base32HexPadUpper = Base32Hex { isLenient = false; padEncoded = true;encodeToLowercase = false }


private val Base32HexLower = Base32Hex { padEncoded = false;encodeToLowercase = true }
private val Base32HexUpper = Base32Hex { padEncoded = false;encodeToLowercase = false }

private val Base16Lower = Base16 { encodeToLowercase = true }
private val Base16Upper = Base16 { encodeToLowercase = false }
