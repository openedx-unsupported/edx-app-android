package org.edx.mobile.event;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.internal.bind.util.ISO8601Utils;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.http.interceptor.NewVersionBroadcastInterceptor;
import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.util.Version;
import org.junit.AssumptionViolatedException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.InOrder;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.edx.mobile.http.HttpStatus.ACCEPTED;
import static org.edx.mobile.http.HttpStatus.UPGRADE_REQUIRED;
import static org.edx.mobile.http.interceptor.NewVersionBroadcastInterceptor.HEADER_APP_LATEST_VERSION;
import static org.edx.mobile.http.interceptor.NewVersionBroadcastInterceptor.HEADER_APP_VERSION_LAST_SUPPORTED_DATE;
import static org.edx.mobile.test.util.TimeUtilsForTests.DEFAULT_TIME;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for verifying implementation correctness of {@link NewVersionAvailableEvent}, and the
 * modules that post it (to ensure that they're respecting it's priority ordering while posting it),
 * such as {@link NewVersionBroadcastInterceptor}.
 */
/* TODO: Once a custom Snackbar shadow is implemented, UI tests should be written to test the event
 * bus registration for this event, and the subsequent display of the appropriate Snackbar
 * notification.
 */
public class NewVersionAvailableEventTest extends BaseTestCase {
    /**
     * Verify that constructing a new instance of {@link NewVersionAvailableEvent} with empty
     * parameters (@{code null} or {@code false}) throws an {@link IllegalArgumentException}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNewInstance_withNoInput_throwsException() {
        new NewVersionAvailableEvent(null, null, false);
    }

    /**
     * Verify that constructing a new instance of {@link NewVersionAvailableEvent}, with the current
     * version number provided as the new version, and empty values (@{code null} or {@code false})
     * provided for all the other parameters, throws an {@link IllegalArgumentException}.
     *
     * @throws ParseException If the current version doesn't correspond to the schema.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNewInstance_withOnlyCurrentVersionInput_throwsException()
            throws ParseException {
        new NewVersionAvailableEvent(getVersionOffset(0), null, false);
    }

    /**
     * Verify that constructing a new instance of {@link NewVersionAvailableEvent}, with an older
     * version number provided as the new version, and empty values (@{code null} or {@code false})
     * provided for all the other parameters, throws an {@link IllegalArgumentException}.
     *
     * @throws ParseException If the current version doesn't correspond to the schema.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNewInstance_withOnlyOldVersionInput_throwsException() throws ParseException {
        new NewVersionAvailableEvent(getVersionOffset(-1), null, false);
    }

    /**
     * Verify that attempting to post a {@link NewVersionAvailableEvent} instance through the
     * {@link NewVersionAvailableEvent#post(Version, Date, boolean)} method with empty parameters
     * (@{code null} or {@code false}) doesn't do anything.
     */
    @Test
    public void testPost_withNoInput_doesNothing() {
        assertNull(postAndRemoveEvent(null, null, false));
    }

    /**
     * Verify that attempting to post a {@link NewVersionAvailableEvent} instance through the
     * {@link NewVersionAvailableEvent#post(Version, Date, boolean)} method, with the current
     * version number provided as the new version, and empty values (@{code null} or {@code false})
     * provided for all the other parameters, doesn't do anything.
     *
     * @throws ParseException If the current version doesn't correspond to the schema.
     */
    @Test
    public void testPost_withOnlyCurrentVersionInput_doesNothing() throws ParseException {
        assertNull(postAndRemoveEvent(getVersionOffset(0), null, false));
    }

    /**
     * Verify that attempting to post a {@link NewVersionAvailableEvent} instance through the
     * {@link NewVersionAvailableEvent#post(Version, Date, boolean)} method, with an older
     * version number provided as the new version, and empty values (@{code null} or {@code false})
     * provided for all the other parameters, doesn't do anything.
     *
     * @throws ParseException If the current version doesn't correspond to the schema.
     */
    @Test
    public void testPost_withOnlyOldVersionInput__doesNothing() throws ParseException {
        assertNull(postAndRemoveEvent(getVersionOffset(-1), null, false));
    }

