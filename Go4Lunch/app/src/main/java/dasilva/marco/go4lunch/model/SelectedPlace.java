package dasilva.marco.go4lunch.model;

import java.util.ArrayList;
import java.util.List;

public class SelectedPlace {

    private String id;
    private String name;
    private String latLng;
    private List<String> userId;

    public SelectedPlace(String id, String name, String latLng) {
        this.id = id;
        this.name = name;
        this.latLng = latLng;
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

    public List<String> getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        if (this.userId == null){
            this.userId = new ArrayList<>();
        }
        this.userId.add(userId);
    }



}
