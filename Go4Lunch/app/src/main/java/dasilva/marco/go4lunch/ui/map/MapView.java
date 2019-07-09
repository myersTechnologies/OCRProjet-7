package dasilva.marco.go4lunch.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlusCode;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.dialog.SettingsDialog;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.main.Main;
import dasilva.marco.go4lunch.ui.details.DetailsActivity;

public class MapView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, View.OnClickListener,
        BottomNavigationView.OnNavigationItemSelectedListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mapView;
    private FloatingActionButton fab;
    private SupportMapFragment mapFragment;
    private LatLng current;
    private PlacesClient placesClient;
    private Location currentLocation;
    private Go4LunchService service;
    private TextView userNameText, userEmailText;
    private ImageView userImage;
    private SettingsDialog settingsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        service = DI.getService();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        userEmailText = (TextView)  navigationView.getHeaderView(0).findViewById(R.id.userEmail);
        userNameText = (TextView) navigationView.getHeaderView(0).findViewById(R.id.userName);
        userImage = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.userAvatar);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        BottomNavigationView mapsBottomNav = (BottomNavigationView) findViewById(R.id.nav_bottom_maps);
        fab = findViewById(R.id.my_location_fab);
        mapsBottomNav.setOnNavigationItemSelectedListener(this);
        fab.setOnClickListener(this);

        initiPlaces();

        mapFragment.getMapAsync(this);

    }

    //initializes places client
    public void initiPlaces() {
        Places.initialize(this, String.valueOf(R.string.google_api_key));
        placesClient = Places.createClient(this);

        //views for navigation drawer textViews and set user info inside them
        userNameText.setText(service.getUser().getUserName());
        userEmailText.setText(service.getUser().getUserEmail());

        if (service.getUser().getImageUrl().contains("google")) {
            Glide.with(this).load(service.getUser().getImageUrl()).apply(RequestOptions.circleCropTransform()).into(userImage);
        } else {
            Glide.with(this).load(service.getUser().getImageUrl() + "?type=large")
                    .apply(RequestOptions.circleCropTransform()).into(userImage);
        }

    }

    public void onMapReady(GoogleMap googleMap) {
        service.setUsersList();
        service.setListOfSelectedPlaces();

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
                    mapView.addMarker(new MarkerOptions().position(current).title("You're here"));
                    mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                    service.setCurrentLocation(location);
                }
            });
        }
    }

    //get google maps information about restaurants
    public void getMapMarker(){
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + currentLocation.getLatitude()
                + "," + currentLocation.getLongitude() +
                "&radius=" + service.getUser().getRadius() + "&type=restaurant&keyword=food&key=" + "AIzaSyDKZnjJaY7UQxDrXsskimpfMb_vY4s6ltc";
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


    private void autoCompleteIntent(){
        int AUTOCOMPLETE_REQUEST_CODE = 1;
// Set the fields to specify which types of place data to
// return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

// Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
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
                        String[] coordinates = service.getListOfSelectedPlaces().get(j).getLatLng().split(",");
                        String[] coordinatesLat = coordinates[0].split(Pattern.quote("("));
                        String[] coordinatesLng = coordinates[1].split(Pattern.quote(")"));
                        String lngString = coordinatesLng[0];
                        String latString = coordinatesLat[1];
                        double lat = Double.valueOf(latString);
                        double lng = Double.valueOf(lngString);
                        LatLng latLng = new LatLng(lat, lng);
                        markerOptions.position(latLng);
                        markerOptions.title(service.getListOfSelectedPlaces().get(j).getName());
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        mapView.addMarker(markerOptions);
                    }
                }

                break;
            case R.id.settings:
                settingsDialog = new SettingsDialog(this);
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
                        .replace(R.id.container, mapFragment, "MapViewFragment")
                        .commit();
                getSupportFragmentManager().beginTransaction().show(mapFragment).commit();
                mapFragment.getMapAsync(this);
                fab.show();
                break;
            case R.id.listViewItem:
                getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new ListViewFragment(), "ListViewFragment")
                        .commit();
                fab.hide();
                break;
            case R.id.workMatesItem:
                getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, new WorkmatesFragment(), "WorkMatesFragment")
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
                            mapView.addMarker(new MarkerOptions().position(current).title("You're here"));
                            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

                        }
                    });
                }
                break;
        }
    }

    // Get marker information to pass them to details activity    @Override
    public boolean onMarkerClick(Marker marker) {
        String url = "";
        String placeMarkerId = "";
        int count = service.getListMarkers().size();
        for (int i = 0; i < count; i++){
            if (service.getListMarkers().get(i).getLatLng().equals(marker.getPosition())){
                PlaceMarker placeMarker = service.getListMarkers().get(i);
                placeMarkerId = placeMarker.getId();
                service.setPlaceMarker(placeMarker);
                service.getPlaceMarker().setPoint(marker);
                url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeMarkerId + "&key="
                        + "AIzaSyDKZnjJaY7UQxDrXsskimpfMb_vY4s6ltc";

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
        Toast.makeText(this, "Resume", Toast.LENGTH_SHORT).show();
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
