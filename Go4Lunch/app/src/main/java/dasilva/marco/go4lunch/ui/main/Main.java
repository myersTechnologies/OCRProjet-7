package dasilva.marco.go4lunch.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.notification.NotificationService;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.activities.MapView;


public class Main extends AppCompatActivity {

    private Intent mapViewActivity;
    private CallbackManager callbackManager;
    private LoginButton signInFb;
    private FirebaseAuth mAuth;
    private Go4LunchService service;
    private SignInButton googleSignIn;
    private DataBaseService dataBaseService;
    private static String  EMAIL = "email";
    private static String PUBLIC_PROFILE =  "public_profile";
    private static int REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent notificationService = new Intent(this, NotificationService.class);
        stopService(notificationService);

        service = DI.getService();
        dataBaseService = DI.getDatabaseService();

        googleSignIn = findViewById(R.id.signInGoogle);
        signInFb = findViewById(R.id.fb_login_button);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());

        mapViewActivity = new Intent(this, MapView.class);

        mAuth = FirebaseAuth.getInstance();

        requestPermissions();
        signInFb.setReadPermissions(EMAIL, PUBLIC_PROFILE);
        callbackManager = CallbackManager.Factory.create();

        service.setDataBase(dataBaseService);
        dataBaseService.setUsersList();
        dataBaseService.setListOfSelectedPlaces();
        connectUser();

    }

    @Override
    public void onRestart(){
        super.onRestart();
        checkIfUserIsConnected();
    }

    public void connectUser(){
        LoginManager.getInstance().logOut();
        FirebaseAuth.getInstance().signOut();
        signInWithFacebook();
        signInWithGoogle();
    }

    //if user id connected get to map activity directly
    public void checkIfUserIsConnected(){
        if (service.getUser() != null) {
            startMapActivity();
            startActivity(mapViewActivity);
        } else {
            LoginManager.getInstance().logOut();
            FirebaseAuth.getInstance().signOut();
            signInWithFacebook();
            signInWithGoogle();
        }
    }

    //Login with facebook
    public void signInWithFacebook(){
        signInFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInFb.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    //check if facebook auth is successful and pass parameters to set user onAuthSuccess method
    private void handleFacebookToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            try {
                                FirebaseUser fbUser = task.getResult().getUser();
                                onAuthSuccess(fbUser);
                                startActivity(mapViewActivity);
                            } catch (NullPointerException e){
                                Toast.makeText(getApplicationContext(), R.string.create_user_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.authentication_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    //set google sign in client, activity for result to get users email and name
    public void signInWithGoogle(){
        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, REQUEST_CODE);
            }
        });
    }

    public void requestPermissions(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                        Manifest.permission.INTERNET}, 2);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //auth with google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser fbUser = task.getResult().getUser();
                            onAuthSuccess(fbUser);
                            startActivity(mapViewActivity);
                        }
                    }
                });
    }

    //on auth success set user's parameters
    public void onAuthSuccess(FirebaseUser user){
        String userName = user.getDisplayName();
        String email = user.getEmail();
        Uri photoUrl = user.getPhotoUrl();
        addNewUser(user.getUid(), userName, email, String.valueOf(photoUrl));

    }

    //add new user to real time data base
    public void addNewUser(String userId, String name, String email, String photoUri){
        User user = new User(userId, name, email, photoUri);
        service.setUser(user);
        dataBaseService.getAdditionalUserData(userId);
    }

    public void startMapActivity(){
        FirebaseUser databaseUser = mAuth.getCurrentUser();
        String userName = null;
        if (databaseUser != null) {
            userName = databaseUser.getDisplayName();
        }
        String email = null;
        if (databaseUser != null) {
            email = databaseUser.getEmail();
        }
        Uri photoUrl = null;
        if (databaseUser != null) {
            photoUrl = databaseUser.getPhotoUrl();
        }
        User user = null;
        if (databaseUser != null) {
            if (userName != null) {
                if (email != null) {
                    user = new User(databaseUser.getUid(), userName, email, String.valueOf(photoUrl));
                }
            }
        }
        service.setUser(user);
        if (databaseUser != null) {
            dataBaseService.getAdditionalUserData(user.getId());
        }
    }

}
