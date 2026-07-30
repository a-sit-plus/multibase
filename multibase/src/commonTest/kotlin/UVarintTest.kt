import at.asitplus.io.UVarInt
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

    data("edge cases", listOf(UInt.MAX_VALUE, UInt.MAX_VALUE, 1u, 0u)) test { value ->
        UVarInt(value).toULong().toUInt() shouldBe value
        UVarInt.fromByteArrayStrict(UVarInt(value).encodeToByteArray()).toULong().toUInt() shouldBe value
    }

    "errors" {
        val AB = 0xAB.toByte()
        // not minimally encoded zero
        shouldThrow<NumberFormatException> { UVarInt.fromByteArrayPermissive(byteArrayOf(AB, 0x0)) }
        // trailing bytes
        shouldThrow<IllegalArgumentException> { UVarInt.fromByteArrayStrict(byteArrayOf(0x0, 0x0)) }
        // runs off the end
        shouldThrow<IllegalArgumentException> { UVarInt.fromByteArrayPermissive(byteArrayOf(AB)) }
        // larger than 2^63-1
        shouldThrow<IllegalArgumentException> { UVarInt(ULong.MAX_VALUE) }
        shouldThrow<IllegalArgumentException> { UVarInt.fromByteArrayPermissive(byteArrayOf(AB, AB, AB, AB, AB, AB, AB, AB, AB, 0x01)) }
    }
}
