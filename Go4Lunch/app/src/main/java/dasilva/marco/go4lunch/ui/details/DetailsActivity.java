package dasilva.marco.go4lunch.ui.details;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import dasilva.marco.go4lunch.model.SelectedPlace;

import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;


public class DetailsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private TextView restaurantInfo, restaurantAdress;
    private ImageView restaurantImage;
    private Go4LunchService service;
    private RecyclerView detailsUsersRecyclerView;
    private DatabaseReference databaseReference;
    private RatingBar placeRate;
    private List<User> userList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        service = DI.getService();

        setViews();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        restaurantInfo.setText(service.getPlaceMarker().getName());
        restaurantAdress.setText(service.getPlaceMarker().getAdress());
        Glide.with(this).load(service.getPlaceMarker().getPhotoUrl()).apply(RequestOptions.noTransformation()).into(restaurantImage);
        placeRate.setRating(service.getPlaceMarker().getLikes());

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
        FloatingActionButton fab = findViewById(R.id.user_choice);
        navigationView.setOnNavigationItemSelectedListener(this);
        fab.setOnClickListener(this);
    }

    public void initList(){
        userList = new ArrayList<>();
        for(SelectedPlace place : service.getListOfSelectedPlaces()){
            for (User user : service.getUsersList()){
                    if (place.getId().equals(service.getPlaceMarker().getId())){
                        if (place.getUserId().contains(user.getId())){
                            userList.add(user);
                        }
                    }
            }
        }
        if (service.getUsersList().size() > 0) {
            DetailsRecyclerViewAdapter adapter = new DetailsRecyclerViewAdapter(userList);
            detailsUsersRecyclerView.setAdapter(adapter);
        }
    }

    public void setUserChoice(){
        if (service.getListOfSelectedPlaces().size() > 0) {
            for (SelectedPlace place : service.getListOfSelectedPlaces()) {
                if (place.getId().equals(service.getPlaceMarker().getId())) {
                    place.setUserId(place.getUserId() + "," + service.getUser().getId());
                    databaseReference.child(getString(R.string.selection)).child(service.getPlaceMarker().getId()).setValue(place);
                } else {
                    SelectedPlace selectedPlace = new SelectedPlace(service.getPlaceMarker().getId(), service.getPlaceMarker().getName(),
                            String.valueOf(service.getPlaceMarker().getLatLng()), service.getUser().getId());

                    databaseReference.child(getString(R.string.selection)).child(service.getPlaceMarker().getId()).setValue(selectedPlace);
                }
            }
        } else {
            SelectedPlace selectedPlace = new SelectedPlace(service.getPlaceMarker().getId(), service.getPlaceMarker().getName(),
                    String.valueOf(service.getPlaceMarker().getLatLng()), service.getUser().getId());

            databaseReference.child(getString(R.string.selection)).child(service.getPlaceMarker().getId()).setValue(selectedPlace);
        }
        service.getUser().setChoice(service.getPlaceMarker().getName());
        databaseReference.child(getString(R.string.users)).child(service.getUser().getId()).child(getString(R.string.choice)).setValue(service.getPlaceMarker().getName());
        initList();
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
                service.getPlaceMarker().setLikes();
                if (service.getUser().getLikedPlacesId() != null){

                    if (!service.getUser().getLikedPlacesId().contains(service.getPlaceMarker().getId())) {
                        service.getUser().setLikedPlacesId(service.getUser().getLikedPlacesId() + "," + service.getPlaceMarker().getId());
                        service.setUserLikedPlaces(service.getUser().getLikedPlacesId() + "," + service.getPlaceMarker().getId());
                    } else {
                            Toast.makeText(this, R.string.already_liked, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    service.getUser().setLikedPlacesId(service.getPlaceMarker().getId());
                    service.setUserLikedPlaces(service.getPlaceMarker().getId());
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
        String phoneNumber = service.getPlaceMarker().getTelephone().trim();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(getString(R.string.telephone_util) + phoneNumber));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_choice:
                if (service.getUser().getChoice() == null){
                    setUserChoice();
                } else {
                    Toast.makeText(this, R.string.already_choosed, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


}
