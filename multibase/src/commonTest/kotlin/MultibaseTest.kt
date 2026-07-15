import at.asitplus.io.MultiBase
import at.asitplus.testballoon.matrix.matrixSuite
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.int

val MultibaseTest by matrixSuite {
    compact("encode/decode") - {
        data("bases", MultiBase.Base.entries) - { base ->
            property(Arb.byteArray(Arb.int(0..256), Arb.byte()), iterations = 1_000) test { bytes ->
                MultiBase.decode(MultiBase.encode(base, bytes)) shouldBe bytes
            }
            data(
                "boundaries",
                listOf(
                    byteArrayOf(), byteArrayOf(0), byteArrayOf(0, 0), byteArrayOf(0, 1),
                    byteArrayOf(Byte.MIN_VALUE), byteArrayOf(Byte.MAX_VALUE), byteArrayOf(-1)
                ),
                nameFn = { index, bytes -> "$index: ${bytes.toHexString()}" }
            ) test { bytes ->
                MultiBase.decode(MultiBase.encode(base, bytes)) shouldBe bytes
            }
        }
    }

    data(
        "canonical BaseN zeros",
        listOf(
            Triple(MultiBase.Base.BASE10, byteArrayOf(0, 0), "900"),
            Triple(MultiBase.Base.BASE58_FLICKR, byteArrayOf(0, 0), "Z11"),
            Triple(MultiBase.Base.BASE58_BTC, byteArrayOf(0, 0), "z11")
        )
    ) test { (base, bytes, expected) ->
        MultiBase.encode(base, bytes) shouldBe expected
        MultiBase.decode(expected) shouldBe bytes
    }
}
