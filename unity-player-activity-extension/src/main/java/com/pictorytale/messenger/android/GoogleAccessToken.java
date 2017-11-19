package com.pictorytale.messenger.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GoogleAccessToken implements GoogleApiClient.OnConnectionFailedListener {


    public static String unityGameObjectName = "GoogleTokenMessageReceiver";
    public static final String unitySuccessCallbackName = "OnDone";
    public static final String unityErrorCallbackName = "OnError";

    private static final String TAG = GoogleAccessToken.class.getSimpleName();

    private static final String webClientID = "156834266108-e3blnen2jvrjrqa2kkcu33u9odmd6hpr.apps.googleusercontent.com";
    private static final String androidClientID = "156834266108-4cb9jor6c7pid42v376nqcc3oq64g3he.apps.googleusercontent.com";
    private static final String callback = "http://localhost";
    private static final String visibleActions = "https://www.google.com/m8/feeds/contacts/default/full?max-results=500";
    private static final String kRedirectURI = "com.googleusercontent.apps.156834266108-0dsp7r80rns5jrbsvbh3kcvkfij6j1hj:/oauthredirect";
    private static final String googleAuthorizationEndpoint = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String googleTokenEndpoint = "https://www.googleapis.com/oauth2/v4/token";

    // [START declare_auth]
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private Scope SCOPE_CONTACTS_MANAGE = new Scope("https://www.google.com/m8/feeds");
    private Scope SCOPE_CONTACTS_READ = new Scope("https://www.googleapis.com/auth/contacts.readonly");
    private Scope SCOPE_EMAIL = new Scope(Scopes.EMAIL);
    private String clientSecret;
    // [END declare_auth]

    public static void setUnityObjectName(String name) {
        unityGameObjectName = name;
    }

    public GoogleAccessToken(String unityGameObjectName, String clientSecret) {
        GoogleAccessToken.unityGameObjectName = unityGameObjectName;
        this.clientSecret = clientSecret;
    }

    public void signInToGoogleAccount(Activity context)
    {
        Log.e(TAG, "signInToGoogleAccount: defaultWebClientId=" + webClientID);
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(SCOPE_CONTACTS_MANAGE, SCOPE_CONTACTS_READ, SCOPE_EMAIL)
                .requestServerAuthCode(webClientID)
                .requestIdToken(webClientID)
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                //.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        context.startActivityForResult(signInIntent, PictoryTaleUnityPlayerActivity.GOOGLE_SIGN_IN_REQUEST_CODE);
    }

    private void signOutFromGoogleAccount() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        Log.e(TAG, "signOutFromGoogleAccount.onResult: "+status.getStatusCode()+" "+status.getStatusMessage());
                    }
                });
    }

    public void getAccessToken(final Context context, final GoogleSignInAccount acct)
    {
        String idToken = acct.getIdToken();
        String authCode = acct.getServerAuthCode();
        OkHttpClient client = new OkHttpClient();
        Log.e(TAG, "getAccessToken: ");
        Log.e(TAG, "getAccessToken: client_id="+androidClientID);
        Log.e(TAG, "getAccessToken: client_secret="+clientSecret);
        Log.e(TAG, "getAccessToken: redirect_uri="+kRedirectURI);
        Log.e(TAG, "getAccessToken: code="+authCode);
        Log.e(TAG, "getAccessToken: id_token="+idToken);

        //        Runnable runnable = new Runnable() {
        //            @Override
        //            public void run() {
        //                try {
        //                    //String scope = "https://www.google.com/m8/feeds";
        //                    String scope = "oauth2:"+"https://www.google.com/m8/feeds";
        //                    Log.d(TAG, "GoogleAuthUtil scope =" + scope );
        //                    String accessToken = GoogleAuthUtil.getToken(context, acct.getAccount(), scope, new Bundle());
        //                    Log.d(TAG, "GoogleAuthUtil.getToken=" + accessToken); //accessToken:ya29.Gl...
        //                } catch (IOException e) {
        //                    e.printStackTrace();
        //                } catch (GoogleAuthException e) {
        //                    e.printStackTrace();
        //                }
        //            }
        //        };
        //        Thread t = new Thread(runnable);
        //        t.start();
        //        //AsyncTask.execute(runnable);
        //        if (true)
        //            return;

        RequestBody requestBody = new FormEncodingBuilder()
                .add("grant_type", "authorization_code")
                .add("client_id", androidClientID)   // something like : ...apps.googleusercontent.com
                .add("client_secret", clientSecret)
                .add("redirect_uri", kRedirectURI)
                .add("code", authCode) // device code.
                //.add("auth_code", authCode)
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
                PictoryTaleUnityPlayerActivity.sendMessageToUnityObject(unityGameObjectName, unityErrorCallbackName, e.toString());
                PictoryTaleUnityPlayerActivity.instance.releaseGoogleAccessToken();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    final String message = jsonObject.toString(5);
                    //String accessToken = jsonObject.getString("access_token");
                    Log.e(TAG, "getAccessToken.onResponse: " + message);
                    //Log.e(TAG, "getAccessToken.onResponse: accessToken=" + accessToken);
                    //PictoryTaleUnityPlayerActivity.sendMessageToUnityObject(unityGameObjectName, unitySuccessCallbackName, accessToken);
                } catch (JSONException e) {
                    e.printStackTrace();
                    PictoryTaleUnityPlayerActivity.sendMessageToUnityObject(unityGameObjectName, unityErrorCallbackName, e.getMessage());
                }
                PictoryTaleUnityPlayerActivity.instance.releaseGoogleAccessToken();
            }
        });
    }

    // [START auth_with_google]
    public void firebaseAuthWithGoogle(final Activity context, final GoogleSignInAccount acct) {
        Log.e(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.e(TAG, "signInWithCredential:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            getAccessToken(context.getApplicationContext(), acct);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.e(TAG, "signInWithCredential:failure", task.getException());
                        }
                        // [START_EXCLUDE]
                        //hideProgressDialog();
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
}
