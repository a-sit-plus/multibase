import at.asitplus.io.UVarInt
import at.asitplus.testballoon.matrix.matrixSuite
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.uInt

val UVarintTest by matrixSuite {
    compact("random values") - {
        property(Arb.uInt(), iterations = 1024) test { value ->
            UVarInt(value).toULong().toUInt() shouldBe value
            UVarInt.fromByteArray(UVarInt(value).encodeToByteArray()).toULong().toUInt() shouldBe value
        }
    }

    data("edge cases", listOf(UInt.MAX_VALUE, UInt.MAX_VALUE, 1u)) test { value ->
        UVarInt(value).toULong().toUInt() shouldBe value
        UVarInt.fromByteArray(UVarInt(value).encodeToByteArray()).toULong().toUInt() shouldBe value
    }
}
