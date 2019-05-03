package org.edx.mobile.tta.analytics;

import android.content.Context;
import android.util.Log;

import com.google.inject.Inject;

import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.tta.analytics.analytics_enums.Action;
import org.edx.mobile.tta.analytics.analytics_enums.Page;
import org.edx.mobile.tta.analytics.analytics_enums.Source;
import org.edx.mobile.tta.data.model.SuccessResponse;
import org.edx.mobile.tta.task.content.course.scorm.GetAllDownloadedScromCountTask;
import org.edx.mobile.tta.utils.BreadcrumbUtil;
import org.edx.mobile.util.Sha1Util;

import java.util.ArrayList;

import roboguice.RoboGuice;

import static org.edx.mobile.util.BrowserUtil.environment;
import static org.edx.mobile.util.BrowserUtil.loginPrefs;

/**
 * Created by manprax on 5/1/17.
 */

public class Analytic {

    private Context ctx;
    private int analyticBatchCount = 100;

    @Inject
    public Analytic(Context mctx) {
        this.ctx = mctx;
    }

    public void addMxAnalytics_db(String metadata, Action action, String page, Source source, String actionId) {
        String username = loginPrefs.getUsername();

        //save analytics to  local db first
        //download entry to db
        if (username != null) {
            AnalyticModel model = new AnalyticModel();
            model.user_id = username;
            model.action = String.valueOf(action);
            model.metadata = metadata;
            model.page = page;
            model.source = String.valueOf(source);
            model.nav = BreadcrumbUtil.getBreadcrumb();
            model.action_id = actionId;

            model.setEvent_date();
            model.setStatus(0);

            environment.getStorage().addAnalytic(model);
        }
    }

    public void addMxAnalytics_db(String metadata, Action action, String page, Source source, String actionId, String nav) {
        String username = loginPrefs.getUsername();

        //save analytics to  local db first
        //download entry to db
        if (username != null) {
            AnalyticModel model = new AnalyticModel();
            model.user_id = username;
            model.action = String.valueOf(action);
            model.metadata = metadata;
            model.page = page;
            model.source = String.valueOf(source);
            model.nav = nav;
            model.action_id = actionId;

            model.setEvent_date();
            model.setStatus(0);

            environment.getStorage().addAnalytic(model);
        }
    }

    public void addTinCanAnalyticDB(String tincan_obj, String course_id) {
        String username = loginPrefs.getUsername();

        //save Tincan analytics to  local db first
        //download entry to db
        if (username == null || tincan_obj.isEmpty()) {
            Log.d("TinCanDbEntry", "unable to add");
            return;
        }

        AnalyticModel model = new AnalyticModel();
        model.user_id = username;
        model.action = String.valueOf(Action.TinCanObject);
        model.metadata = tincan_obj;
        //we are adding courseid to page in case of tincan
        model.page = course_id;
        model.source = String.valueOf(Source.Mobile);
        model.nav = BreadcrumbUtil.getBreadcrumb();
        model.action_id = null;
        model.setEvent_date();
        model.setStatus(0);

        environment.getStorage().addAnalytic(model);
    }

    public void deleteAnalytics(ArrayList<AnalyticModel> analyticModelList) {
        environment.getStorage().removeAnalytics(getIds(analyticModelList), getINQueryParams(getIds(analyticModelList)));
    }

    public void addScromDownload_db(Context mContext, final DownloadEntry model) {
        GetAllDownloadedScromCountTask getAllDownloadedScromCountTask = new GetAllDownloadedScromCountTask(mContext) {
            @Override
            protected void onSuccess(Integer scromCount) throws Exception {
                super.onSuccess(scromCount);
                scromCount = scromCount + 1;
                //update analytics
                addMxAnalytics_db("" + scromCount, Action.OfflineSections,
                        String.valueOf(Page.ProfilePage), Source.Mobile, null);

                //download entry to db
                environment.getStorage().addDownload(model);
            }
        };
        getAllDownloadedScromCountTask.execute();
    }

