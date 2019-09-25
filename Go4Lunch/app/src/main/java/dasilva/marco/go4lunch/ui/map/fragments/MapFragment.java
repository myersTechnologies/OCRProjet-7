package dasilva.marco.go4lunch.ui.map.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import dasilva.marco.go4lunch.BuildConfig;
import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.dialog.LoadingDialog;
import dasilva.marco.go4lunch.events.DetailsEvent;
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

    private static MapFragment map;
    private GoogleMap mapView;
    private LatLng current;
    private Location currentLocation;
    private Go4LunchService service;
    private DataBaseService dataBaseService;
    private SharedPreferences sharedPreferences;
    private Toolbar toolbar;
    private static final String API_KEY = BuildConfig.GOOGLEAPIKEY;
    private PlacesClient client;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> adapter;
    private static final String RESTAURANT = "restaurant";
    private static final String FR = "fr";
    private List<String> idList;
    private LoadingDialog loadingDialog;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment getInstance(){
        if (map == null){
            map = new MapFragment();
        }
        return map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        toolbar = ((AppCompatActivity) getActivity()).findViewById(R.id.toolbar_search);
        service = DI.getService();
        loadingDialog = new LoadingDialog(getContext());
        if (service.getListMarkers() == null) {
            loadingDialog.showLoadingDialog();
        }
        FloatingActionButton fab = view.findViewById(R.id.my_location_fab);
        fab.setOnClickListener(this);
        dataBaseService = DI.getDatabaseService();
        startMap();
        setHasOptionsMenu(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle( R.string.title_activity_map_view);
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        return view;
    }

    private void startMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();
            if (location != null) {
                currentLocation = location;
                current = new LatLng(location.getLatitude(), location.getLongitude());
                if (service.getListMarkers() != null) {
                    getMarkersFromList();
                } else {
                    setUserChoiceToList();
                    getMapMarker();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadingDialog.dismissLoadingDialog();
                            getMarkersFromList();
                        }
                    }, 3000);
                }
                mapView.addMarker(new MarkerOptions().position(current));
                mapView.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
                service.setCurrentLocation(location);
                getJoiningUsers();
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.my_location_fab:
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    FusedLocationProviderClient mFusedLocationClient = null;
                    LocationRequest mLocationRequest = LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    if (mFusedLocationClient == null) {
                        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                locationCallback,
                                null /* Looper */);
                    }
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
        mapView.setOnMarkerClickListener(this);
        Places.initialize(getContext(), API_KEY);
        client = Places.createClient(getContext());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            FusedLocationProviderClient mFusedLocationClient = null;

            LocationRequest mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (mFusedLocationClient == null) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());
                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        locationCallback,
                        null /* Looper */);
            }
        }
    }

    //get google maps information about restaurants
    public void getMapMarker(){
            String url = getString(R.string.first_part_url) + currentLocation.getLatitude()
                    + "," + currentLocation.getLongitude() +
                    getString(R.string.radius_search_url) + service.getUser().getRadius() + getString(R.string.restaurant_type_url) + API_KEY;
            Object dataTransfer[] = new Object[2];
            dataTransfer[0] = url;
            dataTransfer[1] = getContext();
            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
            getNearbyPlacesData.execute(dataTransfer);
    }

    //get markers from list in APIService loaded markers
    public void getMarkersFromList(){
        if (service.getListMarkers() != null){
            MarkerOptions markerOptions = new MarkerOptions();
            setMarker(markerOptions);
        if (!dataBaseService.getListOfSelectedPlaces().isEmpty()) {
            for (SelectedPlace selectedPlace : dataBaseService.getListOfSelectedPlaces()) {
                for (PlaceMarker placeMarker : service.getListMarkers()) {
                    if (selectedPlace.getId().equals(placeMarker.getId())) {
                        if (placeMarker.getLatLng() != null) {
                            markerOptions.position(placeMarker.getLatLng());
                            markerOptions.title(placeMarker.getName());
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            mapView.addMarker(markerOptions);
                        }
                    }
                }
            }
        }
        }
    }

    //set choices into APIService markers list
    public void setUserChoiceToList(){
        if (dataBaseService.getListOfSelectedPlaces().size() > 0) {
            for (int i = 0; i < dataBaseService.getListOfSelectedPlaces().size(); i++) {
                SelectedPlace selectedPlace = dataBaseService.getListOfSelectedPlaces().get(i);
                PlaceMarker placeMarker = new PlaceMarker();
                placeMarker.setId(selectedPlace.getId());
                placeMarker.setLatLng(service.getRealLatLng(selectedPlace));
                placeMarker.setName(selectedPlace.getName());
                getMarkerDetails(placeMarker);
                service.setUserLunchChoice(placeMarker, getContext());
            }
        }
    }

    //set markers in maps
    public void setMarker(MarkerOptions markerOptions){
        if (service.getListMarkers() != null){
            for (PlaceMarker placeMarker : service.getListMarkers()) {
                markerOptions.position(new LatLng(placeMarker.getLatLng().latitude, placeMarker.getLatLng().longitude));
                markerOptions.title(placeMarker.getName());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                mapView.addMarker(markerOptions);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.search:
                autoCompleteTextView = toolbar.findViewById(R.id.auto_complete_text);
                autoCompleteTextView.setHint(getString(R.string.search_restaurant_text));
                autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        onItemClick();
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        googlePredictions(s.toString());
                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        onItemClick();
                    }
                });
                toolbar.setVisibility(View.VISIBLE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void googlePredictions(String query){
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(service.getCurrentLocation().getLatitude(), service.getCurrentLocation().getLongitude()),
                new LatLng(service.getCurrentLocation().getLatitude(), service.getCurrentLocation().getLongitude()));

     FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(bounds)
                .setCountry(FR)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        client.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onSuccess(FindAutocompletePredictionsResponse response) {
                idList = new ArrayList<>();
                List<String> places = new ArrayList<>();
                for (AutocompletePrediction result : response.getAutocompletePredictions()){
                    if (result.getPlaceTypes().toString().toLowerCase().contains(RESTAURANT)) {
                        idList.add(result.getPlaceId());
                        places.add(result.getPrimaryText(null) + "\n" + result.getSecondaryText(null));
                    }
                }
                adapter = new ArrayAdapter<>((getActivity()), android.R.layout.simple_list_item_1, places);
                autoCompleteTextView.setAdapter(adapter);
            }
        });

        client.findAutocompletePredictions(request).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    //on clicking on searched item, loading dialog will start for 1s, the time needed to load google information
    //and add marker to map
    private void onItemClick(){
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String [] predictions = parent.getItemAtPosition(position).toString().split("\n");
                String predictionId = idList.get(position);
                String predictionName = predictions[0];
                final PlaceMarker placeMarker = new PlaceMarker();
                placeMarker.setId(predictionId);
                placeMarker.setName(predictionName);
                getMarkerDetails(placeMarker);
                toolbar.setVisibility(View.GONE);
                autoCompleteTextView.getText().clear();
                loadingDialog.showLoadingDialog();
                hideSoftKeyboard(getActivity());
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (placeMarker.getLatLng() != null) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(placeMarker.getLatLng());
                            markerOptions.title(placeMarker.getName());
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(placeMarker.getLatLng(), 17));
                            mapView.addMarker(markerOptions);
                        }
                            loadingDialog.dismissLoadingDialog();
                    }
                }, 1000);
            }
        });
    }

    //to get marker details
    public void getMarkerDetails(PlaceMarker marker){
        String uri = getContext().getString(R.string.url_begin) + marker.getId() +
                getContext().getString(R.string.and_key) + API_KEY;
        Object dataTransfer[] = new Object[3];
        dataTransfer[0] = uri;
        dataTransfer[1] = marker;
        dataTransfer[2] = getContext();
        PlaceDetailsTask getDetailsPlacesData = new PlaceDetailsTask();
        getDetailsPlacesData.execute(dataTransfer);
    }

    public void getJoiningUsers() {
        PlaceMarker placeMarker = getUserChoicePlaceMarker();
        List<String> usersFistName = getUserChoiceJoiningUsers(placeMarker);
        setChoiceAdress(placeMarker);
        String joiningUsers = TextUtils.join(", ", usersFistName);
        sharedPreferences.edit().putString(getString(R.string.joining_users), joiningUsers).apply();
    }

    //first bubble we get all users joining current user choice
    //second bubble we remove the current user from the list
    //third bubble adding joining users first name to join in notification
    private List<String> getUserChoiceJoiningUsers(PlaceMarker placeMarker){
        List<String> usersFistName = new ArrayList<>();
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

        return usersFistName;
    }

    //comparing each selected place users id in array whit current user id
    // to get current restaurant choice
    private PlaceMarker getUserChoicePlaceMarker(){
        PlaceMarker placeMarker = new PlaceMarker();
        for (SelectedPlace selectedPlace : dataBaseService.getListOfSelectedPlaces()) {
            for (String userId : selectedPlace.getUserId()) {
                if (userId.equals(service.getUser().getId())) {
                    placeMarker.setName(selectedPlace.getName());
                    placeMarker.setId(selectedPlace.getId());
                }
            }
        }
        return placeMarker;
    }

    //it must have a delay for this or concurrentItemModification error will show
    private void setChoiceAdress(final PlaceMarker placeMarker){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (service.getListMarkers() != null) {
                    for (PlaceMarker marker : service.getListMarkers()) {
                        for (SelectedPlace place : dataBaseService.getListOfSelectedPlaces()) {
                            if (marker.getId().equals(place.getId())) {
                                if (place.getId().equals(placeMarker.getId())) {
                                    placeMarker.setAdress(marker.getAdress());
                                    sharedPreferences.edit().putString(getString(R.string.choice_adress), placeMarker.getAdress()).apply();
                                }
                            }
                        }
                    }
                }
            }
        }, 2200);

    }
    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
        startMap();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (autoCompleteTextView != null){
        autoCompleteTextView.getText().clear();
        }
    }

    @Subscribe
    public void getSelectionToDetails(DetailsEvent event){
        service.setPlaceMarker(event.placeMarker);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}
