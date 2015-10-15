package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.edx.mobile.R;
import org.edx.mobile.user.FormField;
import org.edx.mobile.user.FormOption;
import org.edx.mobile.user.FormOptions;
import org.edx.mobile.user.GetFormOptionsTask;

import java.util.ArrayList;
import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;

public class FormFieldSelectFragment extends RoboFragment {

    @InjectExtra(FormFieldSelectActivity.EXTRA_FIELD)
    private FormField formField;

    /*@InjectExtra(value = FormFieldSelectActivity.EXTRA_VALUE, optional = true)
    private String currentValue;*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_form_field_select, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(formField.getLabel());
        final ListView listView = (ListView) view.findViewById(android.R.id.list);
        final List<FormOption> options = new ArrayList<>();
        final FormOptions formOptions = formField.getOptions();
        if (formOptions.getReference() != null) {
            new GetFormOptionsTask(getActivity(), formOptions.getReference()) {
                @Override
                protected void onSuccess(List<FormOption> formOptions) throws Exception {
                    options.addAll(formOptions);
                    ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
                }
            }.execute();

        } else if (formOptions.getRangeMin() != null && formOptions.getRangeMax() != null) {
            for (int i = formOptions.getRangeMax(); i > formOptions.getRangeMin(); --i) {
                options.add(new FormOption(String.valueOf(i), String.valueOf(i)));
            }
        } else if (formOptions.getValues() != null && formOptions.getValues().size() > 0) {
            options.addAll(formOptions.getValues());
        }
        listView.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, options));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FormOption item = (FormOption) parent.getItemAtPosition(position);
                getActivity().setResult(Activity.RESULT_OK, new Intent()
                        .putExtra(FormFieldSelectActivity.EXTRA_FIELD, formField)
                        .putExtra(FormFieldSelectActivity.EXTRA_VALUE, item.getValue()));
                getActivity().finish();
            }
        });
    }
}
