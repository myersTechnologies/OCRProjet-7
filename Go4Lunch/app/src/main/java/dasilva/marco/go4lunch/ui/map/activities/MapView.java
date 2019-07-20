package dasilva.marco.go4lunch.ui.map.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.dialog.SettingsDialog;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.main.Main;
import dasilva.marco.go4lunch.ui.details.DetailsActivity;
import dasilva.marco.go4lunch.ui.map.fragments.ListViewFragment;
import dasilva.marco.go4lunch.ui.map.fragments.WorkmatesFragment;
import dasilva.marco.go4lunch.ui.map.utils.GetNearbyPlacesData;
import dasilva.marco.go4lunch.ui.map.utils.JsonTask;


public class MapView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, View.OnClickListener,
        BottomNavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mapView;
    private FloatingActionButton fab;
    private SupportMapFragment mapFragment;
    private LatLng current;
    private Location currentLocation;
    private Go4LunchService service;
    private TextView userNameText, userEmailText;
    private ImageView userImage;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_map_view);
        setSupportActionBar(toolbar);

        service = DI.getService();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userEmailText = navigationView.getHeaderView(0).findViewById(R.id.userEmail);
        userNameText = navigationView.getHeaderView(0).findViewById(R.id.userName);
        userImage = navigationView.getHeaderView(0).findViewById(R.id.userAvatar);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        BottomNavigationView mapsBottomNav =  findViewById(R.id.nav_bottom_maps);
        fab = findViewById(R.id.my_location_fab);
        mapsBottomNav.setOnNavigationItemSelectedListener(this);
        fab.setOnClickListener(this);

        initView();
        service.setUsersList();
        service.setListOfSelectedPlaces();
    }

    //initializes places client
    public void initView() {
        Places.initialize(this, String.valueOf(R.string.google_api_key));
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

    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        mapView.setOnMarkerClickListener(this);

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // GPS location can be null if GPS is switched off
                    currentLocation = location;
                    current = new LatLng(location.getLatitude(), location.getLongitude());
                    getMapMarker();
                    mapView.addMarker(new MarkerOptions().position(current));
                    mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                    service.setCurrentLocation(location);
                }
            });
        }
    }

    //get google maps information about restaurants
    public void getMapMarker(){
        String url = getString(R.string.first_part_url) + currentLocation.getLatitude()
                + "," + currentLocation.getLongitude() +
                getString(R.string.radius_search_url)+ service.getUser().getRadius() + getString(R.string.restaurant_type_url) + getString(R.string.google_api_key);
        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mapView;
        dataTransfer[1] = url;

        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataTransfer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.search:

                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.your_lunch:
                for (int j = 0; j < service.getListOfSelectedPlaces().size(); j++) {
                    if (service.getListOfSelectedPlaces().get(j).getUserId().compareTo(service.getUser().getId()) == 0 ) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = service.getRealLatLng(service.getListOfSelectedPlaces().get(j));
                        markerOptions.position(latLng);
                        markerOptions.title(service.getListOfSelectedPlaces().get(j).getName());
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        mapView.addMarker(markerOptions);
                    }
                }

                break;
            case R.id.settings:
                SettingsDialog settingsDialog = new SettingsDialog(this);
                settingsDialog.createSettingsDialog();
                break;
            case R.id.logout:
                LoginManager.getInstance().logOut();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Main.class));
                break;
            case R.id.mapViewItem:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, mapFragment, String.valueOf(R.string.map_fragment))
                        .commit();
                getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
                mapFragment.getMapAsync(this);
                fab.show();
                break;
            case R.id.listViewItem:
                service.countPlaceSelectedByUsers();
                service.countPlacesLikes();
                getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new ListViewFragment(), String.valueOf(R.string.list_fragment))
                        .commit();
                fab.hide();
                break;
            case R.id.workMatesItem:
                getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new WorkmatesFragment(), String.valueOf(R.string.workmates_fragment))
                        .commit();
                fab.hide();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Get actual location with FloatingActionButton
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_location_fab:
                FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            currentLocation = location;
                            current = new LatLng(location.getLatitude(), location.getLongitude());
                            getMapMarker();
                            mapView.addMarker(new MarkerOptions().position(current));
                            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

                        }
                    });
                }
                break;
        }
    }

    // Get marker information to pass them to details activity
    @Override
    public boolean onMarkerClick(Marker marker) {
        String url;
        String placeMarkerId;
        int count = service.getListMarkers().size();
        for (int i = 0; i < count; i++){
            if (service.getListMarkers().get(i).getLatLng().equals(marker.getPosition())){
                PlaceMarker placeMarker = service.getListMarkers().get(i);
                placeMarkerId = placeMarker.getId();
                service.setPlaceMarker(placeMarker);
                service.getPlaceMarker().setPoint(marker);
                url = getString(R.string.url_begin) + placeMarkerId + getString(R.string.and_key)
                        + getString(R.string.google_api_key);

                new JsonTask(url).execute();
                startActivity(new Intent(this, DetailsActivity.class));
            }
        }
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mapView != null){
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mapView != null){
            mapFragment.getMapAsync(this);
        }
    }
}
