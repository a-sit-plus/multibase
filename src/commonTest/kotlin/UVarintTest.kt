import at.asitplus.io.UVarInt
import kotlin.collections.forEach
import kotlin.random.Random
import kotlin.random.nextUInt
import kotlin.test.Test
import kotlin.test.assertEquals

class UVarintTest {

    @Test
    fun random() {
        repeat(1024) {
            val it = Random.nextUInt()
            assertEquals(expected = it, actual = UVarInt(it).toULong().toUInt())
            assertEquals(
                expected = it,
                actual = UVarInt.fromByteArray(UVarInt(it).encodeToByteArray()).toULong().toUInt()
            )
        }
    }

    @Test
    fun edgeCases() {
        listOf(UInt.MAX_VALUE, UInt.MAX_VALUE, 1u).forEach {
            assertEquals(expected = it, actual = UVarInt(it).toULong().toUInt())
            assertEquals(
                expected = it,
                actual = UVarInt.fromByteArray(UVarInt(it).encodeToByteArray()).toULong().toUInt()
            )
        }

    }
}