package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.social.SocialGroup;
import org.edx.mobile.view.custom.ETextView;

/**
 * Created by marcashman on 2014-12-17.
 */
public class GroupSummaryActivity extends BaseSingleFragmentActivity {

    private static final String TAG = GroupSummaryFragment.class.getCanonicalName();
    public static final String EXTRA_GROUP = TAG + ".group";
    private SocialGroup group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        group = getIntent().getParcelableExtra(EXTRA_GROUP);
        if (group == null) {
            throw new IllegalArgumentException("missing group");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        setTitle(group.getName());
    }

    @Override
    public Fragment getFirstFragment() {
        Bundle args = new Bundle();
        args.putParcelable(GroupSummaryFragment.ARG_GROUP, getIntent().getParcelableExtra(EXTRA_GROUP));

        Fragment fragment = new GroupSummaryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        boolean create = super.onCreateOptionsMenu(menu);

        MenuItem unreadMenuItem = menu.findItem(R.id.unread_display);

        SocialGroup group = getIntent().getParcelableExtra(EXTRA_GROUP);

        if (group.getUnread() > 0) {

            unreadMenuItem.setVisible(true);
            View unreadTextView = unreadMenuItem.getActionView();
            ETextView unreadTV = (ETextView) unreadTextView.findViewById(R.id.unread_tv);
            unreadTV.setText(getString(R.string.unread_text, group.getUnread()));

        }

        return create;

    }
}
