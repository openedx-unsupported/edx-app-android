package org.edx.mobile.social.google;

import java.io.IOException;
import org.edx.mobile.social.ISocialImpl;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GoogleOauth2 extends ISocialImpl {
    
    private String accessToken;
    private String mEmail; // Received from newChooseAccountIntent(); passed to getToken()
    private static final int REQUEST_AUTHORIZATION = 343;
    
    // https://developers.google.com/+/api/oauth
    
    //private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";
//  private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/plus.login";
    // https://www.googleapis.com/auth/plus.profile.emails.read
//  private static final String SCOPE1      = "plus.login";
//  private static final String SCOPE2      = "plus.profile.emails.read";
    
    public GoogleOauth2(Activity activity) {
        super(activity);
    }

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    private void pickUserAccount() {
        try {
            String[] accountTypes = new String[]{"com.google"};
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    accountTypes, true, null, null, null, null);

            // check if play-services are installed
            int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity.get());
            if (ConnectionResult.SUCCESS == result) {
                activity.get().startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
                logger.debug("Launching google account picker ...");
            } else {
                // display user friendly error message

                logger.debug("Play services are missing ...");
                GooglePlayServicesUtil.getErrorDialog(result, activity.get(), 100).show();
            }
        } catch (ActivityNotFoundException ex) {
            logger.debug("Google-play-services are missing? cannot login by google");
        }
    }

    /**
     * Attempts to retrieve the username.
     * If the account is not yet known, invoke the picker. Once the account is known,
     * start an instance of the AsyncTask to get the auth token and do work with it.
     */
    private void getUsername() {
        if (mEmail == null) {
            pickUserAccount();
        } else {
            new GetUsernameTask(activity.get(), mEmail, getScopes()).execute();
        }
    }

    public class GetUsernameTask extends AsyncTask<Object, Object, Object> {
        Activity mActivity;
        String mScope;
        String mEmail;

        GetUsernameTask(Activity activity, String name, String scope) {
            this.mActivity = activity;
            this.mScope = scope;
            this.mEmail = name;
        }

        /**
         * Executes the asynchronous job. This runs when you call execute()
         * on the AsyncTask instance.
         */
        @Override
        protected Object doInBackground(Object... params) {
            try {
                String token = fetchToken();
                if (token != null) {
                    // Insert the good stuff here.
                    // Use the token to access the user's Google data.
                    return token;
                }
            } catch (IOException e) {
                // The fetchToken() method handles Google-specific exceptions,
                // so this indicates something went wrong at a higher level.
                // TIP: Check for network connectivity before starting the AsyncTask.
                logger.error(e);
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
            if (result != null && result instanceof String) {
                accessToken = (String) result;
                logger.debug("Google oauth2: accessToken: " + accessToken);
                if (callback != null) {
                    callback.onLogin(accessToken);
                }
            }
        }

        /**
         * Gets an authentication token from Google and handles any
         * GoogleAuthException that may occur.
         */
        protected String fetchToken() throws IOException {
            try {
                logger.debug("Fetching google oauth2 token ...");
                return GoogleAuthUtil.getToken(activity.get(), mEmail, mScope);
            } catch (UserRecoverableAuthException userRecoverableException) {
                // GooglePlayServices.apk is either old, disabled, or not present
                // so we need to show the user some UI in the activity to recover.
                logger.debug("User recoverable error occurred");
                logger.error(userRecoverableException);
                
                // Requesting an authorization code will always throw
                  // UserRecoverableAuthException on the first call to GoogleAuthUtil.getToken
                  // because the user must consent to offline access to their data.  After
                  // consent is granted control is returned to your activity in onActivityResult
                  // and the second call to GoogleAuthUtil.getToken will succeed.
                
                activity.get().startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
            } catch (GoogleAuthException fatalException) {
                logger.warn("google auth error occurred");
                // Some other type of unrecoverable exception has occurred.
                // Report and log the error as appropriate for your app.
                logger.error(fatalException);
            }
            return null;
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == Activity.RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                getUsername();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                // Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == REQUEST_AUTHORIZATION) {
            // after authorization, try to get token again
            if (resultCode == Activity.RESULT_OK) {
                // re-try go get the auth token
                getUsername();
            }
        }
    }
    
    @Override
    public void onActivityCreated(Activity arg0, Bundle arg1) {
        // nothing to do
    }

    @Override
    public void onActivityDestroyed(Activity arg0) {
        // nothing to do
    }

    @Override
    public void onActivityPaused(Activity arg0) {
        // nothing to do
    }

    @Override
    public void onActivityResumed(Activity arg0) {
        // nothing to do
    }

    @Override
    public void onActivitySaveInstanceState(Activity arg0, Bundle arg1) {
        // nothing to do
    }

    @Override
    public void onActivityStarted(Activity arg0) {
        // nothing to do
    }

    @Override
    public void onActivityStopped(Activity arg0) {
        // nothing to do
    }
    
    @Override
    public void login() {
        pickUserAccount();
    }

    @Override
    public void logout() {
        if (accessToken != null) {
            try {
                GoogleAuthUtil.clearToken(activity.get(), accessToken);
                logger.debug("Google logged out");
            } catch (GooglePlayServicesAvailabilityException e) {
                logger.error(e);
            } catch (GoogleAuthException e) {
                logger.error(e);
            } catch (IOException e) {
                logger.error(e);
            }
        }
    }

    private String getScopes() {
        String login = "https://www.googleapis.com/auth/plus.login";
//      String email = "https://www.googleapis.com/auth/plus.profile.emails.read";
        String email = "https://www.googleapis.com/auth/userinfo.email";

//      String clientId = activity.get().getString(R.string.google_server_client_id);
//      String scope = "oauth2:server:client_id:" + clientId + ":api_scope:" + login + " " + email;
        
        String scope = "oauth2:" + login + " " + email;
        logger.debug("Scopes= " + scope);
        return scope;
    }
}
