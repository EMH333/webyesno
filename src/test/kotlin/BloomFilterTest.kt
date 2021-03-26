import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class BloomFilterTest{

    @Test
    fun testExistence() {
        val filter = BloomFilter(10_000)
        filter.add("127.0.0.1")
        filter.add("127.0.0.2")
        filter.add("255.255.255.255")
        filter.add("140.211.198.250")

        assertTrue(filter.contains("127.0.0.1"))
        assertTrue(filter.contains("127.0.0.2"))
        assertTrue(filter.contains("255.255.255.255"))
        assertTrue(filter.contains("140.211.198.250"))
        assertFalse(filter.contains("186.75.30.9"))
    }
}