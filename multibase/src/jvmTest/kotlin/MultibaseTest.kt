import at.asitplus.io.MultiBase
import at.asitplus.testballoon.matrix.matrixSuite
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import java.io.BufferedReader

val MultibaseTest by matrixSuite {
    data(
        "CSV fixtures",
        listOf("leading_zero" to false, "two_leading_zeros" to false, "case_insensitivity" to true, "basic" to false)
    ) test { (file, ignoreCase) ->
        loadAndTest(file, ignoreCase)
    }

    compact("encode/decode") - {
        property(
            Arb.bind(
                Arb.enum<MultiBase.Base>(),
                Arb.byteArray(Arb.int(1..4096), Arb.byte()),
                ::Pair
            ),
            iterations = 128
        ) test { (base, bytes) ->
            MultiBase.decode(MultiBase.encode(base, bytes)) shouldBe bytes
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun loadAndTest(file: String, ignoreCase: Boolean = false) {
    val test = readCsv(file)
    val map = test.bases.map {
        it.key.getDecoder() to it.value
    }.filter { (k, _) -> k != null }

    map.forEach { (base, value) ->
        val src = test.str.encodeToByteArray()
        val encoded = MultiBase.encode(base!!, src)
        println("Encode: %-20s".format(base) + "\t" + "%-25s".format(value) + "\t?=\t$encoded")

        if (!ignoreCase) encoded shouldBe value

        val decoded = MultiBase.decode(value)
        println("Decode: %-20s".format(base) + "\t" + "%-25s".format(src.toHexString()) + "\t?=\t${decoded!!.toHexString()}")
        decoded.toHexString() shouldBe src.toHexString()
    }
}

private fun String.getDecoder(): MultiBase.Base? = MultiBase.Base.entries.firstOrNull {
    it.name.replace("_", "").equals(this, ignoreCase = true)
}

private fun readCsv(file: String): Testcase =
    BufferedReader(
        MultibaseTest::class.java.getResourceAsStream("/multibase/tests/$file.csv").reader(Charsets.UTF_8)
    ).use {
        Testcase(it.readLine().let { str ->
            val last = str.split(",").last()
            last.trim().substring(1, last.length - 2).replace("\\x00", "\u0000")
        },
            it.lines().map { str ->
                str.split(",").let {
                    val last = it.last()
                    it.first().trim() to last.trim().substring(1, last.length - 2)
                }
            }.toList().associate { it })
    }


data class Testcase(val str: String, val bases: Map<String, String>)
