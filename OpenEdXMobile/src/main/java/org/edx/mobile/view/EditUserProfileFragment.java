package org.edx.mobile.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.event.AccountDataLoadedEvent;
import org.edx.mobile.event.ProfilePhotoUpdatedEvent;
import org.edx.mobile.extenstion.ViewExtKt;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.notifications.DialogErrorNotification;
import org.edx.mobile.model.user.Account;
import org.edx.mobile.model.user.DataType;
import org.edx.mobile.model.user.FieldType;
import org.edx.mobile.model.user.FormDescription;
import org.edx.mobile.model.user.FormField;
import org.edx.mobile.model.user.LanguageProficiency;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.user.UserAPI.AccountDataUpdatedCallback;
import org.edx.mobile.user.UserService;
import org.edx.mobile.util.InvalidLocaleException;
import org.edx.mobile.util.LocaleUtils;
import org.edx.mobile.util.PermissionsUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UserProfileUtils;
import org.edx.mobile.util.images.ImageCaptureHelper;
import org.edx.mobile.util.images.ImageUtils;
import org.edx.mobile.util.observer.EventObserver;
import org.edx.mobile.view.common.TaskMessageCallback;
import org.edx.mobile.viewModel.ProfileViewModel;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;

@AndroidEntryPoint
public class EditUserProfileFragment extends BaseFragment {

    private String username;

    private Call<Account> getAccountCall;

    @Nullable
    private Account account;

    @Nullable
    private ViewHolder viewHolder;

    @Inject
    UserService userService;

    @Inject
    Router router;

    @Inject
    AnalyticsRegistry analyticsRegistry;

    ProfileViewModel profileViewModel;

    @NonNull
    private final ImageCaptureHelper helper = new ImageCaptureHelper();

