package meupet.android.soasd.com.br.meupet.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import br.com.soasd.meupet.Usuario;
import butterknife.BindView;
import butterknife.ButterKnife;
import meupet.android.soasd.com.br.meupet.R;
import meupet.android.soasd.com.br.meupet.database.LocalDatabase;
import meupet.android.soasd.com.br.meupet.httpservices.GetMethod;
import meupet.android.soasd.com.br.meupet.httpservices.PostMethod;
import meupet.android.soasd.com.br.meupet.httpservices.PrivateGetMethod;


public class Login extends AppCompatActivity  implements GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth mAuth;
    private final int RC_SIGN_IN = 9001;
    private final int START_ACTV = 123;
    GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;
    private GoogleSignInAccount account;
    private int retorno = 0;
    @BindView(R.id.textViewPP)
    TextView textViewPP;







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        new LocalDatabase(this);
        if (isInternetConnectionOn()) {

            if (isGooglePlayServicesAvailable(this)) {

                if (isServerOn()) {

                    startLoginGoogle();
                    callbackManager = CallbackManager.Factory.create();
                    mAuth = FirebaseAuth.getInstance();
                    isLoggedIn();

                    ButterKnife.bind(this);
                    Fresco.initialize(this);
                } else {

                }
            } else {
                setContentView(R.layout.activity_login);
                ButterKnife.bind(this);
                Fresco.initialize(this);
                internetIsOff();
            }
        }



    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                 account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
        if (requestCode == START_ACTV) {
            retorno = resultCode;
        }
    }

    public void loginGoogle(View view){
        if (isInternetConnectionOn()) {
            if (isServerOn()) {

                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        } else {
            internetIsOff();
        }
    }
    public void loginFacebook(View view){
        if (isInternetConnectionOn()) {
            if (isServerOn()) {

                startLoginFacebook();
            }
        }
        else {
            internetIsOff();
        }
    }





    /* START LOGINS */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String metodo = "/SP_BUSCAR_LOGIN?id=" + user.getUid();
                            new GetMethod(metodo, Login.this, new GetMethod.Callback() {
                                @Override
                                public void run(String result) {

                                    Intent i = new Intent(Login.this, MainActivity.class);
                                    i.putExtra("LOGIN", 2);
                                    i.putExtra("FIRST_ACCESS", result);
                                    if (!result.equals("1")) {
                                        addUserGoogleIfNotExist();
                                    }

                                    startActivityForResult(i, 123);
                                }
                            },"Entrando...").execute();

                        }

                    }
                });
    }
    private void startLoginFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile",
                "email", "user_friends"));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                String metodo = "/SP_BUSCAR_LOGIN?id=" + AccessToken.getCurrentAccessToken().getUserId();
                new GetMethod(metodo, Login.this, new GetMethod.Callback() {
                    @Override
                    public void run(String result) {
                        Intent i = new Intent(Login.this, MainActivity.class);
                        i.putExtra("LOGIN", 1);
                        i.putExtra("FIRST_ACCESS", result);
                        if (!result.equals("1")) {
                            addUserFacebookIfNotExist();
                        }

                        startActivityForResult(i, 123);
                    }
            }, "Entrando...").execute();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }
   /* START LOGINS */


    private void startLoginGoogle (){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(Login.this, Login.this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }
    private boolean isInternetConnectionOn() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    private void isLoggedIn() {

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        FirebaseUser currentUser = mAuth.getCurrentUser();


        if (accessToken != null) {
            //LOGGED WITH FACEBOOK
            String metodo = "/SP_BUSCAR_LOGIN?id=" + AccessToken.getCurrentAccessToken().getUserId();
            new GetMethod(metodo, Login.this, new GetMethod.Callback() {
                @Override
                public void run(String result) {

                    Intent i = new Intent(Login.this, MainActivity.class);
                    i.putExtra("LOGIN", 1);
                    i.putExtra("FIRST_ACCESS", result);
                    if (!result.equals("1")) {
                        addUserFacebookIfNotExist();
                    }

                    startActivityForResult(i, 123);
                }
            }, "Entrando...").execute();

        } else if (currentUser != null) {
            //LOGGED WITH GOOGLE
            FirebaseUser user = mAuth.getCurrentUser();
            String metodo = "/SP_BUSCAR_LOGIN?id=" + user.getUid();
            new GetMethod(metodo, Login.this, new GetMethod.Callback() {
                @Override
                public void run(String result) {

                    Intent i = new Intent(Login.this, MainActivity.class);
                    i.putExtra("LOGIN", 2);
                    i.putExtra("FIRST_ACCESS", result);
                    if (!result.equals("1")) {
                        addUserGoogleIfNotExist();
                    }

                    startActivityForResult(i, 123);
                }
            }, "Entrando...").execute();

        }
    }
    private boolean isServerOn() {
        boolean retorno = false;
        try {
            String checkCode = new PrivateGetMethod("", this).execute().get();
            if (checkCode.equals("#$123$#")) {
                retorno = true;
            } else {

                AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                        .setTitle(getResources().getString(R.string.ops))
                        .setMessage(getResources().getString(R.string.server_down))
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                    finish();
                            }
                        });
                dialog.show();

            }
        } catch (InterruptedException e) {
            retorno = false;
        } catch (ExecutionException e) {
            retorno = false;
        }

        return retorno;
    }
    private void addUserFacebookIfNotExist(){
        Bundle bundle = new Bundle();
        bundle.putString("fields", "id, first_name, last_name, birthday, email");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/",
                bundle,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
                            try {
                                JSONObject data = response.getJSONObject();
                                Usuario usuario = new Usuario();
                                String first_name = data.getString("first_name");
                                String last_name = data.getString("last_name");
                                String email = data.getString("email");
                                String id = data.getString("id");

                                usuario.setToken_gcm(FirebaseInstanceId.getInstance().getToken());
                                usuario.setText_nome(first_name);
                                usuario.setText_sobrenome(last_name);
                                usuario.setText_email(email);
                                usuario.setFb_id(id);

                                String postGson = new Gson().toJson(usuario);

                                String metodo = "/SP_REGISTRAR_USUARIO";
                                PostMethod postMethod = new PostMethod(postGson, metodo, Login.this, new PostMethod.Callback() {
                                    @Override
                                    public void run(String result) {


                                    }
                                }, "Cadastrando usuário...");
                                postMethod.execute();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).executeAsync();
    }
    private void addUserGoogleIfNotExist(){
        Usuario usuario = new Usuario();
        FirebaseUser user = mAuth.getCurrentUser();

        usuario.setFb_id(user.getUid());
        usuario.setText_nome(account.getGivenName());
        usuario.setText_sobrenome(account.getFamilyName());
        usuario.setText_email(account.getEmail());
        usuario.setToken_gcm(FirebaseInstanceId.getInstance().getToken());
        String postGson = new Gson().toJson(usuario);

        String metodo = "/SP_REGISTRAR_USUARIO";
        PostMethod postMethod = new PostMethod(postGson, metodo, Login.this, new PostMethod.Callback() {
            @Override
            public void run(String result) {




            }
        }, "Cadastrando usuário...");
        postMethod.execute();



    }
    private void internetIsOff(){

        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.error))
                .setMessage(getResources().getString(R.string.internet_off))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                }).setNegativeButton(R.string.mais_tarde, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            finish();
                    }
                });
        dialog.show();
    }
    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }


    public void viewPP(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://firebasestorage.googleapis.com/v0/b/meupet-d29ed.appspot.com/o/PST%2Fpp%2Fpp.html?alt=media&token=851cb82f-5118-4721-bd50-a7b4ee607c42"));
        startActivity(browserIntent);
    }

}
