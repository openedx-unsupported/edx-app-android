package org.humana.mobile.tta.ui.mxCalenderView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import org.humana.mobile.R;
import org.humana.mobile.tta.data.local.db.table.CalendarEvents;
import org.humana.mobile.tta.ui.programs.units.view_model.UnitCalendarViewModel;
import org.humana.mobile.util.DateUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomCalendarView extends LinearLayout {
    ImageButton nextButton, previousButton;
    static TextView currentDate;
    static GridView calGrid;
    private static final int MAX_CALENDAR_DAYS = 42;
    static Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    static Context context;
    public static UnitCalendarViewModel.CustomCalendarAdapter adapter;


    static List<Date> dates = new ArrayList<>();
    static List<Events> eventsList = new ArrayList<>();

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM, YYYY", Locale.ENGLISH);
    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
    SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY", Locale.ENGLISH);


    public CustomCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initializeView();
        setUpCalendar();

        previousButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, -1);
                setUpCalendar();
            }
        });
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.MONTH, 1);
                setUpCalendar();
            }
        });

    }



    private void initializeView() {
        View view = LayoutInflater.from(context).inflate(R.layout.mx_custom_calender_view, this, true);

        nextButton = view.findViewById(R.id.ib_next);
        previousButton = view.findViewById(R.id.ib_prev);
        currentDate = view.findViewById(R.id.tv_date);
        calGrid = view.findViewById(R.id.cal_grid);
    }


    public void setUpCalendar() {
        String date = simpleDateFormat.format(calendar.getTime());
        currentDate.setText(date);
        dates.clear();

        Calendar monthCalendar = (Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1);

        int FIRST_DAY_OF_MONTH = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1;

        monthCalendar.add(Calendar.DAY_OF_MONTH, -FIRST_DAY_OF_MONTH);

        while (dates.size() < MAX_CALENDAR_DAYS) {
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1);

        }
        setupAdapter();
    }

    public static void createEvents(List<Events> events) {
        Events e;
        eventsList.clear();
        for (int i = 0; i < events.size(); i++) {
            e = new Events(events.get(i).getDATE(), events.get(i).getTitle());
            eventsList.add(e);
        }
        setupAdapter();
    }


    public static void setupAdapter() {
        adapter = new UnitCalendarViewModel.CustomCalendarAdapter(context, dates, calendar, eventsList);
        calGrid.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
