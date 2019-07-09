package dasilva.marco.go4lunch.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.MapView;
import dasilva.marco.notification.NotificationService;

public class Main extends AppCompatActivity {

    private Intent mapViewActivity;
    private CallbackManager callbackManager;
    private LoginButton signInFb;
    private FirebaseAuth mAuth;
    private Go4LunchService service;
    private SignInButton googleSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        service = DI.getService();
        service.setUsersList();
        service.setListOfSelectedPlaces();

        googleSignIn = (SignInButton) findViewById(R.id.signInGoogle);
        signInFb = (LoginButton) findViewById(R.id.fb_login_button);

        mapViewActivity = new Intent(this, MapView.class);

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        mAuth = FirebaseAuth.getInstance();

        requestPermissions();
        callbackManager = CallbackManager.Factory.create();
        signInFb.setReadPermissions("email");

        checkIfUserIsConnected();

        startAlarmToSendANotification();

    }

    private void startAlarmToSendANotification() {
        Intent notificationService = new Intent(this, NotificationService.class);
        startService(notificationService);
    }

    @Override
    public void onRestart(){
        super.onRestart();
        checkIfUserIsConnected();
    }

    //if user id connected get to map activity directly
    public void checkIfUserIsConnected(){
        if (mAuth.getCurrentUser() != null){
            onAuthSuccess(mAuth.getCurrentUser());
            startActivity(mapViewActivity);
        } else {
            LoginManager.getInstance().logOut();
            signInWithFacebook();
            signInWithGoogle();
        }

    }

    //Login with facebook
    public void signInWithFacebook(){
        signInFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(LoginResult loginResult) {
                                handleFacebookToken(loginResult.getAccessToken());
                            }

                            @Override
                            public void onCancel() {
                                // App code
                            }

                            @Override
                            public void onError(FacebookException exception) {
                                Toast.makeText(getApplicationContext(), exception.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
    //check if facebook authentification is successful and pass parameters to set user onAuthSuccess method
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
                                Log.d("Failed", "Create userFailed");
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
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
                startActivityForResult(signInIntent, 5);
            }
        });
    }

    public void requestPermissions(){
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                1);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    15);
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
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 5) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("Failed", "Google sign in failed", e);
                // ...
            }
        }
    }

    //Firebase authentification with google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Success", "signInWithCredential:success");
                            FirebaseUser fbUser = task.getResult().getUser();
                            onAuthSuccess(fbUser);
                            startActivity(mapViewActivity);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Failed", "signInWithCredential:failure", task.getException());
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

    //add new user to firebase real time data base
    public void addNewUser(final String userId, String name, String email, String photoUri){
        final User user = new User(userId, name, email, photoUri);
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseRef = firebaseDatabase.getReference("users");
        Query applesQuery = databaseRef.child(userId);
        applesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    if (dataSnapshot.child("id").getValue().toString().equals(userId)) {
                        try {
                            String choice = dataSnapshot.child("choice").getValue().toString();
                            String radius = dataSnapshot.child("radius").getValue().toString();
                            user.setChoice(choice);
                            user.setRadius(radius);
                            databaseRef.child(userId).child("choice").setValue(choice);
                            databaseRef.child(userId).child("radius").setValue(radius);
                        } catch (NullPointerException e) {

                        }
                    }
                }else{
                    databaseRef.child(user.getId()).setValue(user);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
        service.setUser(user);
    }

}
