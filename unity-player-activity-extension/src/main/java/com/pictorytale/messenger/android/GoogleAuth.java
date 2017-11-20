package com.pictorytale.messenger.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GoogleAuth /*extends FragmentActivity*/ implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    public static String unityGameObjectName = "GoogleTokenMessageReceiver";

    public static final String unitySuccessCallbackName = "OnDone";
    public static final String unityErrorCallbackName = "OnError";

    private static final String TAG = GoogleAuth.class.getSimpleName();

    //private static final String callback = "http://localhost";
    //private static final String visibleActions = "https://www.google.com/m8/feeds/contacts/default/full?max-results=500";
    //private static final String redirectURI = "com.googleusercontent.apps.156834266108-0dsp7r80rns5jrbsvbh3kcvkfij6j1hj:/oauthredirect";
    //private static final String googleAuthorizationEndpoint = "https://accounts.google.com/o/oauth2/v2/auth";
    //private static String webClientID;// = "156834266108-e3blnen2jvrjrqa2kkcu33u9odmd6hpr.apps.googleusercontent.com";
    private static String redirectURI;// = "https://pictorygramtest.firebaseapp.com/__/auth/handler";
    private static String googleTokenEndpoint;// = "https://www.googleapis.com/oauth2/v4/token";
    private static String clientSecret;
    private static String clientID;

    public boolean clientIsSignedIn;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private Scope SCOPE_CONTACTS_MANAGE = new Scope("https://www.google.com/m8/feeds");
    private Scope SCOPE_CONTACTS_READ = new Scope("https://www.googleapis.com/auth/contacts.readonly");
    private Scope SCOPE_EMAIL = new Scope(Scopes.EMAIL);
    private Activity context;
    // [END declare_auth]

    //    @Override
    //    protected void onCreate(Bundle savedInstanceState)
    //    {
    //        super.onCreate(savedInstanceState);
    //    }
    //    @Override
    //    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    //        super.onActivityResult(requestCode, resultCode, data);
    //    }

    public static void setUnityObjectName(String name) {
        unityGameObjectName = name;
    }

    public GoogleAuth(Activity context, String unityGameObjectName, String clientSecretJSONString, String googleTokenEndpoint) {
        setClientParams(unityGameObjectName, clientSecretJSONString, googleTokenEndpoint);
        createGoogleApiClient(context);
        clientIsSignedIn = false;
        this.context = context;
    }

    public void setClientParams(String unityGameObjectName, String clientSecretJSONString, String googleTokenEndpoint)
    {
        GoogleAuth.unityGameObjectName = unityGameObjectName;
        GoogleAuth.googleTokenEndpoint = googleTokenEndpoint;
        try {
            JSONObject jsonObject = new JSONObject(clientSecretJSONString).getJSONObject("web");
            if (jsonObject.has("client_id"))
                GoogleAuth.clientID = jsonObject.getString("client_id");
            if (jsonObject.has("client_secret"))
                GoogleAuth.clientSecret = jsonObject.getString("client_secret");
            if (jsonObject.has("redirect_uris")) {
                JSONArray redirectUrisJSONArray = jsonObject.getJSONArray("redirect_uris");
                GoogleAuth.redirectURI = redirectUrisJSONArray.getString(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createGoogleApiClient(Activity context)
    {
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(SCOPE_CONTACTS_MANAGE, SCOPE_EMAIL)
                .requestServerAuthCode(clientID)
                .requestIdToken(clientID)
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                //.enableAutoManage(context /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
    }

    public void connectWithGoogleAccount(Activity context) {
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    private void signInToGoogleAccount(Activity context) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        context.startActivityForResult(signInIntent, PictoryTaleUnityPlayerActivity.GOOGLE_SIGN_IN_REQUEST_CODE);
    }

    //    private void revokeAccessToGoogleAccount() {
    //        // Check if user is signed in (non-null) and update UI accordingly.
    //        FirebaseUser currentUser = mAuth.getCurrentUser();
    //        // Firebase sign out
    //        if (currentUser != null) {
    //            mAuth.signOut();
    //        }
    //
    //        if (mGoogleApiClient.isConnected()) {
    //        // Google revoke access
    //            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
    //                new ResultCallback<Status>() {
    //                    @Override
    //                    public void onResult(@NonNull Status status) {
    //                        //updateUI(null);
    //                    }
    //                });
    //        }
    //    }

    public void signOutFromGoogleAccount() {
        // Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth != null) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            // Firebase sign out
            if (currentUser != null) {
                mAuth.signOut();
            }
        }
        // Google sign out
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && clientIsSignedIn) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            Log.w(TAG, "signOutFromGoogleAccount.onResult: " + status.getStatusCode() + " " + status.getStatusMessage());
                            clientIsSignedIn = false;
                            mGoogleApiClient.disconnect();
                        }
                    });
        }
    }

    public void getAccessToken(final GoogleSignInAccount acct)
    {
        String idToken = acct.getIdToken();
        String authCode = acct.getServerAuthCode();
        OkHttpClient client = new OkHttpClient();
        Log.w(TAG, "getAccessToken: ");
        Log.w(TAG, "getAccessToken: client_id="+clientID);
        Log.w(TAG, "getAccessToken: client_secret="+clientSecret);
        Log.w(TAG, "getAccessToken: redirect_uri="+ redirectURI);
        Log.w(TAG, "getAccessToken: code="+authCode);
        Log.w(TAG, "getAccessToken: id_token="+idToken);

        RequestBody requestBody = new FormEncodingBuilder()
                .add("grant_type", "authorization_code")
                .add("client_id", clientID)   // something like : ...apps.googleusercontent.com
                .add("client_secret", clientSecret)
                .add("redirect_uri", redirectURI)
                .add("code", authCode) // auth code
                .add("id_token", idToken) // This is what we received in Step 5, the jwt token.
                .build();

        final Request request = new Request.Builder()
                .url(googleTokenEndpoint)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(final Request request, final IOException e) {
                Log.e(TAG, e.toString());
                signOutFromGoogleAccount();
                PictoryTaleUnityPlayerActivity.sendMessageToUnityObject(unityGameObjectName, unityErrorCallbackName, e.toString());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    final String message = jsonObject.toString(5);
                    Log.w(TAG, "getAccessToken.onResponse: " + message);
                    String accessTokenKey = "access_token";
                    if (jsonObject.has(accessTokenKey)) {
                        String accessToken = jsonObject.getString(accessTokenKey);
                        PictoryTaleUnityPlayerActivity.sendMessageToUnityObject(unityGameObjectName, unitySuccessCallbackName, accessToken);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    PictoryTaleUnityPlayerActivity.sendMessageToUnityObject(unityGameObjectName, unityErrorCallbackName, e.getMessage());
                }
            }
        });
    }

    // [START auth_with_google]
    public void firebaseAuthWithGoogle(final Activity context, final GoogleSignInAccount acct) {
        Log.w(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.w(TAG, "signInWithCredential:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            getAccessToken(acct);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                        }
                        // [START_EXCLUDE]
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.w(TAG, "onConnected: " + bundle);
        signInToGoogleAccount(context);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended");
    }
}
