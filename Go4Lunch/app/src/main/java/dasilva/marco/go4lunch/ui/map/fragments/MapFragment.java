package dasilva.marco.go4lunch.ui.map.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.details.DetailsActivity;
import dasilva.marco.go4lunch.ui.map.utils.nearby.GetNearbyPlacesData;
import dasilva.marco.go4lunch.ui.map.utils.details.PlaceDetailsTask;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;


public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener,  GoogleMap.OnMarkerClickListener{

    private SupportMapFragment mapFragment;
    private GoogleMap mapView;
    private FloatingActionButton fab;
    private LatLng current;
    private Location currentLocation;
    private Go4LunchService service;
    private DataBaseService dataBaseService;
    private static MapFragment maps;
    private SharedPreferences sharedPreferences;



    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(){
        if (maps == null){
            maps = new MapFragment();
        }
        return maps;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        service = DI.getService();
        fab = view.findViewById(R.id.my_location_fab);
        fab.setOnClickListener(this);
        dataBaseService = DI.getDatabaseService();

        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle( R.string.title_activity_map_view);

        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_location_fab:
                FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            currentLocation = location;
                            current = new LatLng(location.getLatitude(), location.getLongitude());
                            if (service.getListMarkers() == null) {
                                getMapMarker();
                            } else {
                                getMarkersFromList();
                            }
                            mapView.addMarker(new MarkerOptions().position(current));
                            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

                        }
                    });
                }
                break;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        int count = service.getListMarkers().size();
        for (int i = 0; i < count; i++){
            if (service.getListMarkers().get(i).getLatLng().equals(marker.getPosition())){
                PlaceMarker placeMarker = service.getListMarkers().get(i);
                service.setPlaceMarker(placeMarker);
                startActivity(new Intent(getActivity(), DetailsActivity.class));
            }
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        service.setMapView(mapFragment);
        service.setCallback(this);
        service.setGoogleMap(mapView);
        mapView.setOnMarkerClickListener(this);


        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // GPS location can be null if GPS is switched off
                    currentLocation = location;
                    current = new LatLng(location.getLatitude(), location.getLongitude());
                    if (service.getListMarkers() == null) {
                        getMapMarker();
                    } else {
                        getMarkersFromList();
                    }
                    mapView.addMarker(new MarkerOptions().position(current));
                    mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                    service.setCurrentLocation(location);
                    getJoiningUsers();


                }
            });
        }
    }

    //get google maps information about restaurants
    public void getMapMarker(){
            String url = getString(R.string.first_part_url) + currentLocation.getLatitude()
                    + "," + currentLocation.getLongitude() +
                    getString(R.string.radius_search_url) + service.getUser().getRadius() + getString(R.string.restaurant_type_url) + getString(R.string.google_api_key);
            Object dataTransfer[] = new Object[3];
            dataTransfer[0] = mapView;
            dataTransfer[1] = url;
            dataTransfer[2] = getContext();
            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
            getNearbyPlacesData.execute(dataTransfer);
    }

    public void getMarkersFromList(){
        MarkerOptions markerOptions = new MarkerOptions();
        setMarker(markerOptions);
        if (!dataBaseService.getListOfSelectedPlaces().isEmpty()) {
            for (SelectedPlace selectedPlace: dataBaseService.getListOfSelectedPlaces()) {
                for (PlaceMarker placeMarker: service.getListMarkers()) {
                    if (selectedPlace.getId().equals(placeMarker.getId())) {
                        markerOptions.position(placeMarker.getLatLng());
                        markerOptions.title(placeMarker.getName());
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        mapView.addMarker(markerOptions);
                    }
                }

            }

        } else {
            setMarker(markerOptions);
        }
    }

    public void setMarker(MarkerOptions markerOptions){
        for (PlaceMarker placeMarker : service.getListMarkers()) {
            markerOptions.position(new LatLng(placeMarker.getLatLng().latitude, placeMarker.getLatLng().longitude));
            markerOptions.title(placeMarker.getName());
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            mapView.addMarker(markerOptions);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.search:

                int AUTOCOMPLETE_REQUEST_CODE = 6;

                if (!Places.isInitialized()) {
                    Places.initialize(getContext(), getString(R.string.google_places_api_key));
                    PlacesClient placesClient = Places.createClient(getContext());
                }

                List<Place.Field> fieldList = Arrays.asList(Place.Field.NAME, Place.Field.ID,Place.Field.LAT_LNG, Place.Field.ADDRESS,
                        Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER, Place.Field.TYPES);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fieldList)
                        .build(getContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 6) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                PlaceMarker marker = new PlaceMarker();
                marker.setId(place.getId());
                marker.setName(place.getName());
                marker.setAdress(place.getAddress());
                marker.setLatLng(place.getLatLng());
                getMarkerDetails(marker);
                if (!service.getListMarkers().contains(marker)) {
                    service.getListMarkers().add(marker);
                }

                MarkerOptions markerOptions = new MarkerOptions().position(place.getLatLng());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                mapView.addMarker(markerOptions);
                mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }


    public void getMarkerDetails(PlaceMarker marker){
        String uri = getContext().getString(R.string.url_begin) + marker.getId() +
                getContext().getString(R.string.and_key) + getContext().getString(R.string.google_maps_key);
        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = uri;
        dataTransfer[1] = marker;
        PlaceDetailsTask getDetailsPlacesData = new PlaceDetailsTask();
        getDetailsPlacesData.execute(dataTransfer);

    }

    public void getJoiningUsers() {
        List<String> usersFistName = new ArrayList<>();

        PlaceMarker placeMarker = new PlaceMarker();
        for (SelectedPlace selectedPlace : dataBaseService.getListOfSelectedPlaces()) {
            for (String userId : selectedPlace.getUserId()) {
                if (userId.equals(service.getUser().getId())) {
                    placeMarker.setName(selectedPlace.getName());
                    placeMarker.setId(selectedPlace.getId());
                }
            }
        }

        List<User> userList = new ArrayList<>();
        for(SelectedPlace place : dataBaseService.getListOfSelectedPlaces()){
            for (User user : dataBaseService.getUsersList()){
                if (place.getId().equals(placeMarker.getId())){
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
        }

        if (userList.size() > 0) {
            for (User user : userList) {
                usersFistName.add(user.getFirtName());
            }
        } else {
            usersFistName.add(getString(R.string.nobody_joining));
        }

        String joiningUsers = TextUtils.join(", ", usersFistName);

        sharedPreferences.edit().putString(getString(R.string.joining_users), joiningUsers).apply();
    }



}
