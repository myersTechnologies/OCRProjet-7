package dasilva.marco.go4lunch.model;

public class SelectedPlace {

    private String id;
    private String name;
    private String latLng;
    private String userId;

    public SelectedPlace(String id, String name, String latLng, String userId) {
        this.id = id;
        this.name = name;
        this.latLng = latLng;
        this.userId = userId;
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



}
