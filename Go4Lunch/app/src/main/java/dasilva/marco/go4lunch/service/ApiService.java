package dasilva.marco.go4lunch.service;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.ui.map.adapters.RviewListAdapter;

public class ApiService implements Go4LunchService {

    private PlaceMarker placeMarker;
    private User user;
    private List<PlaceMarker> places;
    private Location location;
    private SupportMapFragment googleMap;
    private OnMapReadyCallback callback;
    private GoogleMap map;
    private DataBaseService dataBaseService;
    private RviewListAdapter adapter;


    @Override
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setPlaceMarker(PlaceMarker placeMarker) {
        this.placeMarker = placeMarker;
    }

    @Override
    public PlaceMarker getPlaceMarker() {
        return placeMarker;
    }

    @Override
    public List<PlaceMarker> getListMarkers() {
        return places;
    }

    @Override
    public void setListMarkers(List<PlaceMarker> places) {
        this.places = places;
    }

    @Override
    public Location getCurrentLocation() {
        return location;
    }

    @Override
    public void setCurrentLocation(Location location) {
        this.location = location;
    }

    @Override
    public double getDistance(Location target, Location current) {
        return target.distanceTo(current);
    }

    @Override
    public void setUserRadius(String radius) {
        user.setRadius(radius);
        dataBaseService.setUserRadius(radius);
    }

    @Override
    public void setUserLikedPlaces(String userLikedPlaces) {
        dataBaseService.setUserLikedPlaces(userLikedPlaces);
    }

    @Override
    public void countPlaceSelectedByUsers() {
        List<User> users = dataBaseService.getUsersList();
        List<SelectedPlace> selectedPlaces = dataBaseService.getListOfSelectedPlaces();
        for (PlaceMarker marker : places) {
            int count = 0;
            for (User user : users) {
                for (SelectedPlace selectedPlace : selectedPlaces) {
                    for (String userId : selectedPlace.getUserId().split(",")) {
                        if (user.getId().equals(userId)) {
                            if (marker.getId().contains(selectedPlace.getId())) {
                                count++;
                                marker.setSelectedTimes(count);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public LatLng getRealLatLng(SelectedPlace place) {
           String[] coordinates = place.getLatLng().split(",");
           String[] coordinatesLat = coordinates[0].split(Pattern.quote("("));
           String[] coordinatesLng = coordinates[1].split(Pattern.quote(")"));
           String lngString = coordinatesLng[0];
           String latString = coordinatesLat[1];
           double lat = Double.valueOf(latString);
           double lng = Double.valueOf(lngString);
        return new LatLng(lat, lng);
    }

    @Override
    public void countPlacesLikes() {
        List<User> users = dataBaseService.getUsersList();
        for (PlaceMarker marker : places){
            int count = 0;
            for (User user : users){
                if (user.getLikedPlacesId() != null) {
                    for (String likedPlaces : user.getLikedPlacesId().split(",")) {
                        if (marker.getId().contains(likedPlaces)) {
                            count++;
                            marker.setLikes(count);
                        }
                    }
                }
            }

        }
    }

    @Override
    public String getTodayClosingHour(PlaceMarker placeMarker) {
            String openUntil = "";
        if (placeMarker.getWeekdayHous() != null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEEE");
            String dayOfTheWeek = dateFormat.format(calendar.getTime());
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            List<String> openHourList = placeMarker.getWeekdayHous();
            int count = 1;
            for (String today : openHourList) {
                count++;
                if (count == day) {
                    if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                        try {
                            String[] openHours = today.split(",");
                            String[] openedUntil = openHours[0].split("–");
                            openUntil = openedUntil[1];
                        } catch (ArrayIndexOutOfBoundsException e){
                            String[] openHours = today.split("–");
                            String[] openHourWithoutDay = openHours[0].split(dayOfTheWeek + ":");
                            if (Arrays.toString(openHours).contains("Open 24 hours")){
                                openUntil = "24/7";
                            } else {
                                openUntil = openHourWithoutDay[1];
                            }
                        }
                    } else {
                        try {
                            String[] openHours = today.split(",");
                            String[] openedUntil = openHours[1].split("–");
                            openUntil = openedUntil[1];
                        } catch (ArrayIndexOutOfBoundsException e){
                            String[] openHours = today.split("–");
                            openUntil = openHours[1];
                        }
                    }
                }
            }
        }
        return openUntil;
    }

    @Override
    public String getTodayOpenHour(PlaceMarker placeMarker) {
        String openUntil = "";
        if (placeMarker.getWeekdayHous() != null) {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            String dayOfTheWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
            List<String> openHourList = placeMarker.getWeekdayHous();
            int count = 1;
            for (String today : openHourList) {
                count++;
                if (count == day) {
                    if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                        try {
                        String[] openHours = today.split(",");
                        String[] openedUntil = openHours[0].split("–");
                        String [] openHourWithoutDay = openedUntil[0].split(dayOfTheWeek + ":");
                        openUntil = openHourWithoutDay[1];
                        } catch (ArrayIndexOutOfBoundsException e){
                            String[] openHours = today.split("–");
                            String[] openedUntil = openHours[0].split(dayOfTheWeek + ":");
                            openUntil = openedUntil[1];
                        }
                    } else {
                        try {
                            String[] openHours = today.split(",");
                            String[] openedUntil = openHours[0].split("–");
                            String[] openHourWithoutDay = openedUntil[0].split(dayOfTheWeek + ":");
                            openUntil = openHourWithoutDay[1];
                        } catch (ArrayIndexOutOfBoundsException e){
                            String[] openHours = today.split("–");
                            String [] openHourWithoutDay = openHours[0].split(dayOfTheWeek + ":");
                            openUntil = openHourWithoutDay[0];
                        }

                    }
                }
            }
        }
        return openUntil;
    }

    @Override
    public SupportMapFragment getMapView() {
        return googleMap;
    }

    @Override
    public void setMapView(SupportMapFragment mapView) {
        this.googleMap = mapView;
    }

    @Override
    public OnMapReadyCallback getCallback() {
        return callback;
    }

    @Override
    public void setCallback(OnMapReadyCallback callback) {
        this.callback = callback;
    }


    @Override
    public GoogleMap getGoogleMap() {
        return map;
    }

    @Override
    public void setGoogleMap(GoogleMap map) {
        this.map = map;
    }

    @Override
    public void setDataBase(DataBaseService dataBase) {
        this.dataBaseService = dataBase;
    }

    @Override
    public void addAdapter(RviewListAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public RviewListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setUserLunchChoice(PlaceMarker placeMarker, Context context) {
        if (places == null){
            places = new ArrayList<>();
        }
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses  = null;
        try {
            addresses = geocoder.getFromLocation(placeMarker.getLatLng().latitude, placeMarker.getLatLng().longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = null;
        if (addresses != null) {
            address = addresses.get(0).getAddressLine(0);
        }
        String city = null;
        if (addresses != null) {
            city = addresses.get(0).getLocality();
        }
        String country = null;
        if (addresses != null) {
            country = addresses.get(0).getCountryName();
        }
        placeMarker.setAdress(address + ", " + city + ", " + country);

        if (!places.contains(placeMarker)) {
            places.add(placeMarker);
        }
        Log.d("LISTSIZE", String.valueOf(places.size()));


    }

}
