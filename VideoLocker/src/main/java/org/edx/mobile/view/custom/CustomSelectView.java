package org.edx.mobile.view.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.util.UiUtil;

import java.util.List;

/**
 * Created by rohan on 2/16/15.
 */
public class CustomSelectView<T> extends ETextView implements View.OnClickListener,
        DialogInterface.OnClickListener {

    private String hint;
    private String prompt;
    private List<T> items;
    private T selectedItem;
    private int checkedItemIndex = 0;

    public CustomSelectView(Context context) {
        super(context);
        setOnClickListener(this);
        setUIParams();
    }

    public CustomSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
        setUIParams();
    }

    public CustomSelectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnClickListener(this);
        setUIParams();
    }

    /**
     * This sets the common view parameters for Custom SelectView
     */
    private void setUIParams(){
        int padding = (int)UiUtil.getParamsInDP(getResources(),10);
        setPadding(padding,padding,padding,padding);
        setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
    }

    public void setHint(String hint) {
        this.hint = hint;
        showHint();
    }

    /**
     * Show the hint text on the custom select view
     */
    private void showHint() {
        setText(hint);
        setTextColor(getResources().getColor(R.color.hint_grey_text));
        setTextSize(UiUtil.getParamsInDP(getResources(), 4));
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * Set the list of items to be displayed when the selectView is clicked
     * @param items - List of items
     * @param defaultItem - Default item to be shown as selected
     */
    public void setItems(List<T> items, T defaultItem) {
        this.items = items;
        this.selectedItem = defaultItem;

        // calculate checkedItemIndex
        if (items != null && selectedItem != null) {
            int i=0;
            for (T item : items) {
                if (item.equals(selectedItem)) {
                    checkedItemIndex = i;
                    break;
                }
                i++;
            }
        }
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if (prompt != null) builder.setTitle(prompt);
        if (items != null) {
            builder.setSingleChoiceItems(getItemsAsStringArray(), checkedItemIndex, this);
            builder.show();
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        checkedItemIndex = which;
        selectedItem = items.get(which);
        select(selectedItem);
        dialog.dismiss();
    }

    /**
     * Call this function when a value is selected in the Dialog for custom select
     * @param item - Selected Item
     */
    private void select(T item) {
        //Check if the selected options value is empty
        //The current json contains '--' as the first value
        //and hence show the hint text instead of '--' in the select view
        if (TextUtils.isEmpty(item.toString())
                ||item.toString().equalsIgnoreCase("--")) {
            showHint();
        } else {
            setText(item.toString());
            setTextColor(getResources().getColor(R.color.grey_text));
            setTextSize(UiUtil.getParamsInDP(getResources(), 4));
        }
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    private String[] getItemsAsStringArray() {
        if (items != null) {
            String[] stringItems = new String[items.size()];
            for (int i=0; i<items.size(); i++) {
                stringItems[i] = items.get(i).toString();
            }
            return stringItems;
        }
        return null;
    }
}
