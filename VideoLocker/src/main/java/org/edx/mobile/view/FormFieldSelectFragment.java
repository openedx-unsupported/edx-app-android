package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.user.FormField;
import org.edx.mobile.user.FormOption;
import org.edx.mobile.user.FormOptions;
import org.edx.mobile.user.GetFormOptionsTask;

import java.util.ArrayList;
import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class FormFieldSelectFragment extends RoboFragment {

    @InjectExtra(FormFieldActivity.EXTRA_FIELD)
    private FormField formField;

    /*@InjectExtra(value = FormFieldSelectActivity.EXTRA_VALUE, optional = true)
    private String currentValue;*/

    @InjectView(android.R.id.list)
    private ListView listView;

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
        if (formOptions.getReference() != null) {
            new GetFormOptionsTask(getActivity(), formOptions.getReference()) {
                @Override
                protected void onSuccess(List<FormOption> formOptions) throws Exception {
                    options.addAll(formOptions);
                    ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                    selectCurrentOption();
                }
            }.execute();

        } else if (formOptions.getRangeMin() != null && formOptions.getRangeMax() != null) {
            for (int i = formOptions.getRangeMax(); i > formOptions.getRangeMin(); --i) {
                options.add(new FormOption(String.valueOf(i), String.valueOf(i)));
            }
            selectCurrentOption();
        } else if (formOptions.getValues() != null && formOptions.getValues().size() > 0) {
            options.addAll(formOptions.getValues());
            selectCurrentOption();
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
        // TODO: Add header view for "current location/language"
        listView.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.edx_selectable_list_item, options));
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
    }

    private void selectCurrentOption() {
        final String currentValue = getArguments().getString(FormFieldActivity.EXTRA_VALUE);
        if (null != currentValue) {
            for (int i = 0; i < listView.getCount(); i++) {
                final FormOption option = (FormOption) listView.getItemAtPosition(i);
                if (null != option && option.getValue().equals(currentValue)) {
                    listView.setSelection(i);
                    listView.setItemChecked(i, true);
                    break;
                }
            }
        }
    }
}
