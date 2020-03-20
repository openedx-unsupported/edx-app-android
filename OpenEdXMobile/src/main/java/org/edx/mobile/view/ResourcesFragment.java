package org.edx.mobile.view;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.widget.IconImageView;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.model.api.EnrolledCoursesResponse;

import de.greenrobot.event.EventBus;

import static org.edx.mobile.deeplink.Screen.COURSE_ANNOUNCEMENT;
import static org.edx.mobile.deeplink.Screen.COURSE_HANDOUT;

public class ResourcesFragment extends OfflineSupportBaseFragment {
    @Inject
    private IEdxEnvironment environment;

    private EnrolledCoursesResponse courseData;

    public static Bundle makeArguments(@NonNull EnrolledCoursesResponse model, @Nullable @ScreenDef String screenName) {
        final Bundle arguments = new Bundle();
        arguments.putSerializable(Router.EXTRA_COURSE_DATA, model);
        arguments.putSerializable(Router.EXTRA_SCREEN_NAME, screenName);
        return arguments;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().registerSticky(ResourcesFragment.this);
        courseData = (EnrolledCoursesResponse) getArguments().getSerializable(Router.EXTRA_COURSE_DATA);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_resources, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final LinearLayout parent = (LinearLayout) view.findViewById(R.id.root);
        ViewHolder holder;

        holder = createViewHolder(inflater, parent);

        holder.typeView.setIcon(FontAwesomeIcons.fa_file_text_o);
        holder.titleView.setText(R.string.handouts_title);
        holder.subtitleView.setText(R.string.handouts_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (courseData != null) {
                    environment.getRouter().showHandouts(getActivity(), courseData);
                }
            }
        });

        holder = createViewHolder(inflater, parent);

        holder.typeView.setIcon(FontAwesomeIcons.fa_bullhorn);
        holder.titleView.setText(R.string.announcement_title);
        holder.subtitleView.setText(R.string.announcement_subtitle);
        holder.rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (courseData != null) {
                    environment.getRouter().showCourseAnnouncement(getActivity(), courseData);
                }
            }
        });

        final Bundle arguments = getArguments();
        @ScreenDef String screenName;
        if (arguments != null) {
            screenName = arguments.getString(Router.EXTRA_SCREEN_NAME);
            if (!TextUtils.isEmpty(screenName)) {
                switch (screenName) {
                    case COURSE_HANDOUT:
                        environment.getRouter().showHandouts(getActivity(), courseData);
                        break;
                    case COURSE_ANNOUNCEMENT:
                        environment.getRouter().showCourseAnnouncement(getActivity(), courseData);
                        break;
                }
                // Setting this to null, so that upon recreation of the fragment, relevant activity
                // shouldn't be auto created again.
                arguments.putString(Router.EXTRA_SCREEN_NAME, null);
            }
        }
    }

    private ViewHolder createViewHolder(LayoutInflater inflater, LinearLayout parent) {
        ViewHolder holder = new ViewHolder();
        holder.rowView = inflater.inflate(R.layout.row_resource_list, parent, false);
        holder.typeView = (IconImageView) holder.rowView.findViewById(R.id.row_type);
        holder.titleView = (TextView) holder.rowView.findViewById(R.id.row_title);
        holder.subtitleView = (TextView) holder.rowView.findViewById(R.id.row_subtitle);
        parent.addView(holder.rowView);
        return holder;
    }

    private class ViewHolder {
        View rowView;
        IconImageView typeView;
        TextView titleView;
        TextView subtitleView;
    }

    @SuppressWarnings("unused")
    public void onEvent(NetworkConnectivityChangeEvent event) {
        onNetworkConnectivityChangeEvent(event);
    }

    @Override
    protected boolean isShowingFullScreenError() {
        return false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }
}
