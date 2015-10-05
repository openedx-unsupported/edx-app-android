package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.widget.TextViewCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.GetAccountTask;
import org.edx.mobile.util.ResourceUtil;

import java.util.HashMap;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;

public class EditUserProfileFragment extends RoboFragment {

    @InjectExtra(UserProfileActivity.EXTRA_USERNAME)
    private String username;

    @Nullable
    private GetAccountTask getAccountTask;

    @Nullable
    private Account account;

    @Nullable
    private ViewHolder viewHolder;

    @Inject
    private Router router;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        getAccountTask = new GetAccountTask(getActivity(), username) {
            @Override
            protected void onSuccess(Account account) throws Exception {
                EditUserProfileFragment.this.account = account;
                if (null != viewHolder) {
                    viewHolder.setAccount(account);
                }
            }
        };
        getAccountTask.setTaskProcessCallback(null); // Disable default loading indicator, we have our own
        getAccountTask.execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_user_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewHolder = new ViewHolder(view);
        viewHolder.setUsername(username);
        viewHolder.setAccount(account);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != getAccountTask) {
            getAccountTask.cancel(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder = null;
    }

    public static class ViewHolder {
        public final View content;
        public final View loadingIndicator;
        public final ImageView profileImage;
        public final TextView username;
        public final RadioGroup privacyOptions;
        public final ViewGroup fields;

        public ViewHolder(@NonNull View parent) {
            this.content = parent.findViewById(R.id.content);
            this.loadingIndicator = parent.findViewById(R.id.loading_indicator);
            this.profileImage = (ImageView) parent.findViewById(R.id.profile_image);
            this.username = (TextView) parent.findViewById(R.id.username);
            this.privacyOptions = (RadioGroup) parent.findViewById(R.id.privacy_options);
            this.fields = (ViewGroup) parent.findViewById(R.id.fields);
            this.privacyOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    fields.setBackgroundColor(fields.getResources().getColor(checkedId == R.id.privacy_option_full ? R.color.white : R.color.edx_grayscale_neutral_x_light));
                }
            });
        }

        public void setUsername(String username) {
            this.username.setText(username);
        }

        public void setAccount(@Nullable final Account account) {
            if (null == account) {
                content.setVisibility(View.GONE);
                loadingIndicator.setVisibility(View.VISIBLE);

            } else {
                content.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);

                Glide.with(profileImage.getContext())
                        .load(account.getProfileImage().getImageUrlMedium())
                        .into(profileImage);

                if (account.getAccountPrivacy() == Account.Privacy.PRIVATE) {
                    privacyOptions.check(R.id.privacy_option_limited);
                } else {
                    privacyOptions.check(R.id.privacy_option_full);
                }

                final LayoutInflater layoutInflater = LayoutInflater.from(fields.getContext());
                createFieldViewHolder(layoutInflater, fields, R.string.edit_user_profile_birth_year_label, null == account.getYearOfBirth() ? fields.getResources().getString(R.string.edit_user_profile_field_placeholder) : account.getYearOfBirth().toString());
                createFieldViewHolder(layoutInflater, fields, R.string.edit_user_profile_country_label, TextUtils.isEmpty(account.getCountry()) ? fields.getResources().getString(R.string.edit_user_profile_field_placeholder) : account.getCountry());
                createFieldViewHolder(layoutInflater, fields, R.string.edit_user_profile_language_label, account.getLanguageProficiencies().isEmpty() ? fields.getResources().getString(R.string.edit_user_profile_field_placeholder) : account.getLanguageProficiencies().get(0).getCode());
                createFieldViewHolder(layoutInflater, fields, R.string.edit_user_profile_bio_label, TextUtils.isEmpty(account.getBio()) ? fields.getResources().getString(R.string.edit_user_profile_bio_placeholder) : account.getBio());
            }
        }
    }

    private static TextView createFieldViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @StringRes int labelRes, @NonNull final String value) {
        final TextView textView = (TextView) inflater.inflate(R.layout.edit_user_profile_field, parent, false);
        final SpannableString formattedLabel = new SpannableString(parent.getResources().getString(labelRes));
        formattedLabel.setSpan(new ForegroundColorSpan(parent.getResources().getColor(R.color.edx_grayscale_neutral_x_dark)), 0, formattedLabel.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        final SpannableString formattedValue = new SpannableString(value);
        formattedValue.setSpan(new ForegroundColorSpan(parent.getResources().getColor(R.color.edx_grayscale_neutral_dark)), 0, formattedValue.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ResourceUtil.getFormattedString(parent.getResources(), R.string.edit_user_profile_field, new HashMap<String, CharSequence>() {{
            put("label", formattedLabel);
            put("value", formattedValue);
        }}));
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView, null, null, new IconDrawable(parent.getContext(), Iconify.IconValue.fa_angle_right).colorRes(R.color.edx_grayscale_neutral_light).sizeDp(24), null);
        parent.addView(textView);
        return textView;
    }
}