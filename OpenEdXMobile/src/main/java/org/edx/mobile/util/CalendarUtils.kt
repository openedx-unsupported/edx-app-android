package org.edx.mobile.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.ContentMetadata
import io.branch.referral.util.LinkProperties
import org.edx.mobile.R
import org.edx.mobile.core.IEdxEnvironment
import org.edx.mobile.deeplink.DeepLink
import org.edx.mobile.deeplink.Screen
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.course.CourseDateBlock
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
object CalendarUtils {
    private val logger = Logger(DateUtil::class.java.name)
    private const val REMINDER_24_HOURS = 24 * 60
    private const val REMINDER_48_HOURS = 2 * 24 * 60
    private const val LOCAL_USER = "local_user"

    val permissions = arrayOf(android.Manifest.permission.WRITE_CALENDAR, android.Manifest.permission.READ_CALENDAR)

    /**
     * Check if the app has the calendar READ/WRITE permissions or not
     */
    fun hasPermissions(context: Context): Boolean = permissions.all { permission ->
        PermissionsUtil.checkPermissions(permission, context)
    }

    /**
     * Check if the calendar is already existed in mobile calendar app or not
     */
    fun isCalendarExists(context: Context, accountName: String, calendarTitle: String): Boolean {
        if (hasPermissions(context)) {
            return getCalendarId(context, accountName, calendarTitle) != -1L
        }
        return false
    }

    /**
     * Create or update the calendar if it is already existed in mobile calendar app
     */
    @JvmStatic
    fun createOrUpdateCalendar(
        context: Context,
        accountName: String,
        accountType: String = CalendarContract.ACCOUNT_TYPE_LOCAL,
        calendarTitle: String
    ): Long {
        val calendarId = getCalendarId(
            context = context,
            accountName = accountName,
            calendarTitle = calendarTitle
        )

        if (calendarId != -1L) {
            deleteCalendar(context = context, calendarId = calendarId)
        }

        return createCalendar(
            context = context,
            accountName = accountName,
            accountType = accountType,
            calendarTitle = calendarTitle
        )
    }

