package dasilva.marco.go4lunch.service;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;

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
    Context context;


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
    public void setUsersList() {
        users = new ArrayList<>();
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = firebaseDatabase.getReference("users");

            databaseRef.orderByChild("userEmail").addValueEventListener(new ValueEventListener() {
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
                    String placeId = postSnapshot.child("placeId").getValue().toString();
                    SelectedPlace selectedPlace = new SelectedPlace(id, name, latLng, userId, placeId);
                    selectedPlaces.add(selectedPlace);
                    Log.d("PlacesCheck", selectedPlace.getName());
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
                if (user.getId().equals(selectedPlaces.get(i).getUserId())) {
                    selectedPlace = selectedPlaces.get(i);
                }
            }
        }
        selectedPlaces.remove(selectedPlace);
        databaseReference.child(selectedPlace.getId()).removeValue();
        user.setChoice(null);
        userReference.child(user.getId()).child("choice").removeValue();
    }

    @Override
    public void setUserRadius(String radius) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(user.getId()).child("radius").setValue(radius);
        user.setRadius(radius);
    }
}
