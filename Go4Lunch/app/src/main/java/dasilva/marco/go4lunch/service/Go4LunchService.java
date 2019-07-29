package dasilva.marco.go4lunch.service;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.ui.map.adapters.RviewListAdapter;

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
    SupportMapFragment getMapView();
    void setMapView(SupportMapFragment mapView);
    OnMapReadyCallback getCallback();
    void setCallback(OnMapReadyCallback callback);
    GoogleMap getGoogleMap();
    void setGoogleMap(GoogleMap map);
    void setDataBase(DataBaseService dataBase);
    void addAdapter(RviewListAdapter adapter);
    RviewListAdapter getAdapter();
    void setUserLunchChoice(PlaceMarker placeMarker, Context context);
}