    /**
     * Verify that attempting to post a {@link NewVersionAvailableEvent} instance by passing the
     * appropriate headers and status code in a request chain through an
     * {@link NewVersionBroadcastInterceptor} with empty parameters (@{code null} or {@code false})
     * doesn't do anything.
     */
    @Test
    public void testPostFromInterceptor_withNoInput_doesNothing() throws IOException {
        assertNull(postAndRemoveEventFromInterceptor(null, null, false));
    }

    /**
     * Verify that attempting to post a {@link NewVersionAvailableEvent} instance by passing the
     * appropriate headers and status code in a request chain through an
     * {@link NewVersionBroadcastInterceptor}, with the current version number provided as the new
     * version, and empty values (@{code null} or {@code false}) provided for all the other
     * parameters, doesn't do anything.
     */
    @Test
    public void testPostFromInterceptor_withOnlyCurrentVersionInput_doesNothing()
            throws IOException, ParseException {
        assertNull(postAndRemoveEventFromInterceptor(getVersionOffset(0), null, false));
    }

    /**
     * Verify that attempting to post a {@link NewVersionAvailableEvent} instance by passing the
     * appropriate headers and status code in a request chain through an
     * {@link NewVersionBroadcastInterceptor}, with an older version number provided as the new
     * version, and empty values (@{code null} or {@code false}) provided for all the other
     * parameters, doesn't do anything.
     */
    @Test
    public void testPostFromInterceptor_withOnlyOldVersionInput__doesNothing()
            throws IOException, ParseException {
        assertNull(postAndRemoveEventFromInterceptor(getVersionOffset(-1), null, false));
    }

    /**
     * Tests for verifying implementation correctness on all valid construction parameter
     * permutations of {@link NewVersionAvailableEvent}, and on the modules that post it, such as
     * {@link NewVersionBroadcastInterceptor}.
     */
    @RunWith(Parameterized.class)
    public static class PermutationsValidityTest {
        /**
         * @return A list of all the parameter permutations possible for constructing a valid
         * {@link NewVersionAvailableEvent}, on which the tests will be iterated.
         *
         * @throws ParseException If the current version doesn't correspond to the schema.
         */
        @Parameters(name = "{index}: new version = {0}, last supported date = {1}, " +
                "unsupported = {2}")
        @NonNull
        public static Iterable<Object[]> getParameterPermutations() throws ParseException {
            final List<Object[]> data = new ArrayList<>();
            final Version currentVersion = new Version(BuildConfig.VERSION_NAME);
            for (final Version newVersion : new Version[] {
                    getVersionOffset(1), currentVersion, getVersionOffset(-1), null }) {
                for (final Date lastSupportedDate : new Date[] { new Date(DEFAULT_TIME), null }) {
                    for (final boolean isUnsupported : new boolean[] { true, false }) {
                        if (isUnsupported || lastSupportedDate != null ||
                                (newVersion != null && newVersion.compareTo(currentVersion) > 0)) {
                            data.add(new Object[] { newVersion, lastSupportedDate, isUnsupported });
                        }
                    }
                }
            }
            return data;
        }

        /**
         * The new version;
         */
        @Nullable
        private final Version newVersion;
        /**
         * The last supported date.
         */
        @Nullable
        private final Date lastSupportedDate;
        /**
         * Whether the current version is unsupported.
         */
        private final boolean isUnsupported;

        /**
         * Construct a new iteration of the test suite with a specific permutation of the parameters
         * for constructing a {@link NewVersionAvailableEvent} instance.
         */
        public PermutationsValidityTest(@Nullable final Version newVersion,
                                        @Nullable final Date lastSupportedDate,
                                        final boolean isUnsupported) {
            this.newVersion = newVersion;
            // Don't reuse a single Date instance across tests, as it's not immutable.
            this.lastSupportedDate = lastSupportedDate == null ? null :
                    (Date) lastSupportedDate.clone();
            this.isUnsupported = isUnsupported;
        }

