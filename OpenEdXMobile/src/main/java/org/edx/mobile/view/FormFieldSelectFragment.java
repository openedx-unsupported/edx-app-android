package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.widget.TextViewCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.user.FormField;
import org.edx.mobile.user.FormOption;
import org.edx.mobile.user.FormOptions;
import org.edx.mobile.util.LocaleUtils;
import org.edx.mobile.util.ResourceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class FormFieldSelectFragment extends BaseFragment {

    @InjectExtra(FormFieldActivity.EXTRA_FIELD)
    private FormField formField;

    @InjectView(android.R.id.list)
    private ListView listView;

    private static final String COUNTRIES = "countries";
    private static final String LANGUAGES = "languages";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_form_field_select, container, false);
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
            final View instructionsContainer = LayoutInflater.from(view.getContext()).inflate(R.layout.form_field_instructions_header, listView, false);
            final TextView instructions = (TextView) instructionsContainer.findViewById(R.id.instructions);
            final TextView subInstructions = (TextView) instructionsContainer.findViewById(R.id.sub_instructions);
            instructions.setText(formField.getInstructions());
            if (TextUtils.isEmpty(formField.getSubInstructions())) {
                subInstructions.setVisibility(View.GONE);
            } else {
                subInstructions.setText(formField.getSubInstructions());
            }
            listView.addHeaderView(instructionsContainer, null, false);
        }
        if (null != formField.getDataType()) {
            switch (formField.getDataType()) {
                case COUNTRY: {
                    final Locale locale = Locale.getDefault();
                    addDetectedValueHeader(listView,
                            R.string.edit_user_profile_current_location,
                            "current_location",
                            locale.getDisplayCountry(),
                            locale.getCountry(),
                            FontAwesomeIcons.fa_map_marker);
                    break;
                }
                case LANGUAGE: {
                    final Locale locale = Locale.getDefault();
                    addDetectedValueHeader(listView,
                            R.string.edit_user_profile_current_language,
                            "current_language",
                            locale.getDisplayLanguage(),
                            locale.getLanguage(),
                            FontAwesomeIcons.fa_comment);
                    break;
                }
            }
        }
        if (formField.getOptions().isAllowsNone()) {
            final TextView textView = (TextView) LayoutInflater.from(listView.getContext()).inflate(R.layout.edx_selectable_list_item, listView, false);
            final String label = formField.getOptions().getNoneLabel();
            textView.setText(label);
            listView.addHeaderView(textView, new FormOption(label, null), true);
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FormOption item = (FormOption) parent.getItemAtPosition(position);
                getActivity().setResult(Activity.RESULT_OK, new Intent()
                        .putExtra(FormFieldActivity.EXTRA_FIELD, formField)
                        .putExtra(FormFieldActivity.EXTRA_VALUE, item.getValue()));
                getActivity().finish();
            }
        });
        selectCurrentOption();
    }

    private static void addDetectedValueHeader(@NonNull ListView listView, @StringRes int labelRes, @NonNull String labelKey, @NonNull String labelValue, @NonNull String value, @NonNull Icon icon) {
        final TextView textView = (TextView) LayoutInflater.from(listView.getContext()).inflate(R.layout.edx_selectable_list_item, listView, false);
        {
            final SpannableString labelValueSpan = new SpannableString(labelValue);
            labelValueSpan.setSpan(new ForegroundColorSpan(listView.getResources().getColor(R.color.primaryBaseColor)), 0, labelValueSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(ResourceUtil.getFormattedString(listView.getContext().getResources(), labelRes, labelKey, labelValueSpan));
        }
        Context context = textView.getContext();
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(textView,
                new IconDrawable(context, icon)
                        .sizeRes(context, R.dimen.edx_base)
                        .colorRes(context, R.color.neutralDark)
                , null, null, null);
        listView.addHeaderView(textView, new FormOption(labelValue, value), true);
    }

    private void selectCurrentOption() {
        final String currentValue = getArguments().getString(FormFieldActivity.EXTRA_VALUE);
        if (null != currentValue) {
            for (int i = 0; i < listView.getCount(); i++) {
                final FormOption option = (FormOption) listView.getItemAtPosition(i);
                if (null != option && TextUtils.equals(option.getValue(), currentValue)) {
                    listView.setItemChecked(i, true);
                    listView.setSelection(i);
                    break;
                }
            }
        }
    }
}
