import at.asitplus.io.UVarInt
import at.asitplus.testballoon.matrix.CompactReport
import at.asitplus.testballoon.matrix.matrixSuite
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.uInt

val UVarintTest by matrixSuite {
    compact("random values") - {
        property(Arb.uInt(), iterations = 1024) test { value ->
            UVarInt(value).toULong().toUInt() shouldBe value
            UVarInt.fromByteArrayStrict(UVarInt(value).encodeToByteArray()) shouldBe UVarInt(value)
        }
    }

    data("edge cases", listOf(Long.MAX_VALUE.toULong(), UInt.MAX_VALUE.toULong(), 1uL, 0uL)) test { value ->
        UVarInt(value).toULong() shouldBe value
        val encoded = UVarInt(value).encodeToByteArray()
        UVarInt.fromByteArrayStrict(encoded).toULong() shouldBe value
        UVarInt.fromByteArrayStrict(byteArrayOf(0x21, 0x15) + encoded, 2).toULong() shouldBe value
        UVarInt.fromByteArrayPermissive(
            byteArrayOf(0x42, 0x53) + encoded + byteArrayOf(0x21, 0x15),
            2
        ).let { (decoded, i) ->
            decoded.toULong() shouldBe value
            i shouldBe encoded.size+2
        }
    }

    "errors" {
        val AB = 0xAB.toByte()
        val FF = 0xFF.toByte()
        // not minimally encoded zero
        shouldThrow<NumberFormatException> { UVarInt.fromByteArrayPermissive(byteArrayOf(AB, 0x0)) }
        // trailing bytes
        shouldThrow<NumberFormatException> { UVarInt.fromByteArrayStrict(byteArrayOf(0x0, 0x0)) }
        // runs off the end
        shouldThrow<NumberFormatException> { UVarInt.fromByteArrayPermissive(byteArrayOf(AB)) }
        // larger than 2^63-1
        shouldThrow<IllegalArgumentException> { UVarInt(ULong.MAX_VALUE) }
        shouldThrow<NumberFormatException> { UVarInt.fromByteArrayPermissive(byteArrayOf(AB, AB, AB, AB, AB, AB, AB, AB, AB, 0x01)) }
        // 2^63-1 with leading byte
        UVarInt.fromByteArrayStrict(byteArrayOf(0x42, FF, FF, FF, FF, FF, FF, FF, FF, 0x7f), 1) shouldBe UVarInt(0x7fffffffffffffffuL)
        // 2^64-1 with leading byte
        shouldThrow<NumberFormatException> { UVarInt.fromByteArrayStrict(byteArrayOf(0x42, FF, FF, FF, FF, FF, FF, FF, FF, FF), 1) }
    }
}