    public void addScromCountAnalytic_db(Context mContext) {
        GetAllDownloadedScromCountTask getAllDownloadedScromCountTask = new GetAllDownloadedScromCountTask(mContext) {
            @Override
            protected void onSuccess(Integer scromCount) throws Exception {
                super.onSuccess(scromCount);
                addMxAnalytics_db("" + scromCount, Action.OfflineSections,
                        String.valueOf(Page.ProfilePage), Source.Mobile, null);
            }
        };
        getAllDownloadedScromCountTask.execute();
    }

    private String getINQueryParams(String[] ids) {
        StringBuilder sb = new StringBuilder();

        if (ids == null || ids.length == 0)
            return "";

        for (int i = 0; i < ids.length; i++) {

            if (i == ids.length - 1)
                sb.append("?");
            else
                sb.append("?,");
        }

        return sb.toString();
    }

    public void syncAnalytics() {
        syncMXAnalytics();
        syncTinCanAnalytics();
    }

    private String[] getIds(ArrayList<AnalyticModel> analyticModelList) {
        String[] mIds = new String[analyticModelList.size() + 4];

        for (int index = 0; index < analyticModelList.size(); index++) {
            mIds[index] = analyticModelList.get(index).analytic_id;
        }
        return mIds;
    }

    private ArrayList<AnalyticModel> getMxAnalytics() {
        ArrayList<AnalyticModel> list = new ArrayList<>();
        try {
            list = environment.getStorage().getMxAnalytics(analyticBatchCount, 0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Analtics", "Dbfetch fail");
        }
        return list;
    }

    private ArrayList<AnalyticModel> getTincanAnalytics() {
        ArrayList<AnalyticModel> list = new ArrayList<>();
        try {
            list = environment.getStorage().getTincanAnalytics(analyticBatchCount, 0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("TincanAnaltics", "Dbfetch fail");
        }
        return list;
    }

    private void syncMXAnalytics() {
        ArrayList<AnalyticModel> list = getMxAnalytics();

        if (list == null || list.size() == 0)
            return;

        final ArrayList<AnalyticModel> finalList = list;
        final MXAnalyticsTask task = new MXAnalyticsTask(ctx, finalList) {
            @Override
            protected void onSuccess(SuccessResponse mx_AnalyticsResponse) throws Exception {
                super.onSuccess(mx_AnalyticsResponse);
                deleteAnalytics(finalList);
                Log.d("MXAnaltics", "successfully updated mx Normal analytics");
            }

            @Override
            protected void onException(Exception ex) {
                Log.d("MXAnaltics", "fail to update mx analytics");
            }
        };
        task.execute();
    }

    private void syncTinCanAnalytics() {
        ArrayList<AnalyticModel> list = getTincanAnalytics();

        if (list == null || list.size() == 0)
            return;
        TincanRequest tincan_item = new TincanRequest();
        tincan_item.user_id = list.get(0).user_id;
        tincan_item.version = 1;// 1 means new analytis

        Tincan tincan = new Tincan();
        for (AnalyticModel item : list) {
            tincan = new Tincan();

            tincan.analytic_id = item.analytic_id;
            tincan.course_id = item.page;
            tincan.metadata = item.metadata;

            tincan_item.tincanObj.add(tincan);
        }

        final ArrayList<AnalyticModel> finalList = list;
        final TinCanAnalyticTask task = new TinCanAnalyticTask(ctx, tincan_item) {
            @Override
            protected void onSuccess(SuccessResponse mx_AnalyticsResponse) throws Exception {
                super.onSuccess(mx_AnalyticsResponse);
                deleteAnalytics(finalList);
                Log.d("MXAnaltics", "successfully updated mx TinCanAnalytics");
            }

            @Override
            protected void onException(Exception ex) {
                Log.d("MXAnaltics", "fail to update tincan analytics");
            }
        };
        task.execute();
    }
}