    private final ActivityResultLauncher<Intent> photoCropLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent resultData = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && resultData != null) {
                    final Uri imageUri = CropImageActivity.getImageUriFromResult(resultData);
                    final Rect cropRect = CropImageActivity.getCropRectFromResult(resultData);
                    if (imageUri != null && cropRect != null) {
                        profileViewModel.uploadProfileImage(requireActivity(), imageUri, cropRect);
                        analyticsRegistry.trackProfilePhotoSet(CropImageActivity.isResultFromCamera(resultData));
                    }
                }
            });

    private final ActivityResultLauncher<Intent> photoChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent resultData = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && resultData != null) {
                    Uri imageUri = resultData.getData();
                    if (imageUri != null) {
                        photoCropLauncher.launch(CropImageActivity.newIntent(requireActivity(), imageUri, false));
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> photoCaptureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Uri imageUri = helper.getImageUriFromResult();
                    if (imageUri != null) {
                        // Rotate image according to exif tag, because exif rotation is creating rotation issues
                        // in third-party libraries used for zooming and cropping in this project. [MA-3175]
                        final Uri rotatedImageUri = ImageUtils.rotateImageAccordingToExifTag(requireActivity(), imageUri);
                        if (rotatedImageUri != null) {
                            imageUri = rotatedImageUri;
                        }
                        photoCropLauncher.launch(CropImageActivity.newIntent(requireActivity(), imageUri, true));
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent resultData = result.getData();
                if (result.getResultCode() == Activity.RESULT_OK && resultData != null) {
                    final FormField fieldName = (FormField) resultData.getSerializableExtra(FormFieldActivity.EXTRA_FIELD);
                    final String fieldValue = resultData.getStringExtra(FormFieldActivity.EXTRA_VALUE);
                    executeUpdate(fieldName, fieldValue);
                }
            }
    );

    private final ActivityResultLauncher<String> storagePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    final Intent galleryIntent = new Intent()
                            .setType("image/*")
                            .setAction(Intent.ACTION_GET_CONTENT);
                    photoChooserLauncher.launch(galleryIntent);
                } else {
                    showPermissionDeniedMessage();
                }
            }
    );

    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    photoCaptureLauncher.launch(helper.createCaptureIntent(requireActivity()));
                } else {
                    showPermissionDeniedMessage();
                }
            });

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        EventBus.getDefault().register(this);
        parseExtras();

        final Activity activity = requireActivity();
        final TaskMessageCallback mCallback = activity instanceof TaskMessageCallback ? (TaskMessageCallback) activity : null;
        getAccountCall = userService.getAccount(username);
        getAccountCall.enqueue(new AccountDataUpdatedCallback(
                activity, username,
                null, // Disable default loading indicator, we have our own
                mCallback, CallTrigger.LOADING_CACHED));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initObservers();

        viewHolder = new ViewHolder(view);
        viewHolder.profileImageProgress.setVisibility(View.GONE);
        viewHolder.username.setText(username);
        viewHolder.username.setContentDescription(ResourceUtil.getFormattedString(getResources(), R.string.profile_username_description, "username", username));

        viewHolder.changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popup = new PopupMenu(requireActivity(), v);
                popup.getMenuInflater().inflate(R.menu.change_photo, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.take_photo ->
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
                            case R.id.choose_photo ->
                                    storagePermissionLauncher.launch(PermissionsUtil.getReadStoragePermission());
                            case R.id.remove_photo -> profileViewModel.removeProfileImage();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
        setData(account, profileViewModel.getFormDescription());
    }

    private void initObservers() {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        profileViewModel.setProfileFormDescription(getResources().openRawResource(R.raw.profiles));

        profileViewModel.getShowProgress().observe(getViewLifecycleOwner(), new EventObserver<>(showProgress -> {
            if (viewHolder != null) {
                ViewExtKt.setVisibility(viewHolder.profileImageProgress, showProgress);
            }
            return null;
        }));
    }

    private void parseExtras() {
        username = getArguments().getString(EditUserProfileActivity.EXTRA_USERNAME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getAccountCall.cancel();
        helper.deleteTemporaryFile();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewHolder = null;
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull ProfilePhotoUpdatedEvent event) {
        UserProfileUtils.loadProfileImage(requireContext(), event, viewHolder.profileImage);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onEventMainThread(@NonNull AccountDataLoadedEvent event) {
        if (event.getAccount().getUsername().equals(username)) {
            account = event.getAccount();
            if (null != viewHolder) {
                setData(account, profileViewModel.getFormDescription());
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
        public final TextView tvProfileVisibilityOff;
        public final LinearLayout llProfileVisibilityOff;
        public final CircularProgressIndicator profileImageProgress;

        public ViewHolder(@NonNull View parent) {
            this.content = parent.findViewById(R.id.content);
            this.loadingIndicator = parent.findViewById(R.id.loading_indicator);
            this.profileImage = parent.findViewById(R.id.profile_image);
            this.username = parent.findViewById(R.id.username);
            this.fields = parent.findViewById(R.id.fields);
            this.tvProfileVisibilityOff = parent.findViewById(R.id.tv_profile_visibility_off);
            this.llProfileVisibilityOff = parent.findViewById(R.id.ll_profile_visibility_off);
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

        } else {
            viewHolder.content.setVisibility(View.VISIBLE);
            viewHolder.loadingIndicator.setVisibility(View.GONE);
            if (account.requiresParentalConsent()) {
                viewHolder.changePhoto.setVisibility(View.GONE);
            } else {
                viewHolder.changePhoto.setVisibility(View.VISIBLE);
            }
            viewHolder.profileImage.setBorderColorResource(viewHolder.changePhoto.isEnabled() ? R.color.primaryBaseColor : R.color.primaryXLightColor);

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
                if (null == field.getFieldType() ||
                        (account.requiresParentalConsent() && field.getFieldType() == FieldType.SWITCH)) {
                    // ignore this field if Missing field type OR user didn't set YOB or is less than 13
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
                                editProfileLauncher.launch(FormFieldActivity.newIntent(requireActivity(), field, value));
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
            if (account.requiresParentalConsent()) {
                viewHolder.llProfileVisibilityOff.setVisibility(View.VISIBLE);
                String profileVisibilityMessage = ResourceUtil.getFormattedString(
                        getResources(),
                        R.string.profile_visibility_off_message,
                        "platform_name",
                        getString(R.string.platform_name)).toString();
                viewHolder.tvProfileVisibilityOff.setText(profileVisibilityMessage);
            } else {
                viewHolder.llProfileVisibilityOff.setVisibility(View.GONE);
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
                .enqueue(new AccountDataUpdatedCallback(requireActivity(), username,
                        new DialogErrorNotification(this)) {
                    @Override
                    protected void onResponse(@NonNull final Account account) {
                        super.onResponse(account);
                        EditUserProfileFragment.this.account = account;
                        setData(account, profileViewModel.getFormDescription());
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
        final TabLayout group = view.findViewById(R.id.options);

        final Tab optionOne = group.getTabAt(0);
        final Tab optionTwo = group.getTabAt(1);

        optionOne.setText(field.getOptions().getValues().get(0).getName());
        optionOne.setTag(field.getOptions().getValues().get(0).getValue());
        optionTwo.setText(field.getOptions().getValues().get(1).getName());
        optionTwo.setTag(field.getOptions().getValues().get(1).getValue());

        for (int i = 0; i < group.getTabCount(); i++) {
            final Tab child = group.getTabAt(i);
            if (child != null && value.equals(child.getTag())) {
                child.select();
                break;
            }
        }
        if (readOnly) {
            group.setEnabled(false);
        } else {
            group.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(Tab tab) {
                    if (tab.getTag() != null) {
                        switchListener.onSwitch((String) tab.getTag());
                    }
                }

                @Override
                public void onTabUnselected(Tab tab) {
                }

                @Override
                public void onTabReselected(Tab tab) {
                }
            });
        }
        parent.addView(view);
        return view;
    }

    private static TextView createField(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, @NonNull final FormField field, @NonNull final String value, boolean readOnly, @NonNull View.OnClickListener onClickListener) {
        final TextView textView = (TextView) inflater.inflate(R.layout.edit_user_profile_field, parent, false);
        final SpannableString formattedValue = new SpannableString(value);
        formattedValue.setSpan(new ForegroundColorSpan(parent.getResources().getColor(R.color.neutralXXDark)), 0, formattedValue.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(ResourceUtil.getFormattedString(parent.getResources(), R.string.edit_user_profile_field, new HashMap<>() {{
            put("label", field.getLabel());
            put("value", formattedValue);
        }}));

        if (readOnly) {
            textView.setEnabled(false);
        } else {
            textView.setOnClickListener(onClickListener);
        }
        parent.addView(textView);
        return textView;
    }
}
