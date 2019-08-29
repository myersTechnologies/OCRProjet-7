package dasilva.marco.go4lunch.ui.map.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;

import dasilva.marco.go4lunch.BuildConfig;
import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.ui.chat.ChatActivity;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.notification.NotificationService;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.main.Main;
import dasilva.marco.go4lunch.ui.details.DetailsActivity;
import dasilva.marco.go4lunch.ui.map.fragments.ListViewFragment;
import dasilva.marco.go4lunch.ui.map.fragments.MapFragment;
import dasilva.marco.go4lunch.ui.map.fragments.WorkmatesFragment;
import dasilva.marco.go4lunch.ui.map.utils.details.PlaceDetailsTask;
import dasilva.marco.go4lunch.ui.settings.Settings;


public class MapView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnNavigationItemSelectedListener{

    private Go4LunchService service;
    private TextView userNameText, userEmailText;
    private ImageView userImage;
    private Toolbar searchToolbar;
    private Toolbar workMatesToolbar;
    private DataBaseService dataBaseService;
    private SharedPreferences preferences;
    private BottomNavigationView mapsBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        searchToolbar = findViewById(R.id.toolbar_search);
        workMatesToolbar = findViewById(R.id.toolbar_search_user);
        setSupportActionBar(toolbar);

        service = DI.getService();
        dataBaseService = DI.getDatabaseService();
        preferences = this.getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        changeFragment(new MapFragment(), R.string.map_fragment);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userEmailText = navigationView.getHeaderView(0).findViewById(R.id.userEmail);
        userNameText = navigationView.getHeaderView(0).findViewById(R.id.userName);
        userImage = navigationView.getHeaderView(0).findViewById(R.id.userAvatar);

        mapsBottomNav =  findViewById(R.id.nav_bottom_maps);

        mapsBottomNav.setOnNavigationItemSelectedListener(this);

        initView();

        if (service.getUser().getChoice() != null) {
            startAlarmToSendANotification(service.getUser().getChoice());
        } else {
            checkChoiceStringToRemoveSelectedPlace();
        }

    }
    public void checkChoiceStringToRemoveSelectedPlace() {
        preferences.edit().remove(getString(R.string.choice)).apply();
        preferences.edit().remove(getString(R.string.choice_adress)).apply();
        preferences.edit().remove(getString(R.string.joining_users)).apply();
    }

    private void startAlarmToSendANotification(String choice) {
        Intent notificationService = new Intent(this, NotificationService.class);
        preferences.edit().putString(getString(R.string.choice), choice).apply();
        startService(notificationService);
    }


    //initializes places client
    public void initView() {
        //views for navigation drawer textViews and set user info inside them
        userNameText.setText(service.getUser().getUserName());
        userEmailText.setText(service.getUser().getUserEmail());

        if (service.getUser().getImageUrl().contains(String.valueOf(R.string.google_string))) {
            Glide.with(this).load(service.getUser().getImageUrl()).apply(RequestOptions.circleCropTransform()).into(userImage);
        } else {
            Glide.with(this).load(service.getUser().getImageUrl() + "?" + getString(R.string.photo_type_fb))
                    .apply(RequestOptions.circleCropTransform()).into(userImage);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_view, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.your_lunch:
               for (SelectedPlace selectedPlace : dataBaseService.getListOfSelectedPlaces()){
                   for (String userId : selectedPlace.getUserId()){
                       if (userId.equals(service.getUser().getId())) {
                           for (PlaceMarker place : service.getListMarkers()) {
                               if (place.getId().equals(selectedPlace.getId())) {
                                   getMarkerDetails(place);
                                   service.setPlaceMarker(place);
                                   startActivity(new Intent(this, DetailsActivity.class));
                               }
                           }
                       }
                   }
               }
                break;
            case R.id.chat:
                Intent chatIntent = new Intent(this, ChatActivity.class);
                startActivity(chatIntent);
                break;
                case R.id.settings:
                Intent settingsActivity = new Intent(this, Settings.class);
                startActivity(settingsActivity);
                break;
            case R.id.logout:
                service.setUser(null);
                service.setListMarkers(null);
                resetActivity();
                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, Main.class));
                finish();
                break;
            case R.id.mapViewItem:
                if (searchToolbar.getVisibility() == View.VISIBLE){
                    searchToolbar.setVisibility(View.GONE);
                }
                if (workMatesToolbar.getVisibility() == View.VISIBLE){
                    workMatesToolbar.setVisibility(View.GONE);
                }
                changeFragment(new MapFragment(), R.string.map_fragment);
                break;
            case R.id.listViewItem:
                if (searchToolbar.getVisibility() == View.VISIBLE){
                    searchToolbar.setVisibility(View.GONE);
                }
                if (workMatesToolbar.getVisibility() == View.VISIBLE){
                    workMatesToolbar.setVisibility(View.GONE);
                }
                changeFragment(new ListViewFragment(), R.string.list_fragment);
                break;
            case R.id.workMatesItem:
                if (searchToolbar.getVisibility() == View.VISIBLE){
                    searchToolbar.setVisibility(View.GONE);
                }
                changeFragment(new WorkmatesFragment(), R.string.map_fragment);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeFragment(Fragment fragment, int value){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment, String.valueOf(value)).addToBackStack(String.valueOf(value))
                .commit();
    }

    private void removeFragment(String TAG){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG);
        if(fragment != null)
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            if (searchToolbar.getVisibility() == View.VISIBLE){
                searchToolbar.setVisibility(View.GONE);
            }
            if (workMatesToolbar.getVisibility() == View.VISIBLE){
                workMatesToolbar.setVisibility(View.GONE);
            }
            mapsBottomNav.setSelectedItemId(R.id.mapViewItem);
            mapsBottomNav.setSelected(true);
        }
    }

    public void getMarkerDetails(PlaceMarker placeMarker){
            String uri = getString(R.string.url_begin) + placeMarker.getId() +
                    getString(R.string.and_key) + BuildConfig.GOOGLEAPIKEY;
            Object dataTransfer[] = new Object[3];
            dataTransfer[0] = uri;
            dataTransfer[1] = placeMarker;
            dataTransfer[2] = this;
            PlaceDetailsTask getNearbyPlacesData = new PlaceDetailsTask();
            getNearbyPlacesData.execute(dataTransfer);
    }

    private void resetActivity(){
        removeFragment(getString(R.string.map_fragment));
        removeFragment(getString(R.string.list_fragment));
        removeFragment(getString(R.string.workmates_fragment));
    }

}
