package com.ethohampton.yesno

import java.util.*
import kotlin.math.roundToInt

class BloomFilter constructor(expectedElements: Int, size: Int = 1024 * 1024 * 8) : Cloneable {
    private val hashes: BitSet
    private val prng: RandomInRange
    private var k: Int // Number of hash functions

    /**
     * Add an element to the container
     */
    fun add(o: Any) {
        prng.init(o)
        for (r in prng) hashes.set(r.value)
    }

    /**
     * If the element is in the container, returns true.
     * If the element is not in the container, returns true with a probability ≈ e^(-ln(2)² * m/n), otherwise false.
     * So, when m is large enough, the return value can be interpreted as:
     * - true  : the element is probably in the container
     * - false : the element is definitely not in the container
     */
    operator fun contains(o: Any): Boolean {
        prng.init(o)
        for (r in prng) if (!hashes[r.value]) return false
        return true
    }

    /**
     * Removes all of the elements from this filter.
     */
    fun clear() {
        hashes.clear()
    }

    /**
     * Create a copy of the current filter
     */
    @Throws(CloneNotSupportedException::class)
    public override fun clone(): BloomFilter {
        return super.clone() as BloomFilter
    }

    /**
     * Generate a unique hash representing the filter
     */
    override fun hashCode(): Int {
        return hashes.hashCode() xor k
    }

    /**
     * Test if the filters have equal bitsets.
     * WARNING: two filters may contain the same elements, but not be equal
     * (if the filters have different size for example).
     */
    fun equals(other: BloomFilter): Boolean {
        return hashes == other.hashes && k == other.k
    }

    /**
     * Merge another bloom filter into the current one.
     * After this operation, the current bloom filter contains all elements in
     * other.
     */
    fun merge(other: BloomFilter) {
        require(!(other.k != k || other.hashes.size() != hashes.size())) { "Incompatible bloom filters" }
        hashes.or(other.hashes)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BloomFilter

        if (hashes != other.hashes) return false
        if (prng != other.prng) return false
        if (k != other.k) return false

        return true
    }

    private inner class RandomInRange(// Maximum value returned + 1
        private val max: Int, // Number of random elements to generate
        private val count: Int
    ) : Iterable<RandomInRange>,
        MutableIterator<RandomInRange> {
        private val prng: Random = Random()
        private var i = 0 // Number of elements generated
        var value // The current value
                = 0

        fun init(o: Any) {
            prng.setSeed(o.hashCode().toLong())
        }

        override fun iterator(): Iterator<RandomInRange> {
            i = 0
            return this
        }

        override fun next(): RandomInRange {
            i++
            value = prng.nextInt() % max
            if (value < 0) value = -value
            return this
        }

        override fun hasNext(): Boolean {
            return i < count
        }

        override fun remove() {
            throw UnsupportedOperationException()
        }

    }

    companion object {
        private const val LN2 = 0.6931471805599453 // ln(2)
    }
    /**
     * Create a new bloom filter.
     * @param expectedElements Expected number of elements
     * @param size Desired size of the container in bits
     */
    /**
     * Create a bloom filter of 1Mib.
     * @param expectedElements Expected number of elements
     */
    init {
        k = (LN2 * size / expectedElements).roundToInt()
        if (k <= 0) k = 1
        hashes = BitSet(size)
        prng = RandomInRange(size, k)
    }
}