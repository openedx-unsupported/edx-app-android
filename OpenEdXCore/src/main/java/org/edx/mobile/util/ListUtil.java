package org.edx.mobile.util;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.edx.mobile.logger.Logger;

/**
 * Created by rohan on 2/27/15.
 */
public class ListUtil {

    private static final Logger logger = new Logger(ListUtil.class);

    /**
     * Returns height of list in DP unit. This height includes height of each row and
     * all the dividers between the rows.
     * @param listView
     * @return
     */
    public static int getFullHeightofListView(ListView listView) {
        ListAdapter mAdapter = listView.getAdapter();

        int totalHeight = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            try {
                View mView = mAdapter.getView(i, null, listView);

                mView.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                totalHeight += mView.getMeasuredHeight();
            } catch(Exception ex) {
                logger.error(ex);
            }
        }

        // add height for all the dividers between the rows
        totalHeight += (listView.getDividerHeight() * (mAdapter.getCount()-1) );

        return totalHeight;
    }

    /**
     * Returns height of one row of the list in DP unit.
     * @param listView
     * @return
     */
    public static int getSingleRowHeight(ListView listView) {
        ListAdapter mAdapter = listView.getAdapter();

        for (int i = 0; i < mAdapter.getCount(); i++) {
            try {
                View mView = mAdapter.getView(i, null, listView);

                mView.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                int height = mView.getMeasuredHeight();
                return height;
            } catch(Exception ex) {
                logger.error(ex);
            }
        }

        return 0;
    }
}
