package org.edx.mobile.services;

import android.view.View;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.model.api.SyncLastAccessedSubsectionResponse;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.task.GetLastAccessedTask;
import org.edx.mobile.task.SyncLastAccessedTask;

/**
 * This can be injected in the future when we use DI
 */
public class LastAccessManager {
    public static interface LastAccessManagerCallback{
        boolean isFetchingLastAccessed();
        void setFetchingLastAccessed(boolean accessed);
        void showLastAccessedView(String lastAccessedSubSectionId, String courseId, View view);
    }

    protected final Logger logger = new Logger(getClass().getName());
    private static LastAccessManager manager;

    public static final LastAccessManager getSharedInstance(){
        if ( manager == null )
            manager = new LastAccessManager();
        return manager;
    }

    public void fetchLastAccessed(final LastAccessManagerCallback callback, final String courseId) {
        fetchLastAccessed(callback, null, courseId);
    }

    public void fetchLastAccessed(final LastAccessManagerCallback callback, final View view, final String courseId){
        try{
            if(!callback.isFetchingLastAccessed()) {
                if(courseId!=null && getProfile()!=null && getProfile().username!=null){
                    String prefName = PrefManager.getPrefNameForLastAccessedBy(getProfile()
                        .username, courseId);
                    final PrefManager prefManager = new PrefManager(MainApplication.instance(), prefName);
                    final String prefModuleId = prefManager.getLastAccessedSubsectionId();

                    logger.debug("Last Accessed Module ID from Preferences "
                        +prefModuleId);

                    callback.showLastAccessedView(prefModuleId, courseId, view);
                    GetLastAccessedTask getLastAccessedTask = new GetLastAccessedTask(MainApplication.instance(),courseId) {
                        @Override
                        public void onSuccess(SyncLastAccessedSubsectionResponse result) {
                            syncWithServerOnSuccess(result, prefModuleId, prefManager, courseId, callback, view);
                        }
                        @Override
                        public void onException(Exception ex) {
                            callback.setFetchingLastAccessed( false );
                            logger.error(ex);
                        }
                    };

                    callback.setFetchingLastAccessed( true );
                    getLastAccessedTask.execute( );
                }
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    private void syncWithServerOnSuccess(SyncLastAccessedSubsectionResponse result,
                                         String prefModuleId,
                                         PrefManager prefManager,
                                         String courseId,
                                         LastAccessManagerCallback callback,
                                         View view
                                         ){
        String server_moduleId = null;
        if(result!=null && result.getLastVisitedModuleId()!=null){
            //Handle the last Visited Module received from Sever
            server_moduleId = result.getLastVisitedModuleId();
            logger.debug("Last Accessed Module ID from Server Get "
                +server_moduleId);
            if(prefManager.isSyncedLastAccessedSubsection()){
                //If preference last accessed flag is true, put the last access fetched
                //from server in Prefernces and display it on Last Accessed.
                prefManager.putLastAccessedSubsection(server_moduleId, true);
                callback.showLastAccessedView(server_moduleId, courseId, view);
            }else{
                //Preference's last accessed is not synched with server,
                //Sync with server and display the result from server on UI.
                if(prefModuleId!=null && prefModuleId.length()>0){
                    syncLastAccessedWithServer(prefManager, view, prefModuleId, courseId, callback);
                }
            }
        }else{
            //There is no Last Accessed module on the server
            if(prefModuleId!=null && prefModuleId.length()>0){
                syncLastAccessedWithServer(prefManager, view, prefModuleId, courseId, callback);
            }
        }
        callback.setFetchingLastAccessed( false );
    }


    private void syncLastAccessedWithServer(final PrefManager prefManager,
                                            final View view,
                                            String prefModuleId,
                                            final String courseId,
                                            final LastAccessManagerCallback callback){
        try{
            SyncLastAccessedTask syncLastAccessTask = new SyncLastAccessedTask(
                MainApplication.instance(),courseId, prefModuleId) {
                @Override
                public void onSuccess(SyncLastAccessedSubsectionResponse result) {
                    if(result!=null && result.getLastVisitedModuleId()!=null){
                        prefManager.putLastAccessedSubsection(result.getLastVisitedModuleId(), true);
                        logger.debug("Last Accessed Module ID from Server Sync "
                            +result.getLastVisitedModuleId());
                         callback.showLastAccessedView(result.getLastVisitedModuleId(), courseId, view);
                    }
                }

                @Override
                public void onException(Exception ex) {
                    logger.error(ex);
                }
            };
            syncLastAccessTask.execute( );
        }catch(Exception e){
            logger.error(e);
        }
    }

    protected ProfileModel getProfile() {
        PrefManager prefManager = new PrefManager(MainApplication.instance(), PrefManager.Pref.LOGIN);
        return prefManager.getCurrentUserProfile();
    }
}
