package org.edx.mobile.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.internal.Animation;
import com.joanzapata.iconify.widget.IconImageView;

import de.greenrobot.event.EventBus;
import de.hdodenhof.circleimageview.CircleImageView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.notifications.DialogErrorNotification;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.task.Task;
import org.edx.mobile.user.*;
import org.edx.mobile.user.UserAPI.AccountDataUpdatedCallback;
import org.edx.mobile.util.InvalidLocaleException;
import org.edx.mobile.util.LocaleUtils;
import org.edx.mobile.util.PermissionsUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UserProfileUtils;
import org.edx.mobile.util.images.ImageCaptureHelper;
import org.edx.mobile.util.images.ImageUtils;
import org.edx.mobile.view.common.TaskMessageCallback;

import retrofit2.Call;
import roboguice.inject.InjectExtra;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class EditUserProfileFragment extends BaseFragment implements BaseFragment.PermissionListener {

    private static final int EDIT_FIELD_REQUEST = 1;
    private static final int CAPTURE_PHOTO_REQUEST = 2;
    private static final int CHOOSE_PHOTO_REQUEST = 3;
    private static final int CROP_PHOTO_REQUEST = 4;

    @InjectExtra(EditUserProfileActivity.EXTRA_USERNAME)
    private String username;

    private Call<Account> getAccountCall;

    private GetProfileFormDescriptionTask getProfileFormDescriptionTask;

    private Task setAccountImageTask;

    @Nullable
    private Account account;

    @Nullable
    private FormDescription formDescription;

    @Nullable
    private ViewHolder viewHolder;

    @Inject
    private UserService userService;

    @Inject
    private Router router;

    @Inject
    private AnalyticsRegistry analyticsRegistry;

    @NonNull
    private final ImageCaptureHelper helper = new ImageCaptureHelper();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);


        final Activity activity = getActivity();
        final TaskMessageCallback mCallback = activity instanceof TaskMessageCallback ? (TaskMessageCallback) activity : null;
        getAccountCall = userService.getAccount(username);
        getAccountCall.enqueue(new AccountDataUpdatedCallback(
                activity, username,
                null, // Disable default loading indicator, we have our own
                mCallback, CallTrigger.LOADING_CACHED));

        getProfileFormDescriptionTask = new GetProfileFormDescriptionTask(activity) {
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
        permissionListener = this;
        viewHolder = new ViewHolder(view);
        viewHolder.profileImageProgress.setVisibility(View.GONE);
        viewHolder.username.setText(username);
        viewHolder.username.setContentDescription(ResourceUtil.getFormattedString(getResources(), R.string.profile_username_description, "username", username));

        final IconDrawable icon = new IconDrawable(getActivity(), FontAwesomeIcons.fa_camera)
                .colorRes(getActivity(), R.color.disableable_button_text)
                .sizeRes(getActivity(), R.dimen.fa_x_small)
                .tint(null); // IconDrawable is tinted by default, but we don't want it to be tinted here
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(viewHolder.changePhoto, icon, null, null, null);
        viewHolder.changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.getMenuInflater().inflate(R.menu.change_photo, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.take_photo: {
                                askForPermission(new String[]{Manifest.permission.CAMERA},
                                        PermissionsUtil.CAMERA_PERMISSION_REQUEST);
                                break;
                            }
                            case R.id.choose_photo: {
                                askForPermission(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        PermissionsUtil.READ_STORAGE_PERMISSION_REQUEST);
                                break;
                            }
                            case R.id.remove_photo: {
                                final Task task = new DeleteAccountImageTask(getActivity(), username);
                                task.setProgressDialog(viewHolder.profileImageProgress);
                                executePhotoTask(task);
                                break;
                            }
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        setData(account, formDescription);
    }

    private void executePhotoTask(Task task) {
        viewHolder.profileImageProgress.setVisibility(View.VISIBLE);
        viewHolder.profileImageProgress.setIconAnimation(Animation.PULSE);
        // TODO: Test this with "Don't keep activities"
        if (null != setAccountImageTask) {
            setAccountImageTask.cancel(true);
        }
        setAccountImageTask = task;
        task.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getAccountCall.cancel();
        getProfileFormDescriptionTask.cancel(true);
        if (null != setAccountImageTask) {
            setAccountImageTask.cancel(true);
        }
        helper.deleteTemporaryFile();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder = null;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ProfilePhotoUpdatedEvent event) {
        UserProfileUtils.loadProfileImage(getContext(), event, viewHolder.profileImage);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull AccountDataLoadedEvent event) {
        if (event.getAccount().getUsername().equals(username)) {
            account = event.getAccount();
            if (null != viewHolder) {
                setData(account, formDescription);
            }
        }
    }

    public class ViewHolder {
        public final View content;
        public final View loadingIndicator;
        public final CircleImageView profileImage;
        public final TextView username;
        public final ViewGroup fields;
        public final TextView changePhoto;
        public final IconImageView profileImageProgress;

        public ViewHolder(@NonNull View parent) {
            this.content = parent.findViewById(R.id.content);
            this.loadingIndicator = parent.findViewById(R.id.loading_indicator);
            this.profileImage = (CircleImageView) parent.findViewById(R.id.profile_image);
            this.username = (TextView) parent.findViewById(R.id.username);
            this.fields = (ViewGroup) parent.findViewById(R.id.fields);
            this.changePhoto = (TextView) parent.findViewById(R.id.change_photo);
            this.profileImageProgress = (IconImageView) parent.findViewById(R.id.profile_image_progress);
        }
    }

    public void setData(@Nullable final Account account, @Nullable FormDescription formDescription) {
        if (null == viewHolder) {
            return;
        }
        if (null == account || null == formDescription) {
            viewHolder.content.setVisibility(View.GONE);
            viewHolder.loadingIndicator.setVisibility(View.VISIBLE);

        } else {
            viewHolder.content.setVisibility(View.VISIBLE);
            viewHolder.loadingIndicator.setVisibility(View.GONE);
            viewHolder.changePhoto.setEnabled(!account.requiresParentalConsent());
            viewHolder.profileImage.setBorderColorResource(viewHolder.changePhoto.isEnabled() ? R.color.edx_brand_primary_base : R.color.edx_brand_gray_accent);

            if (account.getProfileImage().hasImage()) {
                Glide.with(viewHolder.profileImage.getContext())
                        .load(account.getProfileImage().getImageUrlLarge())
                        .into(viewHolder.profileImage);
            } else {
                Glide.with(EditUserProfileFragment.this)
                        .load(R.drawable.profile_photo_placeholder)
                        .into(viewHolder.profileImage);
            }

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
                                        try {
                                            text = TextUtils.isEmpty(value) ? null : LocaleUtils.getCountryNameFromCode(value);
                                        } catch (InvalidLocaleException e) {
                                            continue;
                                        }
                                        break;
                                    case LANGUAGE:
                                        final List<LanguageProficiency> languageProficiencies = gson.fromJson(accountField, new TypeToken<List<LanguageProficiency>>() {
                                        }.getType());
                                        value = languageProficiencies.isEmpty() ? null : languageProficiencies.get(0).getCode();
                                        try {
                                            text = value == null ? null : LocaleUtils.getLanguageNameFromCode(value);
                                        } catch (InvalidLocaleException e) {
                                            continue;
                                        }
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case CAPTURE_PHOTO_REQUEST: {
                Uri imageUri = helper.getImageUriFromResult();
                if (null != imageUri) {
                    // Rotate image according to exif tag, because exif rotation is creating rotation issues
                    // in thirdparty libraries used for zooming and cropping in this project. [MA-3175]
                    final Uri rotatedImageUri = ImageUtils.rotateImageAccordingToExifTag(getContext(), imageUri);
                    if (null != rotatedImageUri) {
                        imageUri = rotatedImageUri;
                    }
                    startActivityForResult(CropImageActivity.newIntent(getActivity(), imageUri, true), CROP_PHOTO_REQUEST);
                }
                break;
            }
            case CHOOSE_PHOTO_REQUEST: {
                final Uri imageUri = data.getData();
                if (null != imageUri) {
                    startActivityForResult(CropImageActivity.newIntent(getActivity(), imageUri, false), CROP_PHOTO_REQUEST);
                }
                break;
            }
            case CROP_PHOTO_REQUEST: {
                final Uri imageUri = CropImageActivity.getImageUriFromResult(data);
                final Rect cropRect = CropImageActivity.getCropRectFromResult(data);
                if (null != imageUri && null != cropRect) {
                    final Task task = new SetAccountImageTask(getActivity(), username, imageUri, cropRect);
                    task.setProgressDialog(viewHolder.profileImageProgress);
                    executePhotoTask(task);
                    analyticsRegistry.trackProfilePhotoSet(CropImageActivity.isResultFromCamera(data));
                }
                break;
            }
            case EDIT_FIELD_REQUEST: {
                final FormField fieldName = (FormField) data.getSerializableExtra(FormFieldActivity.EXTRA_FIELD);
                final String fieldValue = data.getStringExtra(FormFieldActivity.EXTRA_VALUE);
                executeUpdate(fieldName, fieldValue);
                break;
            }
        }
    }

    private void executeUpdate(FormField field, String fieldValue) {
        final Object valueObject;
        if (field.getDataType() == DataType.LANGUAGE) {
            if (TextUtils.isEmpty(fieldValue)) {
                valueObject = Collections.emptyList();
            } else {
                valueObject = Collections.singletonList(new LanguageProficiency(fieldValue));
            }
        } else {
            valueObject = fieldValue;
        }
        userService.updateAccount(username, Collections.singletonMap(field.getName(), valueObject))
                .enqueue(new AccountDataUpdatedCallback(getActivity(), username,
                        new DialogErrorNotification(this)) {
                    @Override
                    protected void onResponse(@NonNull final Account account) {
                        super.onResponse(account);
                        EditUserProfileFragment.this.account = account;
                        setData(account, formDescription);
                    }
                });
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
            view.setBackgroundColor(view.getResources().getColor(R.color.edx_brand_gray_x_back));
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

    private static TextView createField(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull final FormField field, @NonNull final String value, boolean readOnly, @NonNull View.OnClickListener onClickListener) {
        final TextView textView = (TextView) inflater.inflate(R.layout.edit_user_profile_field, parent, false);
        final SpannableString formattedValue = new SpannableString(value);
        formattedValue.setSpan(new ForegroundColorSpan(parent.getResources().getColor(R.color.edx_brand_gray_base)), 0, formattedValue.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ResourceUtil.getFormattedString(parent.getResources(), R.string.edit_user_profile_field, new HashMap<String, CharSequence>() {{
            put("label", field.getLabel());
            put("value", formattedValue);
        }}));
        Context context = parent.getContext();
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(
                textView, null, null, new IconDrawable(context, FontAwesomeIcons.fa_angle_right)
                        .colorRes(context, R.color.edx_brand_gray_back)
                        .sizeDp(context, 24), null);
        if (readOnly) {
            textView.setEnabled(false);
            textView.setBackgroundColor(textView.getResources().getColor(R.color.edx_brand_gray_x_back));
        } else {
            textView.setOnClickListener(onClickListener);
        }
        parent.addView(textView);
        return textView;
    }

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode) {
        switch (requestCode) {
            case PermissionsUtil.CAMERA_PERMISSION_REQUEST:
                startActivityForResult(helper.createCaptureIntent(getActivity()), CAPTURE_PHOTO_REQUEST);
                break;
            case PermissionsUtil.READ_STORAGE_PERMISSION_REQUEST:
                final Intent galleryIntent = new Intent()
                        .setType("image/*")
                        .setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(galleryIntent, CHOOSE_PHOTO_REQUEST);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPermissionDenied(String[] permissions, int requestCode) {
    }
}
