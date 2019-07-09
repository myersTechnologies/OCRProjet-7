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
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;


public class DetailsActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    TextView restaurantInfo, restaurantAdress;
    ImageView restaurantImage;
    Go4LunchService service;
    BottomNavigationView navigationView;
    FloatingActionButton fab;
    RecyclerView detailsUsersRecyclerView;
    DetailsRecyclerViewAdapter adapter;
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        service = DI.getService();

        restaurantInfo = (TextView) findViewById(R.id.restaurant_details_info);
        restaurantAdress = (TextView)  findViewById(R.id.restaurant_details_adress);
        restaurantImage = (ImageView) findViewById(R.id.restaurant_image);
        navigationView =  (BottomNavigationView) findViewById(R.id.nav_options_details);
        detailsUsersRecyclerView = (RecyclerView) findViewById(R.id.joinin_users_list);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        detailsUsersRecyclerView.setLayoutManager(mLayoutManager);
        fab = (FloatingActionButton) findViewById(R.id.user_choice);
        navigationView.setOnNavigationItemSelectedListener(this);
        fab.setOnClickListener(this);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        restaurantInfo.setText(service.getPlaceMarker().getName());
        restaurantAdress.setText(service.getPlaceMarker().getAdress());
        Glide.with(this).load(service.getPlaceMarker().getPhotoUrl()).apply(RequestOptions.noTransformation()).into(restaurantImage);

        try {
            adapter = new DetailsRecyclerViewAdapter(service.getUsersList(), service.getListOfSelectedPlaces());
            detailsUsersRecyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (Exception e){

        }

    }

    public void setUserChoice(){
        SelectedPlace selectedPlace = new SelectedPlace(service.getUser().getId(), service.getPlaceMarker().getName(), service.getPlaceMarker().getLatLng().toString(),
                service.getUser().getId(), service.getPlaceMarker().getId());
        service.getUser().setChoice(service.getPlaceMarker().getName());
        databaseReference.child("selection").child(service.getUser().getId()).setValue(selectedPlace);
        databaseReference.child("users").child(service.getUser().getId()).child("choice").setValue(service.getPlaceMarker().getName());
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

                break;
        }
        return true;
    }

    public void openWebSite(){
        String url = service.getPlaceMarker().getWebSite();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage("com.android.chrome");
        startActivity(intent);
    }

    public void callRestaurant(){
        String phoneNumber = service.getPlaceMarker().getTelephone().trim();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.user_choice:
                if (service.getUser().getChoice() == null){
                    setUserChoice();
                } else {
                    Toast.makeText(this, "Restaurant déja choisi!", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }


}
