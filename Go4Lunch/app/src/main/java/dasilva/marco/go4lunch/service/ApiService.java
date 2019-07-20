package dasilva.marco.go4lunch.service;


import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;

public class ApiService implements Go4LunchService {

    private PlaceMarker placeMarker;
    private User user;
    private List<PlaceMarker> places;
    private List<User>  users;
    private Location location;
    private SelectedPlace selectedPlace;
    private List<SelectedPlace> selectedPlaces;


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

    //Get users info from database
    @Override
    public void setUsersList() {
        users = new ArrayList<>();
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = firebaseDatabase.getReference("users");
            databaseRef.orderByChild("id").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    users.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String id = postSnapshot.child("id").getValue().toString();
                        String name = postSnapshot.child("userName").getValue().toString();
                        String email = postSnapshot.child("userEmail").getValue().toString();
                        String image = postSnapshot.child("imageUrl").getValue().toString();
                        User user = new User(id, name, email, image);
                        try {
                            String userChoice = postSnapshot.child("choice").getValue().toString();
                            user.setChoice(userChoice);
                        } catch (Exception e) {

                        }
                        try {
                            String likedPlaces = postSnapshot.child("likedplacesId").getValue().toString();
                            user.setLikedPlacesId(likedPlaces);
                        }catch (Exception e){

                        }
                        users.add(user);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    @Override
    public List<User> getUsersList() {
        return users;
    }

    @Override
    public void addUserToDatabase() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.child(user.getId()).setValue(user);
    }

    @Override
    public List<SelectedPlace> getListOfSelectedPlaces() {
        return selectedPlaces;
    }

    @Override
    public void setListOfSelectedPlaces() {
        selectedPlaces = new ArrayList<>();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("selection");
        databaseReference.orderByChild("id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                selectedPlaces.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    String id = postSnapshot.child("id").getValue().toString();
                    String name = postSnapshot.child("name").getValue().toString();
                    String latLng = postSnapshot.child("latLng").getValue().toString();
                    String userId = postSnapshot.child("userId").getValue().toString();
                    SelectedPlace selectedPlace = new SelectedPlace(id, name, latLng, userId);
                    selectedPlaces.add(selectedPlace);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void removeCompleteSelectionDatabase() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("selection");
        DatabaseReference userReference = firebaseDatabase.getReference("users");
        if (selectedPlaces.size() > 0) {
            for (int i = 0; i < selectedPlaces.size(); i++) {
                for (String userId : selectedPlaces.get(i).getUserId().split(",")) {
                    if (user.getId().contains(userId)) {
                        selectedPlace = selectedPlaces.get(i);
                    }
                }
            }
        }
        String[] places = selectedPlace.getUserId().split(",");
        if (places.length > 1){
            if (places[0].contains(user.getId())){
                String usersId = selectedPlace.getUserId().replace(user.getId() + ",", "");
                selectedPlace.setUserId(usersId);
                databaseReference.child(selectedPlace.getId()).setValue(selectedPlace);
                user.setChoice(null);
                userReference.child(user.getId()).child("choice").removeValue();
            } else {
                String usersId = selectedPlace.getUserId().replace("," + user.getId(), "");
                selectedPlace.setUserId(usersId);
                databaseReference.child(selectedPlace.getId()).setValue(selectedPlace);
                user.setChoice(null);
                userReference.child(user.getId()).child("choice").removeValue();
            }
        } else {
            selectedPlaces.remove(selectedPlace);
            databaseReference.child(selectedPlace.getId()).removeValue();
            user.setChoice(null);
            userReference.child(user.getId()).child("choice").removeValue();
        }
    }

    @Override
    public void setUserRadius(String radius) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(user.getId()).child("radius").setValue(radius);
        user.setRadius(radius);
    }

    @Override
    public void setUserLikedPlaces(String userLikedPlaces) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(user.getId()).child("likedplacesId").setValue(userLikedPlaces);
    }

    @Override
    public void countPlaceSelectedByUsers() {

        for (PlaceMarker marker : places){
            for (User user : users){
                for (SelectedPlace selectedPlace : selectedPlaces){
                    for (String userId : selectedPlace.getUserId().split(","))
                    if (user.getId().compareTo(userId) == 0){
                        if (marker.getId().contains(selectedPlace.getId())){
                            marker.setSelectedTimes();
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
           LatLng realLatLng = new LatLng(lat, lng);
        return realLatLng;
    }

    @Override
    public void countPlacesLikes() {
        for (PlaceMarker marker : places){
            for (User user : users){
                if (user.getLikedPlacesId() != null) {
                    for (String likedPlaces : user.getLikedPlacesId().split(",")) {
                        if (marker.getId().equals(likedPlaces)) {
                            marker.setLikes();
                        }
                    }
                }
            }
        }
    }
}
