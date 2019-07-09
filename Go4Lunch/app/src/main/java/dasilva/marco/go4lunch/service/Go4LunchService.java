package dasilva.marco.go4lunch.service;

import android.content.Context;
import android.location.Location;

import java.util.List;

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
    void setUsersList();
    List<User> getUsersList();
    void addUserToDatabase();
    List<SelectedPlace> getListOfSelectedPlaces();
    void setListOfSelectedPlaces();
    void removeCompleteSelectionDatabase();
    void setUserRadius(String radius);

}