        /**
         * Verify that all the getters of {@link NewVersionAvailableEvent} correctly return the data
         * that was provided to it on instantiation.
         */
        @Test
        public void testGetters_returnCorrectValues() {
            final NewVersionAvailableEvent event = new NewVersionAvailableEvent(
                    newVersion, lastSupportedDate, isUnsupported);
            assertEquals(newVersion, event.getNewVersion());
            assertEquals(lastSupportedDate, event.getLastSupportedDate());
            assertEquals(isUnsupported, event.isUnsupported());
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#equals(Object)} method returns
         * {@code true} if passed another instance created with an identical (but different) set of
         * parameters.
         *
         * @throws ParseException If the current version doesn't correspond to the schema.
         */
        @Test
        public void testEquals_withIdenticalInstance_isTrue() throws ParseException {
            final NewVersionAvailableEvent event = new NewVersionAvailableEvent(
                    newVersion, lastSupportedDate, isUnsupported);
            final NewVersionAvailableEvent clone = new NewVersionAvailableEvent(
                    newVersion == null ? null : new Version(newVersion.toString()),
                    lastSupportedDate == null ? null : (Date) lastSupportedDate.clone(),
                    isUnsupported);
            assertEquals(event, clone);
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#compareTo(NewVersionAvailableEvent)}
         * method returns zero if passed another instance created with an identical (but different)
         * set of parameters.
         */
        @Test
        public void testCompare_withIdenticalInstance_isEqual() {
            final NewVersionAvailableEvent event = new NewVersionAvailableEvent(
                    newVersion, lastSupportedDate, isUnsupported);
            assertThat(event).isEqualByComparingTo(new NewVersionAvailableEvent(
                    newVersion, lastSupportedDate, isUnsupported));
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#isConsumed} method returns {@code false}
         * immediately after initialization.
         */
        @Test
        public void testIsConsumed_atInitialization_isFalse() {
            final NewVersionAvailableEvent event = new NewVersionAvailableEvent(
                    newVersion, lastSupportedDate, isUnsupported);
            assertFalse(event.isConsumed());
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#isConsumed} method returns {@code true}
         * after being marked as consumed via the {@link NewVersionAvailableEvent#markAsConsumed()}
         * method.
         */
        @Test
        public void testIsConsumed_afterMarkingAsConsumed_isTrue() {
            final NewVersionAvailableEvent event = new NewVersionAvailableEvent(
                    newVersion, lastSupportedDate, isUnsupported);
            event.markAsConsumed();
            assertTrue(event.isConsumed());
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#getNotificationString(Context)} returns a
         * non-null value.
         */
        // TODO: Include this once we have a Robolectric parameterized test runner.
        @Ignore
        @Test
        public void testGetNotificationString_returnsNonNull() {
            final NewVersionAvailableEvent event = new NewVersionAvailableEvent(
                    newVersion, lastSupportedDate, isUnsupported);
            assertNotNull(event.getNotificationString(RuntimeEnvironment.application));
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#post(Version, Date, boolean)} method
         * posts an instance of {@link NewVersionAvailableEvent} on the event bus as a sticky event,
         * and with the same data that was passed to it.
         */
        @Test
        public void testPost_postsEventOnBus() {
            final NewVersionAvailableEvent event = postAndRemoveEvent(
                    newVersion, lastSupportedDate, isUnsupported);
            assertEquals(event, new NewVersionAvailableEvent(
                    newVersion, lastSupportedDate, isUnsupported));
        }

        /**
         * Verify that the {@link NewVersionBroadcastInterceptor} posts an instance of
         * {@link NewVersionAvailableEvent} on the event bus as a sticky event, and with the same
         * data that was provided in the headers and the status code in it's request chain.
         */
        @Test
        public void testPostFromInterceptor_postsEventOnBus() throws IOException {
            final NewVersionAvailableEvent event = postAndRemoveEventFromInterceptor(
                    newVersion, lastSupportedDate, isUnsupported);
            assertEquals(event, new NewVersionAvailableEvent(
                    newVersion, lastSupportedDate, isUnsupported));
        }
    }

    /**
     * Tests for verifying implementation correctness of the comparison logic using all valid non-
     * identical permutations of the combination of two sets of parameters for
     * {@link NewVersionAvailableEvent}, on the event class itself, and also the modules that post
     * it, such as {@link NewVersionBroadcastInterceptor}.
     */
    @RunWith(Parameterized.class)
    public static class ComparisonTests {
        /**
         * @return A list of all valid permutations containing two instances of
         * {@link NewVersionAvailableEvent} that were created with a different set of parameters, on
         * which the comparison tests will be iterated. The first item will always be of a higher
         * priority (and comparison order) than the second one.
         *
         * @throws ParseException If the current version doesn't correspond to the schema.
         */
        @Parameters(name = "{index}: higher priority event = {0}, lower priority event = {1}")
        @NonNull
        public static Iterable<Object[]> getDifferentParametersPermutations()
                throws ParseException {
            final List<Object[]> data = new ArrayList<>();

            final Version[] newVersionsSet =
                    new Version[] { getVersionOffset(2), getVersionOffset(1), null };
            final Date[] lastSupportedDatesSet =
                    new Date[] { new Date(DEFAULT_TIME), new Date(DEFAULT_TIME + 1), null };
            final boolean[] isUnsupportedValuesSet = new boolean[] { true, false };

            /* Add all the valid permutations where the event has higher priority based on the new
             * version parameter. Since this is the parameter that is compared last, all the other
             * parameters must be the same on both event sets here.
             */
            for (final Date lastSupportedDate : lastSupportedDatesSet) {
                for (final boolean isUnsupported : isUnsupportedValuesSet) {
                    for (int newVersionIndex = 0; newVersionIndex < newVersionsSet.length - 1;
                            newVersionIndex++) {
                        if (isUnsupported || lastSupportedDate != null ||
                                (newVersionsSet[newVersionIndex] != null &&
                                        newVersionsSet[newVersionIndex + 1] != null)) {
                            data.add(new Object[] {
                                    new NewVersionAvailableEvent(
                                            newVersionsSet[newVersionIndex],
                                            // Don't reuse a single Date
                                            // instance, as it's not immutable.
                                            lastSupportedDate == null ? null :
                                                    (Date) lastSupportedDate.clone(),
                                            isUnsupported),
                                    new NewVersionAvailableEvent(
                                            newVersionsSet[newVersionIndex + 1],
                                            // Don't reuse a single Date
                                            // instance, as it's not immutable.
                                            lastSupportedDate == null ? null :
                                                    (Date) lastSupportedDate.clone(),
                                            isUnsupported)
                            });
                        }
                    }
                }
            }
            /* Add all the valid permutations where the event has higher priority based on the last
             * supported date parameter. The new version parameter is compared after this one, so
             * it's permutations are not restricted here. The unsupported flag parameter is
             * compared at the start, so it must be the same on both event sets here.
             */
            for (final boolean isUnsupported : isUnsupportedValuesSet) {
                for (int lastSupportedDateIndex = 0; lastSupportedDateIndex <
                        lastSupportedDatesSet.length - 1; lastSupportedDateIndex++) {
                    for (int newVersionIndex = 0; newVersionIndex < newVersionsSet.length - 1;
                            newVersionIndex++) {
                        if (isUnsupported ||
                                (lastSupportedDatesSet[lastSupportedDateIndex] != null &&
                                        lastSupportedDatesSet[lastSupportedDateIndex + 1] != null)
                                || (newVersionsSet[newVersionIndex] != null &&
                                        newVersionsSet[newVersionIndex + 1] != null)) {
                            for (final boolean reverseNewVersionIndexComparison :
                                    new boolean[] { false, true }) {
                                data.add(new Object[] {
                                        new NewVersionAvailableEvent(
                                                newVersionsSet[reverseNewVersionIndexComparison ?
                                                        newVersionIndex + 1 : newVersionIndex],
                                                // Don't reuse a single Date
                                                // instance, as it's not immutable.
                                                lastSupportedDatesSet[lastSupportedDateIndex] ==
                                                        null ? null : (Date) lastSupportedDatesSet[
                                                        lastSupportedDateIndex].clone(),
                                                isUnsupported),
                                        new NewVersionAvailableEvent(
                                                newVersionsSet[reverseNewVersionIndexComparison ?
                                                        newVersionIndex : newVersionIndex + 1],
                                                // Don't reuse a single Date
                                                // instance, as it's not immutable.
                                                lastSupportedDatesSet[
                                                        lastSupportedDateIndex + 1] == null ? null :
                                                        (Date) lastSupportedDatesSet[
                                                                lastSupportedDateIndex + 1].clone(),
                                                isUnsupported)
                                });
                            }
                        }
                    }
                }
            }
            /* Add all the valid permutations where the event has higher priority based on the
             * unsupported flag parameter. This is the first parameter that is compared, so all the
             * other parameter permutations are completely unrestricted here.
             */
            for (int isUnsupportedIndex = 0; isUnsupportedIndex < isUnsupportedValuesSet.length - 1;
                    isUnsupportedIndex++) {
                for (int lastSupportedDateIndex = 0; lastSupportedDateIndex <
                        lastSupportedDatesSet.length - 1; lastSupportedDateIndex++) {
                    for (int newVersionIndex = 0; newVersionIndex < newVersionsSet.length - 1;
                            newVersionIndex++) {
                        if ((isUnsupportedValuesSet[isUnsupportedIndex] &&
                                        isUnsupportedValuesSet[isUnsupportedIndex + 1]) ||
                                (lastSupportedDatesSet[lastSupportedDateIndex] != null &&
                                        lastSupportedDatesSet[lastSupportedDateIndex + 1] != null)
                                || (newVersionsSet[newVersionIndex] != null &&
                                        newVersionsSet[newVersionIndex + 1] != null)) {
                            for (final boolean reverseLastSupportedDateComparison :
                                    new boolean[] { false, true }) {
                                for (final boolean reverseNewVersionComparison :
                                        new boolean[] { false, true }) {
                                    data.add(new Object[] {
                                            new NewVersionAvailableEvent(newVersionsSet[
                                                    reverseNewVersionComparison ?
                                                            newVersionIndex + 1 : newVersionIndex],
                                                    // Don't reuse a single Date
                                                    // instance, as it's not immutable.
                                                    lastSupportedDatesSet[
                                                            reverseLastSupportedDateComparison ?
                                                                    lastSupportedDateIndex + 1 :
                                                                    lastSupportedDateIndex] == null
                                                            ? null : (Date) lastSupportedDatesSet[
                                                            reverseLastSupportedDateComparison ?
                                                                    lastSupportedDateIndex + 1 :
                                                                    lastSupportedDateIndex].clone(),
                                                    isUnsupportedValuesSet[isUnsupportedIndex]),
                                            new NewVersionAvailableEvent(newVersionsSet[
                                                    reverseNewVersionComparison ?
                                                            newVersionIndex : newVersionIndex + 1],
                                                    // Don't reuse a single Date
                                                    // instance, as it's not immutable.
                                                    lastSupportedDatesSet[
                                                            reverseLastSupportedDateComparison ?
                                                                    lastSupportedDateIndex :
                                                                    lastSupportedDateIndex + 1] ==
                                                            null ? null :
                                                            (Date) lastSupportedDatesSet[
                                                            reverseLastSupportedDateComparison ?
                                                                    lastSupportedDateIndex :
                                                                    lastSupportedDateIndex + 1]
                                                                    .clone(),
                                                    isUnsupportedValuesSet[isUnsupportedIndex + 1]),
                                    });
                                }
                            }
                        }
                    }
                }
            }

            return data;
        }

        /**
         * The higher priority event instance.
         */
        @NonNull
        private final NewVersionAvailableEvent higherPriorityEvent;
        /**
         * The lower priority event instance.
         */
        @NonNull
        private final NewVersionAvailableEvent lowerPriorityEvent;

        /**
         * Construct a new iteration of the test suite with a specific permutation of the set of two
         * {@link NewVersionAvailableEvent} instances with variable parameters.
         *
         * @param higherPriorityEvent The higher priority instance.
         * @param lowerPriorityEvent The lower priority instance.
         */
        public ComparisonTests(@NonNull NewVersionAvailableEvent higherPriorityEvent,
                               @NonNull NewVersionAvailableEvent lowerPriorityEvent) {
            this.higherPriorityEvent = higherPriorityEvent;
            this.lowerPriorityEvent = lowerPriorityEvent;
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#compareTo(NewVersionAvailableEvent)}
         * method returns a positive integer if passed another instance that is of lower priority.
         */
        @Test
        public void testCompare_withLowerPriorityInstance_isGreaterThan() {
            assertThat(higherPriorityEvent).isGreaterThan(lowerPriorityEvent);
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#compareTo(NewVersionAvailableEvent)}
         * method returns a negative integer if passed another instance that is of higher priority.
         */
        @Test
        public void testCompare_withHigherPriorityInstance_isLessThan() {
            assertThat(lowerPriorityEvent).isLessThan(higherPriorityEvent);
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#post(Version, Date, boolean)} method
         * doesn't do anything when provided parameters that resolve into a lower priority event
         * than one that has already been posted.
         */
        @Test
        public void testPost_lowerPriorityEvent_doesNothing() {
            assertNotEquals(higherPriorityEvent, lowerPriorityEvent);
            // This will throw an AssumptionViolatedException if the Android runtime
            // isn't loaded (i.e. through using the Robolectric test runner).
            final EventBus eventBus = getEventBus();
            eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
            assertEquals(higherPriorityEvent, postEvent(higherPriorityEvent));
            assertEquals(higherPriorityEvent, postEvent(lowerPriorityEvent));
            eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
        }

        /**
         * Verify that the {@link NewVersionAvailableEvent#post(Version, Date, boolean)} method
         * posts an instance of {@link NewVersionAvailableEvent} on the event bus as a sticky event
         * correctly, when provided parameters that resolve into a higher priority event than one
         * that has already been posted.
         */
        @Test
        public void testPost_higherPriorityEvent_postsEventOnBus() {
            assertNotEquals(higherPriorityEvent, lowerPriorityEvent);
            // This will throw an AssumptionViolatedException if the Android runtime
            // isn't loaded (i.e. through using the Robolectric test runner).
            final EventBus eventBus = getEventBus();
            eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
            assertEquals(lowerPriorityEvent, postEvent(lowerPriorityEvent));
            assertEquals(higherPriorityEvent, postEvent(higherPriorityEvent));
            eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
        }

        /**
         * Verify that the {@link NewVersionBroadcastInterceptor} doesn't do anything when provided
         * data (headers and status code) in it's request chain that resolve into a lower priority
         * event than one that has already been posted.
         */
        @Test
        public void testPostFromInterceptor_lowerPriorityEvent_doesNothing() throws IOException {
            assertNotEquals(higherPriorityEvent, lowerPriorityEvent);
            // This will throw an AssumptionViolatedException if the Android runtime
            // isn't loaded (i.e. through using the Robolectric test runner).
            final EventBus eventBus = getEventBus();
            eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
            final NewVersionBroadcastInterceptor interceptor = new NewVersionBroadcastInterceptor();
            assertEquals(higherPriorityEvent, postEventFromInterceptor(
                    interceptor, higherPriorityEvent));
            assertEquals(higherPriorityEvent, postEventFromInterceptor(
                    interceptor, lowerPriorityEvent));
            eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
        }

        /**
         * Verify that the {@link NewVersionBroadcastInterceptor} posts an instance of
         * {@link NewVersionAvailableEvent} on the event bus as a sticky event correctly, when
         * provided data (headers and status code) in it's request chain that resolve into a higher
         * priority event than one that has already been posted.
         */
        @Test
        public void testPostFromInterceptor_higherPriorityEvent_postsEventOnBus()
                throws IOException {
            assertNotEquals(higherPriorityEvent, lowerPriorityEvent);
            // This will throw an AssumptionViolatedException if the Android runtime
            // isn't loaded (i.e. through using the Robolectric test runner).
            final EventBus eventBus = getEventBus();
            eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
            final NewVersionBroadcastInterceptor interceptor = new NewVersionBroadcastInterceptor();
            assertEquals(lowerPriorityEvent, postEventFromInterceptor(
                    interceptor, lowerPriorityEvent));
            assertEquals(higherPriorityEvent, postEventFromInterceptor(
                    interceptor, higherPriorityEvent));
            eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
        }

        /**
         * Post the provided event's data via the
         * {@link NewVersionAvailableEvent#post(Version, Date, boolean)} method.
         *
         * @param event The event whose data is to be posted.
         * @return The event instance that was actually posted.
         */
        @NonNull
        private static NewVersionAvailableEvent postEvent(
                @NonNull final NewVersionAvailableEvent event) {
            // Since we already have the event generated, we can be sure that
            // the data is valid, and should in fact be posted.
            //noinspection ConstantConditions
            return NewVersionAvailableEventTest.postEvent(event.getNewVersion(),
                    event.getLastSupportedDate(), event.isUnsupported());
        }

        /**
         * Post the provided event's data by passing the appropriate headers and status code in a
         * request chain through an {@link NewVersionBroadcastInterceptor}.
         *
         * @param event The event whose data is to be posted.
         * @return The event instance that was actually posted.
         */
        @NonNull
        private static NewVersionAvailableEvent postEventFromInterceptor(
                @NonNull final NewVersionBroadcastInterceptor interceptor,
                @NonNull final NewVersionAvailableEvent event) throws IOException {
            // Since we already have the event generated, we can be sure that
            // the data is valid, and should in fact be posted.
            //noinspection ConstantConditions
            return NewVersionAvailableEventTest.postEventFromInterceptor(interceptor,
                    event.getNewVersion(), event.getLastSupportedDate(), event.isUnsupported());
        }
    }

    /**
     * Post the provided data via the {@link NewVersionAvailableEvent#post(Version, Date, boolean)}
     * method.
     *
     * @param newVersion The new version.
     * @param lastSupportedDate The last supported date.
     * @param isUnsupported Whether the current version is unsupported.
     * @return The posted event. This can be null if the data does not constitute a valid event.
     */
    @Nullable
    private static NewVersionAvailableEvent postEvent(@Nullable final Version newVersion,
                                                      @Nullable final Date lastSupportedDate,
                                                      final boolean isUnsupported) {
        // This will throw an AssumptionViolatedException if the Android runtime
        // isn't loaded (i.e. through using the Robolectric test runner).
        final EventBus eventBus = getEventBus();
        NewVersionAvailableEvent.post(newVersion, lastSupportedDate, isUnsupported);
        return eventBus.getStickyEvent(NewVersionAvailableEvent.class);
    }

    /**
     * Post the provided data via the {@link NewVersionAvailableEvent#post(Version, Date, boolean)}
     * method, then remove the sticky event from the event bus.
     *
     * @param newVersion The new version.
     * @param lastSupportedDate The last supported date.
     * @param isUnsupported Whether the current version is unsupported.
     * @return The event that was posted. This can be null if the data does not constitute a valid
     * event.
     */
    @Nullable
    private static NewVersionAvailableEvent postAndRemoveEvent(
            @Nullable final Version newVersion,
            @Nullable final Date lastSupportedDate,
            final boolean isUnsupported) {
        // This will throw an AssumptionViolatedException if the Android runtime
        // isn't loaded (i.e. through using the Robolectric test runner).
        final EventBus eventBus = getEventBus();
        eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
        final NewVersionAvailableEvent event =
                postEvent(newVersion, lastSupportedDate, isUnsupported);
        if (event != null) {
            assertTrue(eventBus.removeStickyEvent(event));
        }
        return event;
    }

    /**
     * Post the provided data by passing the appropriate headers and status code in a request chain
     * through an {@link NewVersionBroadcastInterceptor}.
     *
     * @param interceptor The interceptor through which the data is to be posted.
     * @param newVersion The new version.
     * @param lastSupportedDate The last supported date.
     * @param isUnsupported Whether the current version is unsupported.
     * @return The posted event. This can be null if the data does not constitute a valid event.
     */
    @Nullable
    private static NewVersionAvailableEvent postEventFromInterceptor(
            @NonNull final NewVersionBroadcastInterceptor interceptor,
            @Nullable final Version newVersion,
            @Nullable final Date lastSupportedDate,
            final boolean isUnsupported) throws IOException {
        // This will throw an AssumptionViolatedException if the Android runtime
        // isn't loaded (i.e. through using the Robolectric test runner).
        final EventBus eventBus = getEventBus();
        eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
        final Interceptor.Chain chain = mock(Interceptor.Chain.class);
        final Request request = new Request.Builder()
                .url("https://localhost:1/")
                .build();
        final Response response; {
            final Response.Builder responseBuilder = new Response.Builder();
            responseBuilder.request(request);
            responseBuilder.protocol(Protocol.HTTP_1_1);
            responseBuilder.code(isUnsupported ? UPGRADE_REQUIRED : ACCEPTED);
            responseBuilder.message("");
            if (newVersion != null) {
                responseBuilder.header(HEADER_APP_LATEST_VERSION, newVersion.toString());
            }
            if (lastSupportedDate != null) {
                responseBuilder.header(HEADER_APP_VERSION_LAST_SUPPORTED_DATE,
                        ISO8601Utils.format(lastSupportedDate, true));
            }
            response = responseBuilder.build();
        }
        when(chain.request()).thenReturn(request);
        when(chain.proceed(request)).thenReturn(response);
        interceptor.intercept(chain);
        final InOrder inOrder = inOrder(chain);
        inOrder.verify(chain).request();
        inOrder.verify(chain).proceed(request);
        verifyNoMoreInteractions(chain);
        return eventBus.getStickyEvent(NewVersionAvailableEvent.class);
    }

    /**
     * Post the provided data by passing the appropriate headers and status code in a request chain
     * through an {@link NewVersionBroadcastInterceptor}, then remove the sticky event from the
     * event bus.
     *
     * @param newVersion The new version.
     * @param lastSupportedDate The last supported date.
     * @param isUnsupported Whether the current version is unsupported.
     * @return The event that was posted. This can be null if the data does not constitute a valid
     * event.
     */
    @Nullable
    private static NewVersionAvailableEvent postAndRemoveEventFromInterceptor(
            @Nullable final Version newVersion,
            @Nullable final Date lastSupportedDate,
            final boolean isUnsupported) throws IOException {
        // This will throw an AssumptionViolatedException if the Android runtime
        // isn't loaded (i.e. through using the Robolectric test runner).
        final EventBus eventBus = getEventBus();
        eventBus.removeStickyEvent(NewVersionAvailableEvent.class);
        final NewVersionAvailableEvent event = postEventFromInterceptor(
                new NewVersionBroadcastInterceptor(), newVersion, lastSupportedDate, isUnsupported);
        if (event != null) {
            assertTrue(eventBus.removeStickyEvent(event));
        }
        return event;
    }

    /**
     * Get the event bus that's being used in the app.
     *
     * @return The event bus.
     * @throws AssumptionViolatedException If the default event bus can't be constructed because of
     * the Android framework not being loaded. This will stop the calling tests from being reported
     * as failures.
     */
    @NonNull
    private static EventBus getEventBus() {
        try {
            return EventBus.getDefault();
        } catch (RuntimeException e) {
            /* The event bus uses the Looper from the Android framework, so
             * initializing it would throw a runtime exception if the
             * framework is not loaded. Nor can RoboGuice be used to inject
             * a mocked instance to get around this issue, since customizing
             * RoboGuice injections requires a Context.
             *
             * Robolectric can't be used to solve this issue, because it
             * doesn't support parameterized testing. The only solution that
             * could work at the moment would be to make this an
             * instrumented test suite.
             *
             * TODO: Mock the event bus when RoboGuice is replaced with
             * another dependency injection framework, or when there is a
             * Robolectric test runner available that support parameterized
             * tests.
             */
            throw new AssumptionViolatedException(
                    "Event bus requires Android framework", e, nullValue());
        }
    }

    /**
     * Construct and return a {@link Version} instance at the specified offset from the current
     * build's version number.
     *
     * @param offset The offset from the current build's version.
     * @return The version at the specified offset.
     * @throws ParseException If the current version doesn't correspond to the schema.
     */
    @NonNull
    private static Version getVersionOffset(final int offset) throws ParseException {
        final Version currentVersion = new Version(BuildConfig.VERSION_NAME);
        if (offset == 0) {
            return currentVersion;
        }
        int majorVersion = currentVersion.getMajorVersion();
        int minorVersion = currentVersion.getMinorVersion();
        int patchVersion = currentVersion.getPatchVersion();
        if (offset > 0) {
            patchVersion += offset;
        } else {
            if      (patchVersion > 0) patchVersion += offset;
            else if (minorVersion > 0) minorVersion += offset;
            else if (majorVersion > 0) majorVersion += offset;
            else throw new IllegalStateException("Version must be a positive number: " +
                        currentVersion);
        }
        return new Version(majorVersion + "." + minorVersion + "." + patchVersion);
    }
}
