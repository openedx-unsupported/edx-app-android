package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.third_party.iconify.IconDrawable;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.user.Account;
import org.edx.mobile.user.DataType;
import org.edx.mobile.user.FormDescription;
import org.edx.mobile.user.FormField;
import org.edx.mobile.user.GetAccountTask;
import org.edx.mobile.user.GetProfileFormDescriptionTask;
import org.edx.mobile.user.LanguageProficiency;
import org.edx.mobile.user.SetAccountImageTask;
import org.edx.mobile.user.UpdateAccountTask;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.images.LocalImageChooserHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;

public class EditUserProfileFragment extends RoboFragment {

    private static final int EDIT_FIELD_REQUEST = 1;
    private static final int CHOOSE_PHOTO_REQUEST = 2;
    private static final int CROP_PHOTO_REQUEST = 3;

    @InjectExtra(EditUserProfileActivity.EXTRA_USERNAME)
    private String username;

    private GetAccountTask getAccountTask;

    private GetProfileFormDescriptionTask getProfileFormDescriptionTask;

    private SetAccountImageTask setAccountImageTask;

    @Nullable
    private Account account;

    @Nullable
    private FormDescription formDescription;

    @Nullable
    private ViewHolder viewHolder;

    @Inject
    private Router router;

    @NonNull
    private final LocalImageChooserHelper helper = new LocalImageChooserHelper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);

        getAccountTask = new GetAccountTask(getActivity(), username) {
            @Override
            protected void onSuccess(Account account) throws Exception {
                EditUserProfileFragment.this.account = account;
                if (null != viewHolder) {
                    setData(account, formDescription);
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
                    setData(account, formDescription);
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
        viewHolder.profileImageProgress.setVisibility(View.GONE);
        viewHolder.username.setText(username);
        viewHolder.changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(helper.createChooserIntent(getActivity()), CHOOSE_PHOTO_REQUEST);
            }
        });
        setData(account, formDescription);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getAccountTask.cancel(true);
        getProfileFormDescriptionTask.cancel(true);
        if (null != setAccountImageTask) {
            setAccountImageTask.cancel(true);
        }
        helper.onDestroy();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder = null;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ProfilePhotoUpdatedEvent event) {
        Glide.with(this)
                .load(event.getUri())
                .skipMemoryCache(true) // URI is re-used in subsequent events; disable caching
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(viewHolder.profileImage);

    }

    public class ViewHolder {
        public final View content;
        public final View loadingIndicator;
        public final ImageView profileImage;
        public final TextView username;
        public final ViewGroup fields;
        public final View changePhoto;
        public final View profileImageProgress;

        public ViewHolder(@NonNull View parent) {
            this.content = parent.findViewById(R.id.content);
            this.loadingIndicator = parent.findViewById(R.id.loading_indicator);
            this.profileImage = (ImageView) parent.findViewById(R.id.profile_image);
            this.username = (TextView) parent.findViewById(R.id.username);
            this.fields = (ViewGroup) parent.findViewById(R.id.fields);
            this.changePhoto = parent.findViewById(R.id.change_photo);
            this.profileImageProgress = parent.findViewById(R.id.profile_image_progress);
        }
    }

    public void setData(@Nullable final Account account, @Nullable FormDescription formDescription) {
        if (null == viewHolder) {
            return;
        }
        if (null == account || null == formDescription) {
            viewHolder.content.setVisibility(View.GONE);
            viewHolder.loadingIndicator.setVisibility(View.VISIBLE);
            viewHolder.changePhoto.setVisibility(View.GONE);

        } else {
            viewHolder.content.setVisibility(View.VISIBLE);
            viewHolder.loadingIndicator.setVisibility(View.GONE);
            viewHolder.changePhoto.setVisibility(account.requiresParentalConsent() ? View.GONE : View.VISIBLE);

            Glide.with(viewHolder.profileImage.getContext())
                    .load(account.getProfileImage().getImageUrlLarge())
                    .into(viewHolder.profileImage);

            final Gson gson = new GsonBuilder().serializeNulls().create();
            final JsonObject obj = (JsonObject) gson.toJsonTree(account);

            final boolean isLimited = account.getAccountPrivacy() != Account.Privacy.ALL_USERS || account.requiresParentalConsent();

            final LayoutInflater layoutInflater = LayoutInflater.from(viewHolder.fields.getContext());
            viewHolder.fields.removeAllViews();
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
                        if (isAccountPrivacyField && null == value || account.requiresParentalConsent()) {
                            value = Account.PRIVATE_SERIALIZED_NAME;
                        }

                        createSwitch(layoutInflater, viewHolder.fields, field, value,
                                account.requiresParentalConsent() ? getString(R.string.profile_consent_needed_explanation) : field.getInstructions(),
                                isAccountPrivacyField ? account.requiresParentalConsent() : isLimited,
                                new SwitchListener() {
                                    @Override
                                    public void onSwitch(@NonNull String value) {
                                        executeUpdate(field, value);
                                    }
                                });
                        break;
                    }
                    case SELECT:
                    case TEXTAREA: {
                        final String value;
                        final String text;
                        {
                            final JsonElement accountField = obj.get(field.getName());
                            if (null == accountField) {
                                value = null;
                                text = null;
                            } else if (null == field.getDataType()) {
                                // No data type is specified, treat as generic string
                                value = gson.fromJson(accountField, String.class);
                                text = value;
                            } else {
                                switch (field.getDataType()) {
                                    case COUNTRY:
                                        value = gson.fromJson(accountField, String.class);
                                        text = TextUtils.isEmpty(value) ? null : new Locale.Builder().setRegion(value).build().getDisplayCountry();
                                        break;
                                    case LANGUAGE:
                                        final List<LanguageProficiency> languageProficiencies = gson.fromJson(accountField, new TypeToken<List<LanguageProficiency>>() {
                                        }.getType());
                                        value = languageProficiencies.isEmpty() ? null : languageProficiencies.get(0).getCode();
                                        text = value == null ? null : new Locale.Builder().setLanguage(value).build().getDisplayName();
                                        break;
                                    default:
                                        // Unknown data type; ignore this field
                                        continue;
                                }
                            }
                        }
                        final String displayValue;
                        if (TextUtils.isEmpty(text)) {
                            final String placeholder = field.getPlaceholder();
                            if (TextUtils.isEmpty(placeholder)) {
                                displayValue = viewHolder.fields.getResources().getString(R.string.edit_user_profile_field_placeholder);
                            } else {
                                displayValue = placeholder;
                            }
                        } else {
                            displayValue = text;
                        }
                        createField(layoutInflater, viewHolder.fields, field, displayValue, isLimited && !field.getName().equals(Account.YEAR_OF_BIRTH_SERIALIZED_NAME), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivityForResult(FormFieldActivity.newIntent(getActivity(), field, value), EDIT_FIELD_REQUEST);
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
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO_REQUEST: {
                final Uri imageUri = helper.onActivityResult(resultCode, data);
                if (null != imageUri) {
                    startActivityForResult(CropImageActivity.newIntent(getActivity(), imageUri), CROP_PHOTO_REQUEST);
                }
                break;
            }
            case CROP_PHOTO_REQUEST: {
                final Uri imageUri = CropImageActivity.getImageUriFromResult(data);
                final Rect cropRect = CropImageActivity.getCropRectFromResult(data);
                if (null != imageUri && null != cropRect) {
                    viewHolder.profileImageProgress.setVisibility(View.VISIBLE);
                    if (viewHolder.profileImageProgress.getAnimation() == null) {
                        viewHolder.profileImageProgress.startAnimation(
                                AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));
                    }
                    // TODO: Test this with "Don't keep activities"
                    if (null != setAccountImageTask) {
                        setAccountImageTask.cancel(true);
                    }
                    setAccountImageTask = new SetAccountImageTask(getActivity(), username, imageUri, cropRect) {
                        @Override
                        protected void onSuccess(Void aVoid) throws Exception {
                            hideLoading();
                        }

                        @Override
                        protected void onException(Exception e) throws RuntimeException {
                            super.onException(e);
                            showErrorMessage(e);
                            hideLoading();
                        }

                        private void hideLoading() {
                            if (null != viewHolder) {
                                viewHolder.profileImageProgress.clearAnimation();
                                viewHolder.profileImageProgress.setVisibility(View.GONE);
                            }
                        }
                    };
                    setAccountImageTask.setProgressCallback(null); // Hide default loading indicator
                    setAccountImageTask.execute();
                }
                break;
            }
            case EDIT_FIELD_REQUEST: {
                if (resultCode == Activity.RESULT_OK) {
                    final FormField fieldName = (FormField) data.getSerializableExtra(FormFieldActivity.EXTRA_FIELD);
                    final String fieldValue = data.getStringExtra(FormFieldActivity.EXTRA_VALUE);
                    executeUpdate(fieldName, fieldValue);
                }
                break;
            }
            default: {
                super.onActivityResult(requestCode, resultCode, data);
                break;
            }
        }
    }

    private void executeUpdate(FormField field, String fieldValue) {
        final Object valueObject;
        if (field.getDataType() == DataType.LANGUAGE) {
            valueObject = Collections.singletonList(new LanguageProficiency(fieldValue));
        } else {
            valueObject = fieldValue;
        }
        new UpdateAccountTask(getActivity(), username, field.getName(), valueObject) {
            @Override
            protected void onSuccess(Account account) throws Exception {
                EditUserProfileFragment.this.account = account;
                setData(account, formDescription);
            }
        }.execute();
    }

    public interface SwitchListener {
        void onSwitch(@NonNull String value);
    }

    private static View createSwitch(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull FormField field, @NonNull String value, @NonNull String instructions, boolean readOnly, @NonNull final SwitchListener switchListener) {
        final View view = inflater.inflate(R.layout.edit_user_profile_switch, parent, false);
        ((TextView) view.findViewById(R.id.label)).setText(field.getLabel());
        ((TextView) view.findViewById(R.id.instructions)).setText(instructions);
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
            child.setEnabled(!readOnly);
            if (child.getTag().equals(value)) {
                group.check(child.getId());
                break;
            }
        }
        if (readOnly) {
            group.setEnabled(false);
            view.setBackgroundColor(view.getResources().getColor(R.color.edx_grayscale_neutral_x_light));
        } else {
            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switchListener.onSwitch((String) group.findViewById(checkedId).getTag());
                }
            });
        }
        parent.addView(view);
        return view;
    }

    private static TextView createField(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull FormField field, @NonNull final String value, boolean readOnly, @NonNull View.OnClickListener onClickListener) {
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
        if (readOnly) {
            textView.setEnabled(false);
            textView.setBackgroundColor(textView.getResources().getColor(R.color.edx_grayscale_neutral_x_light));
        } else {
            textView.setOnClickListener(onClickListener);
        }
        parent.addView(textView);
        return textView;
    }
}