package dasilva.marco.go4lunch.model;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class User {

    private String id;
    private String userName;
    private String userEmail;
    private String imageUrl;
    private String choice;
    private String radius;

    public User(@NonNull String userId, @NonNull String userName, @NonNull String userEmail, @NonNull String imageUrl) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.imageUrl = imageUrl;
        this.id = userId;
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getChoice() {
        return choice;
    }

    public void setChoice(String choice) {
        this.choice = choice;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }
}
