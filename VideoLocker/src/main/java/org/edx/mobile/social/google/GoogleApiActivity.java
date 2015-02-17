package org.edx.mobile.social.google;

import java.io.IOException;
import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;

import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;

public class GoogleApiActivity extends BaseFragmentActivity implements ConnectionCallbacks,
        OnConnectionFailedListener, View.OnClickListener {

    /*
     * Track whether the sign-in button has been clicked so that we know to
     * resolve all issues preventing sign-in without waiting.
     */
    private boolean mSignInClicked;

    /*
     * Store the connection result from onConnectionFailed callbacks so that we
     * can resolve them when the user clicks sign-in.
     */
    private ConnectionResult mConnectionResult;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;

    /*
     * A flag indicating that a PendingIntent is in progress and prevents us
     * from starting further intents.
     */
    private boolean mIntentInProgress;

    private Runnable getTokenRunnable = new Runnable() {
        
        @Override
        public void run() {
            try {
                String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
                String accessToken = GoogleAuthUtil.getToken(GoogleApiActivity.this,
                        accountName,
                        "oauth2:" + Scopes.PLUS_LOGIN);
                logger.debug("Google accessToken: " + accessToken);
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Signed in to Google", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (UserRecoverableAuthException e) {
                logger.error(e);
            } catch (IOException e) {
                logger.error(e);
            } catch (GoogleAuthException e) {
                logger.error(e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // test layout file
        // setContentView(R.layout.activity_google);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
        
        initUI();
    }

    private void initUI() {
        Button signIn = (Button) findViewById(R.id.sign_in_button);
        Button signOut = (Button) findViewById(R.id.sign_out_button);

        if (signIn != null)
            signIn.setOnClickListener(this);
        if (signOut != null)
            signOut.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress && result.hasResolution()) {
            // Store the ConnectionResult so that we can use it later when the
            // user clicks
            // 'sign-in'.
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to
                // resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }

            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution()
                        .getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                // The intent was canceled before it was sent. Return to the
                // default
                // state and attempt to connect to get an updated
                // ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
                logger.error(e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode,
            Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors. mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.
        mSignInClicked = false;
        Toast.makeText(this, "User is connected!", Toast.LENGTH_LONG).show();
        new Thread(getTokenRunnable ).start();
    }
    
    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mConnectionResult != null && mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution()
                        .getIntentSender(), RC_SIGN_IN, null, 0, 0, 0);
            } catch (SendIntentException e) {
                // The intent was canceled before it was sent. Return to the
                // default
                // state and attempt to connect to get an updated
                // ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        } else {
            mGoogleApiClient.connect();
        }
    }
    
    protected void signInGoogle() {
        if (!mGoogleApiClient.isConnecting()) {
            mSignInClicked = true;
            resolveSignInError();
        }

        if (mGoogleApiClient.isConnected()) {
            Toast.makeText(getApplicationContext(), "Already Signed in to Google", Toast.LENGTH_LONG).show();
        }
    }

    protected void signOutGoogle() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            logger.debug("Explicitly disconnected");
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_button) {
            signInGoogle();
        } else if (view.getId() == R.id.sign_out_button) {
            signOutGoogle();
        }
    }
}