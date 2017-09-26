package org.edx.mobile.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

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

    /**
     * Parameterize test cases class for {@link Version#isNMinorVersionsDiff(Version, int)} method,
     * method should return true if passed another instance with greater or equal minor versions
     * difference than the specified value.
     */
    @RunWith(Parameterized.class)
    public static class ParameterizedTest_isNMinorVersionsDiff {
        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    // true test cases
                    {"2.3.6", "2.5.6", 2, true},
                    {"2.5.6", "2.3.6", 2, true},
                    {"2.5.6", "2.5.6", 0, true},
                    {"2.5.6", "2.1.6", 4, true},
                    {"2.5.6", "2.9.6", 4, true},
                    {"2.5.6", "3.5.6", 4, true},
                    {"2.2.0", "2", 2, true},
                    {"2.2.0", "3", 4, true},
                    // false test cases
                    {"2.2.5", "2.2.5", 2, false},
                    {"2.2.5", "2.3.5", 2, false},
                    {"2.2.5", "2.5.5", 4, false},
                    {"2.1.5", "2", 2, false},
            });
        }

        private Version firstVersion;
        private Version secondVersion;
        private int minorVersionsDiff;
        private boolean expected;

        public ParameterizedTest_isNMinorVersionsDiff(String firstVersion, String secondVersion,
                                                      int minorVersionsDiff, boolean expected)
                throws ParseException {
            this.firstVersion = new Version(firstVersion);
            this.secondVersion = new Version(secondVersion);
            this.minorVersionsDiff = minorVersionsDiff;
            this.expected = expected;
        }

        @Test
        public void test() throws ParseException {
            assertThat(firstVersion.isNMinorVersionsDiff(secondVersion, minorVersionsDiff)).isEqualTo(expected);
        }
    }

    /**
     * Parameterize test cases class for {@link Version#hasSameMajorMinorVersion(Version)} method,
     * method should return true if passed another instance having same major and minor version.
     */
    @RunWith(Parameterized.class)
    public static class SameMajorMinorVersionTest {
        @Parameterized.Parameters(name = "Test#{index}: first version = {0}, second version = {1}, " +
                "expected = {2}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    // true test cases
                    {"2.8.1", "2.8.6", true},
                    {"2.8", "2.8.6", true},
                    // false test cases
                    {"2.8.1", "2.9.1", false},
                    {"2.8.1", "3.8.1", false},
            });
        }

        private Version firstVersion;
        private Version secondVersion;
        private boolean expected;

        public SameMajorMinorVersionTest(String firstVersion, String secondVersion,
                                         boolean expected) throws ParseException {
            this.firstVersion = new Version(firstVersion);
            this.secondVersion = new Version(secondVersion);
            this.expected = expected;
        }

        @Test
        public void testHasSameMajorMinorVersion() throws ParseException {
            assertThat(firstVersion.hasSameMajorMinorVersion(secondVersion)).isEqualTo(expected);
        }
    }
}
