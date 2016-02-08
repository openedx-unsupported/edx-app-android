package org.edx.mobile.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ahmedjazzar.rosetta.LanguagesListDialogFragment;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;

import java.util.Locale;

/**
 * Created by ahmedjazzar on 11/18/15.
 */

public class ChangeLanguageDialogFragment extends LanguagesListDialogFragment {

    private final Logger mLogger = new Logger(getClass().getName());
    private final int DIALOG_TITLE_STRING = R.string.settings_language_top;
    private ListView container;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    /**
     *
     * @param savedInstanceState
     * @return a Dialog fragment
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mLogger.debug("Building DialogFragment.");

        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_language, container, false);
        final TextView title_tv = (TextView) view.findViewById(R.id.tv_dialog_title);
        final Button positive_btn = (Button) view.findViewById(R.id.positiveButton);
        final Button negative_btn = (Button) view.findViewById(R.id.negativeButton);

        title_tv.setText(getString(DIALOG_TITLE_STRING));
        container = (ListView) view.findViewById(R.id.lv_dialog_container);
        final CharSequence[] values = getLanguages();
        final ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_single_choice, values);
        container.setAdapter(adapter);

        container.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        container.setItemChecked(getCurrentLocaleIndex(), true);
        container.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onLanguageSelectedLocalized(position, title_tv, positive_btn, negative_btn);
            }
        });

        positive_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onPositiveClick();
            }
        });

        negative_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(view);
        mLogger.debug("DialogFragment built.");
        return builder.create();
    }
}