    /**
     * Method to create a separate calendar based on course name in mobile calendar app
     */
    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun createCalendar(
        context: Context,
        accountName: String,
        accountType: String,
        calendarTitle: String
    ): Long {
        val contentValues = ContentValues()
        contentValues.put(CalendarContract.Calendars.NAME, calendarTitle)
        contentValues.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, calendarTitle)
        contentValues.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
        contentValues.put(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
        contentValues.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName)
        contentValues.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_ROOT)
        contentValues.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
        contentValues.put(CalendarContract.Calendars.VISIBLE, 1)
        contentValues.put(CalendarContract.Calendars.CALENDAR_COLOR, ContextCompat.getColor(context, R.color.primaryBaseColor))
        val creationUri: Uri? = asSyncAdapter(
            Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString()),
            accountName,
            accountType
        )
        creationUri?.let {
            val calendarData: Uri? = context.contentResolver.insert(creationUri, contentValues)
            calendarData?.let {
                val id = calendarData.lastPathSegment.toLong()
                logger.debug("Calendar ID $id")
                return id
            }
        }
        return -1
    }

    /**
     * Method to check if the calendar with the course name exist in the mobile calendar app or not
     */
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun getCalendarId(context: Context, accountName: String, calendarTitle: String): Long {
        var calendarId = -1
        val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.NAME)
        val calendarContentResolver = context.contentResolver
        val cursor: Cursor = calendarContentResolver.query(
                CalendarContract.Calendars.CONTENT_URI, projection,
                CalendarContract.Calendars.ACCOUNT_NAME + "=? and (" +
                        CalendarContract.Calendars.NAME + "=? or " +
                        CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + "=?)", arrayOf(accountName, calendarTitle,
                calendarTitle), null)
        if (cursor.moveToFirst()) {
            if (cursor.getString(cursor.getColumnIndex(CalendarContract.Calendars.NAME))
                    .equals(calendarTitle)
            ) {
                calendarId = cursor.getInt(cursor.getColumnIndex(CalendarContract.Calendars._ID))
            }
        }
        cursor.close()
        return calendarId.toLong()
    }

    /**
     * Method to add important dates of course as calendar event into calendar of mobile app
     */
    @JvmStatic
    fun addEventsIntoCalendar(
        context: Context,
        calendarId: Long,
        courseId: String,
        courseName: String,
        courseDateBlock: CourseDateBlock,
        isDeeplinkEnabled: Boolean
    ) {
        val date = courseDateBlock.getDateCalendar()
        // start time of the event added to the calendar
        val startMillis: Long = Calendar.getInstance().run {
            set(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY) - 1,
                date.get(Calendar.MINUTE)
            )
            timeInMillis
        }
        // end time of the event added to the calendar
        val endMillis: Long = Calendar.getInstance().run {
            set(
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH),
                date.get(Calendar.DAY_OF_MONTH),
                date.get(Calendar.HOUR_OF_DAY),
                date.get(Calendar.MINUTE)
            )
            timeInMillis
        }

        val values = ContentValues().apply {
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DTEND, endMillis)
            put(CalendarContract.Events.TITLE, "${AppConstants.ASSIGNMENT_DUE} : $courseName")
            put(
                CalendarContract.Events.DESCRIPTION,
                getEventDescription(
                    context = context,
                    courseId = courseId,
                    courseDateBlock = courseDateBlock,
                    isDeeplinkEnabled = isDeeplinkEnabled
                )
            )
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        addReminderToEvent(context = context, uri = uri)
    }

    /**
     * Method to generate & add deeplink into event description
     *
     * @return event description with deeplink for assignment block else block title
     */
    private fun getEventDescription(
        context: Context, courseId: String, courseDateBlock: CourseDateBlock, isDeeplinkEnabled: Boolean
    ): String {
        var eventDescription = courseDateBlock.title
        if (isDeeplinkEnabled && !TextUtils.isEmpty(courseDateBlock.blockId)) {
            val metaData = ContentMetadata()
                .addCustomMetadata(DeepLink.Keys.SCREEN_NAME, Screen.COURSE_COMPONENT)
                .addCustomMetadata(DeepLink.Keys.COURSE_ID, courseId)
                .addCustomMetadata(DeepLink.Keys.COMPONENT_ID, courseDateBlock.blockId)

            val branchUniversalObject = BranchUniversalObject()
                .setCanonicalIdentifier("${Screen.COURSE_COMPONENT}\n${courseDateBlock.blockId}")
                .setTitle(courseDateBlock.title)
                .setContentDescription(courseDateBlock.title)
                .setContentMetadata(metaData)

            val linkProperties = LinkProperties()
                .addControlParameter("\$desktop_url", courseDateBlock.link)

            eventDescription += "\n" + branchUniversalObject.getShortUrl(context, linkProperties)
        }
        return eventDescription
    }

    /**
     * Method to add a reminder to the given calendar events
     *
     * @param context
     * @param uri Calendar event Uri
     */
    private fun addReminderToEvent(context: Context, uri: Uri) {
        val eventId: Long = uri.lastPathSegment.toLong()
        logger.debug("Event ID $eventId")
        // Adding reminder on the start of event
        val eventValues = ContentValues().apply {
            put(CalendarContract.Reminders.MINUTES, 0)
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, eventValues)
        // Adding reminder 24 hours before the event get started
        eventValues.apply {
            put(CalendarContract.Reminders.MINUTES, REMINDER_24_HOURS)
        }
        context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, eventValues)
        // Adding reminder 48 hours before the event get started
        eventValues.apply {
            put(CalendarContract.Reminders.MINUTES, REMINDER_48_HOURS)
        }
        context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, eventValues)
    }

    /**
     * Method to query the events for the given calendar id
     *
     * @param context [Context]
     * @param calendarId calendarId to query the events
     *
     * @return [Cursor]
     *
     * */
    private fun getCalendarEvents(context: Context, calendarId: Long): Cursor? {
        val calendarContentResolver = context.contentResolver
        val projection = arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DESCRIPTION
        )
        val selection = CalendarContract.Events.CALENDAR_ID + "=?"
        return calendarContentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                arrayOf(calendarId.toString()),
                null
        )
    }

    /**
     * Method to compare the calendar events with course dates
     * @return  true if the events are the same as calendar dates otherwise false
     */
    private fun compareEvents(
        context: Context,
        calendarId: Long,
        courseDateBlocks: List<CourseDateBlock>
    ): Boolean {
        val cursor = getCalendarEvents(context, calendarId)
        // Creating a local copy of courseDateBlock as this method required nested iteration to compare events
        // To decrease the loop complexity we can remove object from list if they matched with existed events.
        // through this the loop complexity can be reduced to 1/2 of N^2 which is O(N^2).
        // Ref: https://dzone.com/articles/learning-big-o-notation-with-on-complexity
        val datesList = ArrayList(courseDateBlocks)
        var count = 0
        cursor?.run {
            if (moveToFirst()) {
                do {
                    val startDate = Calendar.getInstance().apply {
                        timeInMillis = getLong(getColumnIndex(CalendarContract.Events.DTSTART))
                    }
                    val description = getString(getColumnIndex(CalendarContract.Events.DESCRIPTION))
                    if (description != null) {
                        run breaker@{
                            datesList.forEachIndexed { index, unit ->
                                if (description.contains(unit.title, ignoreCase = true)) {
                                    val date = unit.getDateCalendar()
                                    // As event started 1 hour before it's due time so mincing 1 hour from current date
                                    date.add(Calendar.HOUR_OF_DAY, -1)

                                    // Comparing the existed events start time with current dates block
                                    if (date.get(Calendar.YEAR) == startDate.get(Calendar.YEAR) &&
                                        date.get(Calendar.MONTH) == startDate.get(Calendar.MONTH) &&
                                        date.get(Calendar.DAY_OF_MONTH) == startDate.get(Calendar.DAY_OF_MONTH) &&
                                        date.get(Calendar.HOUR_OF_DAY) == startDate.get(Calendar.HOUR_OF_DAY) &&
                                        date.get(Calendar.MINUTE) == startDate.get(Calendar.MINUTE)
                                    ) {
                                        count++
                                        datesList.removeAt(index)
                                        return@breaker
                                    }
                                }
                            }
                        }
                    }
                } while (moveToNext())
            }
        }
        val areEventsEqual = cursor?.count != 0 && cursor?.count == count
        cursor?.close()
        return areEventsEqual
    }

    /**
     * Method to delete the course calendar from the mobile calendar app
     */
    fun deleteCalendar(context: Context, calendarId: Long) {
        context.contentResolver.delete(Uri.parse("content://com.android.calendar/calendars/$calendarId"), null, null)
    }

    /**
     * Helper method used to return a URI for use with a sync adapter (how an application and a
     * sync adapter access the Calendar Provider)
     *
     * @param uri URI to access the calendar
     * @param account Name of the calendar owner
     *
     * @return URI of the calendar
     *
     */
    private fun asSyncAdapter(uri: Uri, account: String, accountType: String): Uri? {
        return uri.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.SyncState.ACCOUNT_NAME, account)
            .appendQueryParameter(CalendarContract.SyncState.ACCOUNT_TYPE, accountType).build()
    }

    fun openCalendarApp(fragment: Fragment) {
        val builder: Uri.Builder = CalendarContract.CONTENT_URI.buildUpon()
                .appendPath("time")
        ContentUris.appendId(builder, Calendar.getInstance().timeInMillis)
        val intent = Intent(Intent.ACTION_VIEW)
                .setData(builder.build())
        fragment.startActivity(intent)
    }

    /**
     * Helper method used to check that the calendar if outdated for the course or not
     *
     * @param context - current [Context] of the application
     * @param accountName Name of the calendar owner
     * @param calendarTitle Title for the course Calendar
     * @param courseDateBlocks Course dates events
     *
     * @return Calendar Id if Calendar is outdated otherwise -1
     *
     */
    @JvmStatic
    fun isCalendarOutOfDate(
        context: Context,
        accountName: String,
        calendarTitle: String,
        courseDateBlocks: List<CourseDateBlock>
    ): Long {
        if (isCalendarExists(context, accountName, calendarTitle)) {
            val calendarId = getCalendarId(context, accountName, calendarTitle)
            if (compareEvents(context, calendarId, courseDateBlocks).not()) {
                return calendarId
            }
        }
        return -1
    }

    /**
     * Method to get the current user account as the Calendar owner
     *
     * @param environment Relevant configuration environment.
     *
     * @return calendar owner account or "local_user"
     */
    @JvmStatic
    fun getUserAccountForSync(environment: IEdxEnvironment): String {
        return environment.loginPrefs.currentUserProfile?.email ?: LOCAL_USER
    }

    /**
     * Method to create the Calendar title for the platform against the course
     *
     * @param environment Relevant configuration environment.
     * @param courseName Name of the course for that creating the Calendar events.
     *
     * @return title of the Calendar against the course
     */
    @JvmStatic
    fun getCourseCalendarTitle(
        environment: IEdxEnvironment,
        courseName: String
    ): String {
        return "${environment.config.platformName} - $courseName"
    }
}
