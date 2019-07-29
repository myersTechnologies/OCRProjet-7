package dasilva.marco.go4lunch.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String id;
    private String userName;
    private String userEmail;
    private String imageUrl;
    private String choice;
    private String radius;
    private List<String> likedPlacesId;

    public User(@NonNull String userId, @NonNull String userName, @NonNull String userEmail, @NonNull String imageUrl) {
        this.userName = userName;
        this.userEmail = userEmail;
        this.imageUrl = imageUrl;
        this.id = userId;
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

    public List<String> getLikedPlacesId() {
        return likedPlacesId;
    }

    public void setLikedPlacesId(String likedPlacesId) {
        if (this.likedPlacesId == null){
            this.likedPlacesId = new ArrayList<>();
        }
            this.likedPlacesId.add(likedPlacesId);

    }

    public String getFirtName(){
        String[] completeName = userName.split(" ");
        String firstName = completeName[0];
        return firstName;
    }
}
