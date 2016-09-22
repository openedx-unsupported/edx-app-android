package org.edx.mobile.view.dialog;

import java.util.HashMap;

public interface IListDialogCallback {
    public void onItemClicked(HashMap<String, String> lang);
    public void onCancelClicked();
}
