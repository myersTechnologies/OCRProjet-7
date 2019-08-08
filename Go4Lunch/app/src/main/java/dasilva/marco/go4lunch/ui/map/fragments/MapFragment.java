package dasilva.marco.go4lunch.ui.map.fragments;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

import dasilva.marco.go4lunch.BuildConfig;
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

import static android.content.Context.MODE_PRIVATE;


public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener,  GoogleMap.OnMarkerClickListener{

    private SupportMapFragment mapFragment;
    private GoogleMap mapView;
    private FloatingActionButton fab;
    private LatLng current;
    private Location currentLocation;
    private Go4LunchService service;
    private DataBaseService dataBaseService;
    private SharedPreferences sharedPreferences;
    private Toolbar toolbar = null;
    private static final String API_KEY = BuildConfig.GOOGLEAPIKEY;
    private PlacesClient client;
    private AutoCompleteTextView autoCompleteTextView;


    public MapFragment() {
        // Required empty public constructor
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
                            if (service.getListMarkers() != null) {
                                getMarkersFromList();
                            } else {
                                getMapMarker();
                            }
                            mapView.addMarker(new MarkerOptions().position(current));
                            mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                            getJoiningUsers();

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
        Places.initialize(getContext(), BuildConfig.GOOGLEAPIKEY);
        client = Places.createClient(getContext());


        final FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // GPS location can be null if GPS is switched off
                    currentLocation = location;
                    current = new LatLng(location.getLatitude(), location.getLongitude());
                    if (service.getListMarkers() != null) {
                       getMarkersFromList();
                    } else {
                        getMapMarker();
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
                    getString(R.string.radius_search_url) + service.getUser().getRadius() + getString(R.string.restaurant_type_url) + API_KEY;
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
                toolbar = ((AppCompatActivity) getActivity()).findViewById(R.id.toolbar_search);
                toolbar.setVisibility(View.VISIBLE);
                autoCompleteTextView = toolbar.findViewById(R.id.auto_complete_text);
                autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        googlePredictions(s.toString());
                    }
                });

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void googlePredictions(String query){
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(service.getCurrentLocation().getLatitude(), service.getCurrentLocation().getLongitude()),
                new LatLng(service.getCurrentLocation().getLatitude(), service.getCurrentLocation().getLongitude()));
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setCountry("fr")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        client.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onSuccess(FindAutocompletePredictionsResponse response) {
                final List<String> idList = new ArrayList<>();
                List<String> places = new ArrayList<>();
                for (AutocompletePrediction result : response.getAutocompletePredictions()){
                    if (result.getPlaceTypes().toString().toLowerCase().contains("restaurant")) {
                        idList.add(result.getPlaceId());
                        places.add(result.getPrimaryText(null) + "\n" + result.getSecondaryText(null));
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, places);
                autoCompleteTextView.setAdapter(adapter);
                autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getContext(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                        String [] predictions = parent.getItemAtPosition(position).toString().split("\n");
                        String predictionId = idList.get(position);
                        String predictionName = predictions[0];
                        PlaceMarker placeMarker = new PlaceMarker();
                        placeMarker.setId(predictionId);
                        placeMarker.setName(predictionName);
                        getMarkerDetails(placeMarker);
                        toolbar.setVisibility(View.GONE);
                        autoCompleteTextView.setText("");
                    }
                });

            }
        });

        client.findAutocompletePredictions(request).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    public void getMarkerDetails(PlaceMarker marker){
        String uri = getContext().getString(R.string.url_begin) + marker.getId() +
                getContext().getString(R.string.and_key) + API_KEY;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (toolbar != null) {
            if (toolbar.getVisibility() == View.VISIBLE) {
                toolbar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        if (toolbar != null) {
            if (toolbar.getVisibility() == View.VISIBLE) {
                toolbar.setVisibility(View.GONE);
            }
        }
    }

}
