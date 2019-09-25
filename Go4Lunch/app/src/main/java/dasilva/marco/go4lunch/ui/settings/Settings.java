package dasilva.marco.go4lunch.ui.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.main.Main;
import dasilva.marco.go4lunch.ui.map.activities.MapView;

public class Settings extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener {

    private EditText editTextRadius;
    private Go4LunchService service = DI.getService();
    private DataBaseService dataBaseService = DI.getDatabaseService();
    private SharedPreferences sharedPreferences;
    private ImageView userAvatar;
    private TextView userName;
    private TextView restaurantName;
    private TextView restaurantChoice;
    private ImageView restaurantPhoto;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.setting_tittle));

        sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        findViews();

        Glide.with(this).load(service.getUser().getImageUrl()).apply(RequestOptions.circleCropTransform()).into(userAvatar);

        setUserChoiceIntoAndInfoIntoView();
        editTextRadius.setOnClickListener(this);

    }

    private void findViews(){
        userAvatar = findViewById(R.id.user_avatar_settings);
        userName = findViewById(R.id.user_name_settings);
        restaurantName = findViewById(R.id.restaurant_name);
        restaurantChoice = findViewById(R.id.restaurant_choice);
        restaurantPhoto = findViewById(R.id.restaurant_photo);
        editTextRadius = findViewById(R.id.radius_editText);
        editTextRadius.setOnEditorActionListener(this);
        Button deleteLunchBtn = findViewById(R.id.delete_choosed_lunch);
        Button deleteAccountBtn = findViewById(R.id.delete_user_button);
        Button confirmChanges = findViewById(R.id.confirm_button);
        deleteLunchBtn.setOnClickListener(this);
        deleteAccountBtn.setOnClickListener(this);
        confirmChanges.setOnClickListener(this);
        editTextRadius.setOnClickListener(this);

    }

    private void setUserChoiceIntoAndInfoIntoView(){
        userName.setText(service.getUser().getUserName());

        if (service.getUser().getChoice() != null){
            restaurantChoice.setText(getString(R.string.lunch_text_settings));
            restaurantName.setText(service.getUser().getChoice());
            for (int i = 0; i < dataBaseService.getListOfSelectedPlaces().size(); i++){
                if (dataBaseService.getListOfSelectedPlaces().get(i).getUserId().contains(service.getUser().getId())){
                    for (int j = 0; j < service.getListMarkers().size(); j++){
                        if (service.getListMarkers().get(j).getId().equals(dataBaseService.getListOfSelectedPlaces().get(i).getId())){
                            Glide.with(this).load(service.getListMarkers().get(j).getPhotoUrl())
                                    .apply(RequestOptions.circleCropTransform()).into(restaurantPhoto);
                        }
                    }
                }
            }
        } else {
            restaurantChoice.setText(getString(R.string.lunch_text_nothing_selected));
        }

        if (service.getUser().getRadius() != null){
            String radiusText = getString(R.string.search_zone_hint)+ service.getUser().getRadius();
            editTextRadius.setText(radiusText);
        } else {
            editTextRadius.setHint(R.string.radius_search);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.delete_choosed_lunch:
                confirmDialog(getString(R.string.lunch_string));
                break;
            case R.id.delete_user_button:
                confirmDialog(getString(R.string.user_string));
                break;
            case R.id.radius_editText:
                editTextRadius.setText("");
                editTextRadius.setHint(R.string.radius_search);
                break;
            case R.id.confirm_button:
                Intent mapViewIntent = new Intent(this, MapView.class);
                startActivity(mapViewIntent);
                break;
        }
    }

    private void confirmDialog(final String deleteString){
        final AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
        confirmDialog.setTitle(R.string.confirm_delete);
        confirmDialog.setPositiveButton(R.string.sure_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deleteString.equals(getString(R.string.lunch_string))){
                    dataBaseService.removeCompleteSelectionDatabase();
                    sharedPreferences.edit().remove(getString(R.string.choice)).apply();
                    sharedPreferences.edit().remove(getString(R.string.choice_adress)).apply();
                    sharedPreferences.edit().remove(getString(R.string.joining_users)).apply();
                    dataBaseService.setListOfSelectedPlaces();
                    restaurantChoice.setText(getString(R.string.lunch_text_nothing_selected));
                    restaurantName.setText("");
                    restaurantPhoto.setImageResource(0);
                } else {
                    if (deleteString.equals(getString(R.string.user_string))) {
                        dataBaseService.deleteUserFromFireBase();
                        LoginManager.getInstance().logOut();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(Settings.this, Main.class);
                        startActivity(intent);
                    }
                }
                dialog.dismiss();
            }
        });
        confirmDialog.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        confirmDialog.show();
    }

    private void getEditTextContentText(){
        if (editTextRadius.getText().toString().contains(getString(R.string.search_zone_hint))) {
            String[] radius = editTextRadius.getText().toString().split(":");
            service.getUser().setRadius(radius[1]);
            service.setUserRadius(service.getUser().getRadius());
        } else {
            service.getUser().setRadius(editTextRadius.getText().toString());
            service.setUserRadius(service.getUser().getRadius());
            service.setListMarkers(null);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            getEditTextContentText();
            return true;
        }
        return true;
    }
}
