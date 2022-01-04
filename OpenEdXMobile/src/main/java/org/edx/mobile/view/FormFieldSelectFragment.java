package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.databinding.FragmentFormFieldSelectBinding;
import org.edx.mobile.user.FormField;
import org.edx.mobile.user.FormOption;
import org.edx.mobile.user.FormOptions;
import org.edx.mobile.util.LocaleUtils;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.UiUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FormFieldSelectFragment extends BaseFragment {

    private static final String COUNTRIES = "countries";
    private static final String LANGUAGES = "languages";

    private FormField formField;
    private FragmentFormFieldSelectBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseExtras();
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFormFieldSelectBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(formField.getLabel());
        final List<FormOption> options = new ArrayList<>();
        final FormOptions formOptions = formField.getOptions();
        final ArrayAdapter<FormOption> adapter = new ArrayAdapter<>(getActivity(), R.layout.edx_selectable_list_item, options);
        if (formOptions.getReference() != null) {
            if (COUNTRIES.equals(formOptions.getReference())) {
                options.addAll(LocaleUtils.getCountries());
            } else if (LANGUAGES.equals(formOptions.getReference())) {
                options.addAll(LocaleUtils.getLanguages());
            }
            adapter.notifyDataSetChanged();
            selectCurrentOption();
        } else if (formOptions.getRangeMin() != null && formOptions.getRangeMax() != null) {
            for (int i = formOptions.getRangeMax(); i >= formOptions.getRangeMin(); --i) {
                options.add(new FormOption(String.valueOf(i), String.valueOf(i)));
            }
        } else if (formOptions.getValues() != null && formOptions.getValues().size() > 0) {
            options.addAll(formOptions.getValues());
        }
        if (!TextUtils.isEmpty(formField.getInstructions())) {
            final View instructionsContainer = LayoutInflater.from(view.getContext()).inflate(R.layout.form_field_instructions_header, binding.list, false);
            final TextView instructions = instructionsContainer.findViewById(R.id.instructions);
            final TextView subInstructions = instructionsContainer.findViewById(R.id.sub_instructions);
            instructions.setText(formField.getInstructions());
            if (TextUtils.isEmpty(formField.getSubInstructions())) {
                subInstructions.setVisibility(View.GONE);
            } else {
                subInstructions.setText(formField.getSubInstructions());
            }
            binding.list.addHeaderView(instructionsContainer, null, false);
        }
        if (null != formField.getDataType()) {
            switch (formField.getDataType()) {
                case COUNTRY: {
                    final Locale locale = Locale.getDefault();
                    addDetectedValueHeader(binding.list,
                            R.string.edit_user_profile_current_location,
                            "current_location",
                            locale.getDisplayCountry(),
                            locale.getCountry(),
                            R.drawable.ic_place);
                    break;
                }
                case LANGUAGE: {
                    final Locale locale = Locale.getDefault();
                    addDetectedValueHeader(binding.list,
                            R.string.edit_user_profile_current_language,
                            "current_language",
                            locale.getDisplayLanguage(),
                            locale.getLanguage(),
                            R.drawable.ic_language);
                    break;
                }
            }
        }
        if (formField.getOptions().isAllowsNone()) {
            final TextView textView = (TextView) LayoutInflater.from(binding.list.getContext()).inflate(R.layout.edx_selectable_list_item, binding.list, false);
            final String label = formField.getOptions().getNoneLabel();
            textView.setText(label);
            binding.list.addHeaderView(textView, new FormOption(label, null), true);
        }
        binding.list.setAdapter(adapter);
        binding.list.setOnItemClickListener((parent, view1, position, id) -> {
            final FormOption item = (FormOption) parent.getItemAtPosition(position);
            getActivity().setResult(Activity.RESULT_OK, new Intent()
                    .putExtra(FormFieldActivity.EXTRA_FIELD, formField)
                    .putExtra(FormFieldActivity.EXTRA_VALUE, item.getValue()));
            getActivity().finish();
        });
        selectCurrentOption();
    }

    private static void addDetectedValueHeader(@NonNull ListView listView, @StringRes int labelRes, @NonNull String labelKey, @NonNull String labelValue, @NonNull String value, @DrawableRes int iconResId) {
        final TextView textView = (TextView) LayoutInflater.from(listView.getContext()).inflate(R.layout.edx_selectable_list_item, listView, false);
        {
            final SpannableString labelValueSpan = new SpannableString(labelValue);
            labelValueSpan.setSpan(new ForegroundColorSpan(listView.getResources().getColor(R.color.primaryBaseColor)), 0, labelValueSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(ResourceUtil.getFormattedString(listView.getContext().getResources(), labelRes, labelKey, labelValueSpan));
        }
        Context context = textView.getContext();
        UiUtils.INSTANCE.setTextViewDrawableStart(context, textView, iconResId, R.dimen.edx_base,
                R.color.neutralDark);
        listView.addHeaderView(textView, new FormOption(labelValue, value), true);
    }

    private void parseExtras() {
        formField = (FormField) getArguments().getSerializable(FormFieldActivity.EXTRA_FIELD);
    }

    private void selectCurrentOption() {
        final String currentValue = getArguments().getString(FormFieldActivity.EXTRA_VALUE);
        if (null != currentValue) {
            for (int i = 0; i < binding.list.getCount(); i++) {
                final FormOption option = (FormOption) binding.list.getItemAtPosition(i);
                if (null != option && TextUtils.equals(option.getValue(), currentValue)) {
                    binding.list.setItemChecked(i, true);
                    binding.list.setSelection(i);
                    break;
                }
            }
        }
    }
}
