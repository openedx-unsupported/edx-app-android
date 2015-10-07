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
import android.widget.EditText;
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

public class FormFieldTextAreaFragment extends RoboFragment {

    @InjectExtra(FormFieldSelectActivity.EXTRA_FIELD)
    private FormField formField;

    @InjectExtra(FormFieldSelectActivity.EXTRA_VALUE)
    private String currentValue;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_form_field_textarea, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(formField.getLabel());
        final EditText editText = (EditText) view.findViewById(R.id.text);
        editText.setHint(formField.getPlaceholder());
        editText.setText(currentValue);
        view.findViewById(R.id.submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setResult(Activity.RESULT_OK,
                        new Intent()
                                .putExtra(FormFieldTextAreaActivity.EXTRA_FIELD_NAME, formField.getName())
                                .putExtra(FormFieldTextAreaActivity.EXTRA_VALUE, editText.getText().toString()));
                getActivity().finish();
            }
        });
    }
}
