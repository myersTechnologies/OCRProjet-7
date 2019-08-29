package dasilva.marco.go4lunch.service;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;

public interface Go4LunchService {

    void setUser(User user);
    User getUser();
    void setPlaceMarker(PlaceMarker placeMarker);
    PlaceMarker getPlaceMarker();
    List<PlaceMarker> getListMarkers();
    void setListMarkers(List<PlaceMarker> places);
    Location getCurrentLocation();
    void setCurrentLocation(Location location);
    double getDistance(Location target, Location current);
    void setUserRadius(String radius);
    void setUserLikedPlaces(List<String> userLikedPlaces);
    void countPlaceSelectedByUsers();
    LatLng getRealLatLng(SelectedPlace place);
    void countPlacesLikes();
    String getTodayClosingHour(PlaceMarker place);
    String getTodayOpenHour(PlaceMarker place);
    void setDataBase(DataBaseService dataBase);
    void setUserLunchChoice(PlaceMarker placeMarker, Context context);
}
