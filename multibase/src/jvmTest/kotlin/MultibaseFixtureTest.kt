import at.asitplus.io.MultiBase
import at.asitplus.testballoon.matrix.matrixSuite
import io.kotest.matchers.shouldBe
import java.io.BufferedReader

val MultibaseFixtureTest by matrixSuite {
    data(
        "CSV fixtures",
        listOf("leading_zero" to false, "two_leading_zeros" to false, "case_insensitivity" to true, "basic" to false),
        nameFn = { index, (file, ignoreCase) -> "$index: ($file, ignoreCase = $ignoreCase)" }
    ) - { (file, ignoreCase) ->
        val test = readCsv(file)
        val map = test.bases.map {
            it.key.getDecoder() to it.value
        }.filter { (k, _) -> k != null }
        map.asData("base", nameFn = { (k, _) -> k?.name ?: "null" }) - { (base, value) ->
            val src = test.str.encodeToByteArray()

            val encoded = MultiBase.encode(base!!, src)
            ("Encode: $value\t?=\t$encoded"){
                encoded.first() shouldBe value.first()
                encoded.drop(1).let { if (ignoreCase) it.lowercase() else it } shouldBe
                        value.drop(1).let { if (ignoreCase) it.lowercase() else it }
            }

            val decoded = MultiBase.decode(value)
            (("Decode: " + src.toHexString()) + "\t?=\t${decoded!!.toHexString()}") {
                decoded.toHexString() shouldBe src.toHexString()
            }
        }
    }

}

private fun String.getDecoder(): MultiBase.Base? = MultiBase.Base.entries.firstOrNull {
    it.name.replace("_", "").equals(this, ignoreCase = true)
}

private fun readCsv(file: String): Testcase =
    BufferedReader(
        MultibaseFixtureTest::class.java.getResourceAsStream("/multibase/tests/$file.csv").reader(Charsets.UTF_8)
    ).use {
        Testcase(
            it.readLine().let { str ->
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
