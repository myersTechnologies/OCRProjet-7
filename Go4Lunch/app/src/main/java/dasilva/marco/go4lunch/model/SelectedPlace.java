package dasilva.marco.go4lunch.model;

import com.google.android.gms.maps.model.LatLng;

public class SelectedPlace {

    String id;
    String name;
    String latLng;
    String userId;
    String placeId;

    public SelectedPlace(String id, String name, String latLng, String userId, String placeId) {
        this.id = id;
        this.name = name;
        this.latLng = latLng;
        this.userId = userId;
        this.placeId = placeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
}
