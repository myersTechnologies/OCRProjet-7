package dasilva.marco.go4lunch.ui.details;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.SelectedPlace;

import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.notification.NotificationService;
import dasilva.marco.go4lunch.service.Go4LunchService;


public class DetailsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private TextView restaurantInfo, restaurantAdress;
    private ImageView restaurantImage;
    private Go4LunchService service;
    private RecyclerView detailsUsersRecyclerView;
    private DatabaseReference databaseReference;
    private RatingBar placeRate;
    private List<User> userList;
    private FloatingActionButton fab;
    private DataBaseService dataBaseService;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        service = DI.getService();
        dataBaseService = DI.getDatabaseService();
        sharedPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        setViews();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        restaurantInfo.setText(service.getPlaceMarker().getName());
        restaurantAdress.setText(service.getPlaceMarker().getAdress());
        Glide.with(this).load(service.getPlaceMarker().getPhotoUrl()).apply(RequestOptions.noTransformation()).into(restaurantImage);
        placeRate.setRating(service.getPlaceMarker().getLikes() / 2);
        initList();


    }

    public void setViews(){
        placeRate = findViewById(R.id.details_rating_bar);
        restaurantInfo = findViewById(R.id.restaurant_details_info);
        restaurantAdress =  findViewById(R.id.restaurant_details_adress);
        restaurantImage = findViewById(R.id.restaurant_image);
        BottomNavigationView navigationView = findViewById(R.id.nav_options_details);
        detailsUsersRecyclerView = findViewById(R.id.joinin_users_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        detailsUsersRecyclerView.setLayoutManager(mLayoutManager);
        fab = findViewById(R.id.user_choice);
        navigationView.setOnNavigationItemSelectedListener(this);
        fab.setOnClickListener(this);
        if (service.getUser().getChoice() != null){
            fab.setImageResource(R.drawable.ic_check);
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
        } else{
            fab.setImageResource(R.drawable.ic_uncheck);
            fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        }
    }

    public void initList(){
        userList = new ArrayList<>();
        for(SelectedPlace place : dataBaseService.getListOfSelectedPlaces()){
            for (User user : dataBaseService.getUsersList()){
                    if (place.getId().equals(service.getPlaceMarker().getId())){
                        if (place.getUserId().contains(user.getId())){
                            userList.add(user);
                        }
                    }
            }
        }
        if (userList != null) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getId().equals(service.getUser().getId())) {
                    userList.remove(userList.get(i));
                }
            }
            if (dataBaseService.getUsersList().size() > 0) {
                DetailsRecyclerViewAdapter adapter = new DetailsRecyclerViewAdapter(userList);
                detailsUsersRecyclerView.setAdapter(adapter);
            }
        }
    }

    public void setUserChoice(){
        if (dataBaseService.getListOfSelectedPlaces().size() > 0) {
            for (SelectedPlace place : dataBaseService.getListOfSelectedPlaces()) {
                if (place.getId().equals(service.getPlaceMarker().getId())) {
                    place.setUserId(service.getUser().getId());
                    databaseReference.child(getString(R.string.selection)).child(service.getPlaceMarker().getId()).setValue(place);
                } else {
                    SelectedPlace selectedPlace = new SelectedPlace(service.getPlaceMarker().getId(), service.getPlaceMarker().getName(),
                            String.valueOf(service.getPlaceMarker().getLatLng()));
                    selectedPlace.setUserId(service.getUser().getId());
                    databaseReference.child(getString(R.string.selection)).child(service.getPlaceMarker().getId()).setValue(selectedPlace);
                }
            }
        } else {
            SelectedPlace selectedPlace = new SelectedPlace(service.getPlaceMarker().getId(), service.getPlaceMarker().getName(),
                    String.valueOf(service.getPlaceMarker().getLatLng()));
            selectedPlace.setUserId(service.getUser().getId());
            databaseReference.child(getString(R.string.selection)).child(service.getPlaceMarker().getId()).setValue(selectedPlace);
        }
        service.getUser().setChoice(service.getPlaceMarker().getName());

        sharedPreferences.edit().putString(getString(R.string.choice_adress), service.getPlaceMarker().getAdress()).apply();

        sharedPreferences.edit().putString(getString(R.string.joining_users), getJoiningUsers()).apply();
        databaseReference.child(getString(R.string.users)).child(service.getUser().getId()).child(getString(R.string.choice)).setValue(service.getPlaceMarker().getName());
        service.countPlaceSelectedByUsers();
        startAlarmToSendANotification();
        initList();
    }

    public String getJoiningUsers(){
        List<String> usersFistName = new ArrayList<>();
        if (userList.size() > 0) {
            for (User user : userList) {
                usersFistName.add(user.getFirtName());
            }
        } else {
            usersFistName.add(getString(R.string.nobody_joining));
        }
        String joiningUsers = TextUtils.join(", ", usersFistName);
        return joiningUsers;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.website_item:
                if (service.getPlaceMarker().getWebSite() != null){
                    openWebSite();
                }
                break;
            case R.id.details_call_item:
                if (service.getPlaceMarker().getTelephone() != null){
                    callRestaurant();
                }
                break;
            case R.id.details_like_item:
                if (service.getUser().getLikedPlacesId() != null){
                    if (!service.getUser().getLikedPlacesId().contains(service.getPlaceMarker().getId())) {
                        service.getUser().setLikedPlacesId(service.getPlaceMarker().getId());
                        service.setUserLikedPlaces(service.getUser().getLikedPlacesId());
                        int likes = service.getPlaceMarker().getLikes();
                        service.getPlaceMarker().setLikes(likes++);
                        service.countPlacesLikes();

                        } else {
                            Toast.makeText(this, R.string.already_liked, Toast.LENGTH_SHORT).show();
                        }

                } else {
                    service.getUser().setLikedPlacesId(service.getPlaceMarker().getId());
                    service.setUserLikedPlaces(service.getUser().getLikedPlacesId());
                    int likes = service.getPlaceMarker().getLikes();
                    service.getPlaceMarker().setLikes(likes++);
                    service.countPlacesLikes();
                }
                break;
        }
        return true;
    }

    public void openWebSite(){
        String url = service.getPlaceMarker().getWebSite();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage(getString(R.string.web_package));
        startActivity(intent);
    }

    public void callRestaurant(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    15);
        } else {
            String phoneNumber = service.getPlaceMarker().getTelephone().trim();
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse(getString(R.string.telephone_util) + phoneNumber));
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_choice:
                if (!service.getTodayOpenHour(service.getPlaceMarker()).equals("Closed today")) {
                    if (service.getUser().getChoice() == null) {
                        setUserChoice();
                        fab.setImageResource(R.drawable.ic_check);
                        fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                    } else {
                        Toast.makeText(this, R.string.already_choosed, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.closed_today_text_toast), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void startAlarmToSendANotification() {
        Intent notificationService = new Intent(this, NotificationService.class);
        sharedPreferences.edit().putString(getString(R.string.choice), service.getUser().getChoice()).apply();
        startService(notificationService);
    }

}
