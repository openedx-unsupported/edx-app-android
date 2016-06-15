package org.edx.mobile.util;

import org.junit.Test;

import java.text.ParseException;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Tests for verifying implementation correctness of {@link Version}.
 */
public class VersionTest {
    /**
     * Verify that a new instance created with a two-dot release string (x.x.x) is parsed
     * correctly (setting the major, minor, and patch version properties), and that it has
     * the correct string representation.
     */
    @Test
    public void testNewInstance_withTwoDotRelease_isParsedCorrectly() throws ParseException {
        final Version version = new Version("1.26.6");
        assertThat(version.getMajorVersion()).isEqualTo(1);
        assertThat(version.getMinorVersion()).isEqualTo(26);
        assertThat(version.getPatchVersion()).isEqualTo(6);
        assertThat(version).hasToString("1.26.6");
    }

    /**
     * Verify that a new instance created with a one-dot release string (x.x) is parsed
     * correctly (setting the major and minor version properties), and that it has the
     * correct string representation.
     */
    @Test
    public void testNewInstance_withOneDotRelease_isParsedCorrectly() throws ParseException {
        final Version version = new Version("1.26");
        assertThat(version.getMajorVersion()).isEqualTo(1);
        assertThat(version.getMinorVersion()).isEqualTo(26);
        assertThat(version.getPatchVersion()).isEqualTo(0);
        assertThat(version).isEqualTo(new Version("1.26.0"));
        assertThat(version).hasToString("1.26.0");
    }

    /**
     * Verify that a new instance created with a zero-dot release string (consisting only
     * of a single number) is parsed correctly (setting only the major version property), and
     * that it has the correct string representation.
     */
    @Test
    public void testNewInstance_withZeroDotRelease_isParsedCorrectly() throws ParseException {
        final Version version = new Version("1");
        assertThat(version.getMajorVersion()).isEqualTo(1);
        assertThat(version.getMinorVersion()).isEqualTo(0);
        assertThat(version.getPatchVersion()).isEqualTo(0);
        assertThat(version).isEqualTo(new Version("1.0.0"));
        assertThat(version).hasToString("1.0.0");
    }

    /**
     * Verify that a new instance created with more than three tokens is parsed correctly,
     * setting the supported version properties, and discarding the extra tokens.
     */
    @Test
    public void testNewInstance_withExtraTokens_isParsedCorrectly() throws ParseException {
        final Version version = new Version("1.26.6.alpha1");
        assertThat(version.getMajorVersion()).isEqualTo(1);
        assertThat(version.getMinorVersion()).isEqualTo(26);
        assertThat(version.getPatchVersion()).isEqualTo(6);
        assertThat(version).isEqualTo(new Version("1.26.6"));
        assertThat(version).hasToString("1.26.6");
    }

    /**
     * Verify that a new instance created with non-numeric characters in one of the first
     * three tokens throws a {@link ParseException}.
     */
    @Test(expected = ParseException.class)
    public void testNewInstance_withNonNumericTokens_ThrowsException() throws ParseException {
        new Version("1.26.alpha1");
    }

    /**
     * Verify that the {@link Version#equals(Object)} method returns {@code true}
     * if passed another instance created with an identical version string.
     */
    @Test
    public void testEquals_withSameVersion_isTrue() throws ParseException {
        assertThat(new Version("2.0.0")).isEqualTo(new Version("2.0.0"));
    }

    /**
     * Verify that the {@link Version#equals(Object)} method returns {@code false}
     * if passed another instance created with a different version string.
     */
    @Test
    public void testEquals_withDifferentVersion_isFalse() throws ParseException {
        assertThat(new Version("2.0.0")).isNotEqualTo(new Version("1.0.0"));
    }

    /**
     * Verify that the {@link Version#compareTo(Version)} method returns zero
     * if passed another instance created with an identical version string.
     */
    @Test
    public void testCompareTo_withSameVersion_isEqual() throws ParseException {
        assertThat(new Version("2.0.0")).isEqualByComparingTo(new Version("2.0.0"));
    }

    /**
     * Verify that the {@link Version#compareTo(Version)} method returns a positive
     * integer if passed another instance created with a lesser version string.
     */
    @Test
    public void testCompareTo_withEarlierVersion_isGreaterThan() throws ParseException {
        assertThat(new Version("2.0.0")).isGreaterThan(new Version("1.0.0"));
    }

    /**
     * Verify that the {@link Version#compareTo(Version)} method returns a negative
     * integer if passed another instance created with a larger version string.
     */
    @Test
    public void testCompareTo_withLaterVersion_isLessThan() throws ParseException {
        assertThat(new Version("1.0.0")).isLessThan(new Version("2.0.0"));
    }

    /**
     * Verify that the {@link Version#compareTo(Version)} method returns zero if passed
     * another instance created with a more precise, but identical, version string.
     */
    @Test
    public void testCompareTo_withMorePreciseSameVersion_isEqual() throws ParseException {
        assertThat(new Version("1")).isEqualByComparingTo(new Version("1.0.0"));
    }

    /**
     * Verify that the {@link Version#compareTo(Version)} method returns a positive integer
     * if passed another instance created with a more precise, but lesser, version string.
     */
    @Test
    public void testCompareTo_withMorePreciseEarlierVersion_isGreaterThan() throws ParseException {
        assertThat(new Version("2")).isGreaterThan(new Version("1.0.0"));
    }

    /**
     * Verify that the {@link Version#compareTo(Version)} method returns a negative integer
     * if passed another instance created with a more precise, but larger, version string.
     */
    @Test
    public void testCompareTo_withMorePreciseLaterVersion_isLessThan() throws ParseException {
        assertThat(new Version("1")).isLessThan(new Version("1.0.1"));
    }
}
