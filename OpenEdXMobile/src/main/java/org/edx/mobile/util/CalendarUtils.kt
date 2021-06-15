package org.edx.mobile.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.edx.mobile.R
import org.edx.mobile.logger.Logger
import org.edx.mobile.model.course.CourseDateBlock
import java.util.*

class CalendarUtils {

    companion object {

        private val logger = Logger(DateUtil::class.java.name)

        /**
         * Method to create a separate calendar based on course name in mobile calendar app
         */
        @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        fun createCalendar(context: Context, accountName: String, courseName: String): Long {
            val contentValues = ContentValues()
            contentValues.put(CalendarContract.Calendars.NAME, courseName)
            contentValues.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, courseName)
            contentValues.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            contentValues.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            contentValues.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_ROOT)
            contentValues.put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            contentValues.put(CalendarContract.Calendars.VISIBLE, 1)
            contentValues.put(CalendarContract.Calendars.CALENDAR_COLOR, ContextCompat.getColor(context, R.color.primaryBaseColor))
            val creationUri: Uri? = asSyncAdapter(Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString()), accountName)
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
        fun getCalendarId(context: Context, accountName: String, courseName: String): Long {
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
                            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + "=?)", arrayOf(accountName, courseName,
                    courseName), null)
            if (cursor.moveToFirst()) {
                if (cursor.getString(2).equals(courseName))
                    calendarId = cursor.getInt(0)
            }
            return calendarId.toLong()
        }

        /**
         * Method to add important dates of course as calendar event into calendar of mobile app
         */
        fun addEventsIntoCalendar(context: Context, calendarId: Long, courseName: String, courseDateBlock: CourseDateBlock) {
            val date = courseDateBlock.getDateCalendar()
            // start time of the event added to the calendar
            val startMillis: Long = Calendar.getInstance().run {
                set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.HOUR_OF_DAY) - 1, date.get(Calendar.MINUTE))
                timeInMillis
            }
            // end time of the event added to the calendar
            val endMillis: Long = Calendar.getInstance().run {
                set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE))
                timeInMillis
            }

            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.TITLE, "${AppConstants.ASSIGNMENT_DUE} : $courseName")
                put(CalendarContract.Events.DESCRIPTION, courseDateBlock.title)
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
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
        private fun asSyncAdapter(uri: Uri, account: String): Uri? {
            return uri.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                    .appendQueryParameter(CalendarContract.SyncState.ACCOUNT_NAME, account)
                    .appendQueryParameter(CalendarContract.SyncState.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build()
        }

        fun openCalendarApp(fragment: Fragment) {
            val builder: Uri.Builder = CalendarContract.CONTENT_URI.buildUpon()
                    .appendPath("time")
            ContentUris.appendId(builder, Calendar.getInstance().timeInMillis)
            val intent = Intent(Intent.ACTION_VIEW)
                    .setData(builder.build())
            fragment.startActivity(intent)
        }
    }
}