package org.edx.mobile.util

import java.text.ParseException
import java.util.*
import kotlin.math.abs
import kotlin.math.min

/**
 * Simple representation of the app's version.
 */
class Version
/**
 * Create a new instance from the provided version string.
 *
 * @param version The version string. The first three present dot-separated
 * tokens will be parsed as major, minor, and patch version
 * numbers respectively, and any further tokens will be
 * discarded.
 * @throws ParseException If one or more of the first three present dot-
 * separated tokens contain non-numeric characters.
 */
@Throws(ParseException::class)
constructor(version: String) : Comparable<Version> {

    /**
     * The version numbers
     */
    private val numbers = IntArray(3)

    /**
     * @return The major version.
     */
    val majorVersion: Int
        get() = getVersionAt(0)

    /**
     * @return The minor version.
     */
    val minorVersion: Int
        get() = getVersionAt(1)

    /**
     * @return The patch version.
     */
    val patchVersion: Int
        get() = getVersionAt(2)

    init {
        val numberStrings = version.split("\\.".toRegex())
        val versionsCount = min(NUMBERS_COUNT, numberStrings.size)
        for (i in 0 until versionsCount) {
            val numberString = numberStrings[i]
            /* Integer.parseInt() parses a string as a signed integer value, and
             * there is no available method for parsing as unsigned instead.
             * Therefore, we first check the first character manually to see
             * whether it's a plus or minus sign, and throw a ParseException if
             * it is.
             */
            val firstChar = numberString[0]
            if (firstChar == '-' || firstChar == '+') {
                throw VersionParseException(0)
            }
            try {
                numbers[i] = Integer.parseInt(numberString)
            } catch (e: NumberFormatException) {
                // Rethrow as a checked ParseException
                throw VersionParseException(version.indexOf(numberString))
            }

        }
    }

    /**
     * Returns the version number at the provided index.
     *
     * @param index The index at which to get the version number
     * @return The version number.
     */
    private fun getVersionAt(index: Int): Int {
        return if (index < numbers.size) numbers[index] else 0
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is Version && Arrays.equals(numbers, other.numbers))
    }

    override fun compareTo(other: Version): Int {
        for (i in 0 until NUMBERS_COUNT) {
            val number = numbers[i]
            val otherNumber = other.numbers[i]
            if (number != otherNumber) {
                return if (number < otherNumber) -1 else 1
            }
        }
        return 0
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(numbers)
    }

    override fun toString(): String {
        when (numbers.size) {
            0 -> return ""
            1 -> return numbers[0].toString()
        }
        val sb = StringBuilder()
        sb.append(numbers[0])
        for (i in 1 until numbers.size) {
            sb.append(".")
            sb.append(numbers[i])
        }
        return sb.toString()
    }

    /**
     * Convenience subclass of [ParseException], with the detail
     * message already provided.
     */
    private class VersionParseException
    /**
     * Constructs a new instance of this class with its stack
     * trace, detail message and the location of the error filled
     * in.
     *
     * @param location The location of the token at which the parse
     * exception occurred.
     */
    (location: Int) : ParseException("Token couldn't be parsed as a valid number.", location)

    /**
     * Compares this version with the specified version, to determine if minor versions'
     * difference between both is greater than or equal to the specified value.
     *
     * @param otherVersion      The version to compare to this instance.
     * @param minorVersionsDiff Value difference to compare between versions.
     * @return `true` if difference is greater than or equal to the specified value,
     * `false` otherwise.
     */
    fun isNMinorVersionsDiff(otherVersion: Version,
                             minorVersionsDiff: Int): Boolean {
        // Difference in major version is consider to be valid for any minor versions difference
        return abs(this.majorVersion - otherVersion.majorVersion) >= 1 || abs(this.minorVersion - otherVersion.minorVersion) >= minorVersionsDiff
    }

    /**
     * Compares this version with the specified version and determine if both have same major and
     * minor versions.
     *
     * @param otherVersion The version to compare to this instance.
     * @return `true` if both have same major and minor versions, `false` otherwise.
     */
    fun hasSameMajorMinorVersion(otherVersion: Version): Boolean {
        return this.majorVersion == otherVersion.majorVersion && this.minorVersion == otherVersion.minorVersion
    }

    companion object {
        /**
         * The number of version number tokens to parse.
         */
        private const val NUMBERS_COUNT = 3
    }
}
