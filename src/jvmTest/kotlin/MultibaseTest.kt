import at.asitplus.io.MultiBase
import org.junit.Test
import java.io.BufferedReader
import kotlin.random.Random
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class MultibaseTest {


    @Test
    fun leading_zero() = foo("leading_zero")

    @Test
    fun two_leading_zeros() = foo("two_leading_zeros")

    @Test
    fun case_insensitivit() = foo("case_insensitivity", ignoreCase = true)

    @Test
    fun basic() = foo("basic")

    @Test
    fun encodeDecode() {
        val rnd1024 = Random.nextBytes(1024)
        val bytes = listOf(
            Random.nextBytes(1),
            Random.nextBytes(8),
            rnd1024,
            List(4) { rnd1024.apply { shuffle() }.asList() }.flatten().toByteArray()
        )

        bytes.forEach {
            println("Encoding/Decoding ${it.size} bytes")
            MultiBase.Base.entries.forEach { base ->
                println("\t$base")
                assertContentEquals(it, MultiBase.decode(MultiBase.encode(base, it)))
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun foo(file: String, ignoreCase: Boolean = false) {
        val test = readCsv(file)
        val map = test.bases.map {
            it.key.getDecoder() to it.value
        }.filter { (k, _) -> k != null }

        map.forEach { (base, value) ->
            val src = test.str.encodeToByteArray()
            val encoded = MultiBase.encode(base!!, src)
            println("Encode: %-20s".format(base) + "\t" + "%-25s".format(value) + "\t?=\t$encoded")

            if (!ignoreCase) assertEquals(expected = value, actual = encoded)

            val decoded = MultiBase.decode(value)
            println("Decode: %-20s".format(base) + "\t" + "%-25s".format(src.toHexString()) + "\t?=\t${decoded!!.toHexString()}")
            assertEquals(expected = src.toHexString(), actual = decoded.toHexString())
        }
    }


    fun String.getDecoder(): MultiBase.Base? = MultiBase.Base.entries.firstOrNull {
        it.name.replace("_", "").equals(this, ignoreCase = true)
    }


    fun readCsv(file: String): Testcase =
        BufferedReader(
            this::class.java.getResourceAsStream("/multibase/tests/$file.csv").reader(Charsets.UTF_8)
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
}


data class Testcase(val str: String, val bases: Map<String, String>)