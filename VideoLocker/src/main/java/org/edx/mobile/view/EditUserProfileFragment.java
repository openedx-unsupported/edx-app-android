package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.FieldType;
import org.edx.mobile.user.FormDescription;
import org.edx.mobile.user.FormField;
import org.edx.mobile.user.GetAccountTask;
import org.edx.mobile.user.GetProfileFormDescriptionTask;
import org.edx.mobile.user.LanguageProficiency;
import org.edx.mobile.user.UpdateAccountTask;
import org.edx.mobile.util.ResourceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;

public class EditUserProfileFragment extends RoboFragment {

    private static final int EDIT_FIELD_REQUEST = 1;

    @InjectExtra(EditUserProfileActivity.EXTRA_USERNAME)
    private String username;

    private GetAccountTask getAccountTask;

    private GetProfileFormDescriptionTask getProfileFormDescriptionTask;

    @Nullable
    private Account account;

    @Nullable
    private FormDescription formDescription;

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
                    viewHolder.setData(account, formDescription);
                }
            }
        };
        getAccountTask.setTaskProcessCallback(null); // Disable default loading indicator, we have our own
        getAccountTask.execute();

        getProfileFormDescriptionTask = new GetProfileFormDescriptionTask(getActivity()) {
            @Override
            protected void onSuccess(@NonNull FormDescription formDescription) throws Exception {
                EditUserProfileFragment.this.formDescription = formDescription;
                if (null != viewHolder) {
                    viewHolder.setData(account, formDescription);
                }
            }
        };
        getProfileFormDescriptionTask.setTaskProcessCallback(null);
        getProfileFormDescriptionTask.execute();
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
        viewHolder.setData(account, formDescription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getAccountTask.cancel(true);
        getProfileFormDescriptionTask.cancel(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder = null;
    }

    public class ViewHolder {
        public final View content;
        public final View loadingIndicator;
        public final ImageView profileImage;
        public final TextView username;
        public final ViewGroup fields;

        public ViewHolder(@NonNull View parent) {
            this.content = parent.findViewById(R.id.content);
            this.loadingIndicator = parent.findViewById(R.id.loading_indicator);
            this.profileImage = (ImageView) parent.findViewById(R.id.profile_image);
            this.username = (TextView) parent.findViewById(R.id.username);
            this.fields = (ViewGroup) parent.findViewById(R.id.fields);
        }

        public void setUsername(String username) {
            this.username.setText(username);
        }

        public void setData(@Nullable final Account account, @Nullable FormDescription formDescription) {
            if (null == account || null == formDescription) {
                content.setVisibility(View.GONE);
                loadingIndicator.setVisibility(View.VISIBLE);

            } else {
                content.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);

                Glide.with(profileImage.getContext())
                        .load(account.getProfileImage().getImageUrlMedium())
                        .into(profileImage);

                final Gson gson = new GsonBuilder().serializeNulls().create();
                final JsonObject obj = (JsonObject) gson.toJsonTree(account);

                final LayoutInflater layoutInflater = LayoutInflater.from(fields.getContext());
                fields.removeAllViews();
                for (final FormField field : formDescription.getFields()) {
                    if (null == field.getFieldType()) {
                        // Missing field type; ignore this field
                        continue;
                    }
                    switch (field.getFieldType()) {
                        case SWITCH: {
                            if (field.getOptions().getValues().size() != 2) {
                                // We expect to have exactly two options; ignore this field.
                                continue;
                            }
                            final boolean isAccountPrivacyField = field.getName().equals(Account.ACCOUNT_PRIVACY_SERIALIZED_NAME);
                            String value = gson.fromJson(obj.get(field.getName()), String.class);
                            if (null == value && isAccountPrivacyField) {
                                value = Account.PRIVATE_SERIALIZED_NAME;
                            }

                            createSwitch(layoutInflater, fields, field, value, new SwitchListener() {
                                @Override
                                public void onSwitch(@NonNull String value) {
                                    executeUpdate(field.getName(), value);
                                }
                            });
                            break;
                        }
                        case SELECT:
                        case TEXTAREA: {
                            final String value;
                            {
                                final JsonElement accountField = obj.get(field.getName());
                                if (null == accountField) {
                                    value = null;
                                } else if (null == field.getDataType()) {
                                    // No data type is specified, treat as generic string
                                    value = gson.fromJson(accountField, String.class);
                                } else {
                                    switch (field.getDataType()) {
                                        case COUNTRY:
                                            final String countryCode = gson.fromJson(accountField, String.class);
                                            value = TextUtils.isEmpty(countryCode) ? null : new Locale.Builder().setRegion(countryCode).build().getDisplayCountry();
                                            break;
                                        case LANGUAGE:
                                            final List<LanguageProficiency> languageProficiencies = gson.fromJson(accountField, new TypeToken<List<LanguageProficiency>>() {
                                            }.getType());
                                            value = languageProficiencies.isEmpty() ? null : new Locale.Builder().setLanguage(languageProficiencies.get(0).getCode()).build().getDisplayName();
                                            break;
                                        default:
                                            // Unknown data type; ignore this field
                                            continue;
                                    }
                                }
                            }
                            final String displayValue;
                            if (TextUtils.isEmpty(value)) {
                                final String placeholder = field.getPlaceholder();
                                if (TextUtils.isEmpty(placeholder)) {
                                    displayValue = fields.getResources().getString(R.string.edit_user_profile_field_placeholder);
                                } else {
                                    displayValue = placeholder;
                                }
                            } else {
                                displayValue = value;
                            }
                            createField(layoutInflater, fields, field, displayValue).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // TODO: Move this switch into field activity?
                                    if (field.getFieldType() == FieldType.SELECT) {
                                        startActivityForResult(FormFieldSelectActivity.newIntent(getActivity(), field, value), EDIT_FIELD_REQUEST);
                                    } else {
                                        startActivityForResult(FormFieldTextAreaActivity.newIntent(getActivity(), field, value), EDIT_FIELD_REQUEST);
                                    }
                                }
                            });
                            break;
                        }
                        default: {
                            // Unknown field type; ignore this field
                            break;
                        }
                    }
                }

                final boolean isLimited = account.getAccountPrivacy() != Account.Privacy.ALL_USERS;
                fields.setBackgroundColor(fields.getResources().getColor(isLimited ? R.color.edx_grayscale_neutral_x_light : R.color.white));
                // TODO: make fields readable / read-only (except birth year)
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_FIELD_REQUEST && resultCode == Activity.RESULT_OK) {
            final String fieldName = data.getStringExtra(FormFieldSelectActivity.EXTRA_FIELD_NAME);
            final String fieldValue = data.getStringExtra(FormFieldSelectActivity.EXTRA_VALUE);
            executeUpdate(fieldName, fieldValue);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void executeUpdate(String fieldName, String fieldValue) {
        new UpdateAccountTask(getActivity(), username, fieldName, fieldValue) {
            @Override
            protected void onSuccess(Account account) throws Exception {
                EditUserProfileFragment.this.account = account;
                if (null != viewHolder) {
                    viewHolder.setData(account, formDescription);
                }
            }
        }.execute();
    }

    public interface SwitchListener {
        void onSwitch(@NonNull String value);
    }

    private static View createSwitch(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull FormField field, @NonNull String value, @NonNull final SwitchListener switchListener) {
        final View view = inflater.inflate(R.layout.edit_user_profile_switch, parent, false);
        ((TextView) view.findViewById(R.id.label)).setText(field.getLabel());
        ((TextView) view.findViewById(R.id.instructions)).setText(field.getInstructions());
        final RadioGroup group = ((RadioGroup) view.findViewById(R.id.options));
        {
            final RadioButton optionOne = ((RadioButton) view.findViewById(R.id.option_one));
            final RadioButton optionTwo = ((RadioButton) view.findViewById(R.id.option_two));
            optionOne.setText(field.getOptions().getValues().get(0).getName());
            optionOne.setTag(field.getOptions().getValues().get(0).getValue());
            optionTwo.setText(field.getOptions().getValues().get(1).getName());
            optionTwo.setTag(field.getOptions().getValues().get(1).getValue());
        }
        for (int i = 0; i < group.getChildCount(); i++) {
            final View child = group.getChildAt(i);
            if (child.getTag().equals(value)) {
                group.check(child.getId());
                break;
            }
        }
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switchListener.onSwitch((String) group.findViewById(checkedId).getTag());
            }
        });
        parent.addView(view);
        return view;
    }

    private static TextView createField(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull FormField field, @NonNull final String value) {
        final TextView textView = (TextView) inflater.inflate(R.layout.edit_user_profile_field, parent, false);
        final SpannableString formattedLabel = new SpannableString(field.getLabel());
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