package dasilva.marco.go4lunch.firebase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class Go4LunchDataBase implements DataBaseService {

    private List<User>  users;
    private SelectedPlace selectedPlace;
    private List<SelectedPlace> selectedPlaces;
    private Go4LunchService service;
    private static String USERS = "users";
    private static String ID = "id";
    private static String USERNAME = "userName";
    private static String USER_EMAIL = "userEmail";
    private static String IMAGE_URL = "imageUrl";
    private static String CHOICE = "choice";
    private static String LIKED_PLACES_ID = "likedPlacesId";
    private static String SELECTION = "selection";
    private static String PLACE_NAME = "name";
    private static String LAT_LNG = "latLng";
    private static String USER_ID = "userId";
    private static String RADIUS = "radius";

    public Go4LunchDataBase(){
        service = DI.getService();
    }

    @Override
    public void setUsersList() {
            users = new ArrayList<>();
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = firebaseDatabase.getReference(USERS);
            databaseRef.orderByChild(ID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    users.clear();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        String id = postSnapshot.child(ID).getValue().toString();
                        String name = postSnapshot.child(USERNAME).getValue().toString();
                        String email = postSnapshot.child(USER_EMAIL).getValue().toString();
                        String image = postSnapshot.child(IMAGE_URL).getValue().toString();
                        User user = new User(id, name, email, image);

                        if (postSnapshot.child(CHOICE).getValue() != null) {
                            String userChoice = postSnapshot.child(CHOICE).getValue().toString();
                            user.setChoice(userChoice);
                        }


                        if (user.getLikedPlacesId() == null) {
                            for (DataSnapshot childSnapshot : postSnapshot.child(LIKED_PLACES_ID).getChildren()) {
                                String placeLikedId = childSnapshot.getValue(String.class);
                                user.setLikedPlacesId(placeLikedId);
                            }
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
    public void deleteUserFromFireBase() {
        User user = service.getUser();
        try {
            removeCompleteSelectionDatabase();
        }catch (NullPointerException e){}

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(USERS);
        mDatabase.child(user.getId()).removeValue();
        users.remove(user);
        if (firebaseUser != null) {
            firebaseUser.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("COMPLETED", "DELETED USER");
                            }
                        }
                    });
        }
        service.setUser(null);

    }

    @Override
    public List<SelectedPlace> getListOfSelectedPlaces() {
        return selectedPlaces;
    }

    @Override
    public void setListOfSelectedPlaces() {
            selectedPlaces = new ArrayList<>();
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference = firebaseDatabase.getReference(SELECTION);
            databaseReference.orderByChild(ID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    selectedPlaces.clear();
                    SelectedPlace selectedPlace;
                    String id;
                    String name;
                    String latLng;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        id = postSnapshot.child(ID).getValue().toString();
                        name = postSnapshot.child(PLACE_NAME).getValue().toString();
                        latLng = postSnapshot.child(LAT_LNG).getValue().toString();
                        selectedPlace = new SelectedPlace(id, name, latLng);

                        for (DataSnapshot childSnapshot : postSnapshot.child(USER_ID).getChildren()) {
                            String userId = childSnapshot.getValue(String.class);
                            selectedPlace.setUserId(userId);
                        }
                        selectedPlaces.add(selectedPlace);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    //to remove selected restaurant
    //if the usersId Arrays is superior or equals to 2 then delete only the user id
    //else if its 1 then the selected place is deleted from database
    @Override
    public void removeCompleteSelectionDatabase() {
        User user = service.getUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference(SELECTION);
        DatabaseReference userReference = firebaseDatabase.getReference(USERS);
            for (int i = 0; i < selectedPlaces.size(); i++) {
                if (selectedPlaces.get(i).getUserId().size() >= 2){
                    for (String userId : selectedPlaces.get(i).getUserId()) {
                        if (user.getId().contains(userId)) {
                            selectedPlace = selectedPlaces.get(i);
                            selectedPlaces.get(i).getUserId().remove(user.getId());
                            databaseReference.child(selectedPlace.getId()).setValue(selectedPlace);
                            user.setChoice(null);
                            userReference.child(user.getId()).child(CHOICE).removeValue();
                        }
                    }
                } else {
                    for (String userId : selectedPlaces.get(i).getUserId()) {
                        if (user.getId().contains(userId)) {
                            selectedPlace = selectedPlaces.get(i);
                            selectedPlace.getUserId().remove(userId);
                            selectedPlaces.remove(selectedPlace);
                            databaseReference.child(selectedPlace.getId()).removeValue();
                            user.setChoice(null);
                            userReference.child(user.getId()).child(CHOICE).removeValue();
                        }
                        for (PlaceMarker placeMarker : service.getListMarkers()){
                            if (placeMarker.getId().equals(selectedPlace.getId())){
                                placeMarker.setSelectedTimes(0);
                            }
                        }
                    }
                }
            }
    }

    //to save radius on database
    @Override
    public void setUserRadius(String radius) {
        User user = service.getUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(USERS).child(user.getId()).child(RADIUS).setValue(radius);
    }

    //add a liked place to users table
    @Override
    public void setUserLikedPlaces(List<String> userLikedPlaces) {
        User user = service.getUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(USERS).child(user.getId()).child(LIKED_PLACES_ID).setValue(userLikedPlaces);
    }

    //get additional data, this is used because initially users haven't choiced, liked a place or radius is set up to google search
    @Override
    public void getAdditionalUserData(String userId){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference databaseRef = firebaseDatabase.getReference(USERS);
        databaseRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (service.getUser() != null) {
                        if (dataSnapshot.child(ID).getValue().toString().equals(service.getUser().getId())) {

                            if (dataSnapshot.child(CHOICE).getValue() != null) {
                                String choice = dataSnapshot.child(CHOICE).getValue().toString();
                                service.getUser().setChoice(choice);
                            }

                            if (dataSnapshot.child(RADIUS).getValue() != null) {
                                String radius = dataSnapshot.child(RADIUS).getValue().toString();
                                service.getUser().setRadius(radius);
                            } else {
                                service.getUser().setRadius("10");
                            }

                            if (service.getUser().getLikedPlacesId() == null) {
                                for (DataSnapshot childSnapshot : dataSnapshot.child(LIKED_PLACES_ID).getChildren()) {
                                    String placeLikedId = childSnapshot.getValue(String.class);
                                    service.getUser().setLikedPlacesId(placeLikedId);
                                }
                            }
                        }
                    }
                }
                if (service.getUser() != null) {
                    databaseRef.child(service.getUser().getId()).setValue(service.getUser());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
