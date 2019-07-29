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
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class Go4LunchDataBase implements DataBaseService {

    private List<User>  users;
    private SelectedPlace selectedPlace;
    private List<SelectedPlace> selectedPlaces;
    private Go4LunchService service;

    public Go4LunchDataBase(){
        service = DI.getService();
    }


    @Override
    public void setUsersList() {
            users = new ArrayList<>();
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            DatabaseReference databaseRef = firebaseDatabase.getReference("users");
            databaseRef.orderByChild("id").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<String> likesPlaces = new ArrayList<>();
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

                        if (user.getLikedPlacesId() == null) {
                            for (DataSnapshot childSnapshot : postSnapshot.child("likedPlacesId").getChildren()) {
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
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("selection");
        if (selectedPlaces.size() > 0) {
            for (int i = 0; i < selectedPlaces.size(); i++) {
                if (selectedPlaces.get(i).getUserId().size() > 1){
                    for (String userId : selectedPlaces.get(i).getUserId()) {
                        if (user.getId().contains(userId)) {
                            selectedPlaces.get(i).getUserId().remove(user.getId());
                            databaseReference.child(selectedPlace.getId()).setValue(selectedPlace);
                            break;
                        }
                    }
                } else {
                    selectedPlaces.remove(selectedPlace);
                    databaseReference.child(selectedPlace.getId()).removeValue();
                }

            }
        }

        }catch (NullPointerException e){}


        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.child(user.getId()).removeValue();
        users.remove(user);
        firebaseUser.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("COMPLETED", "DELETED USER") ;
                        }
                    }
                });
        user = null;
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
            DatabaseReference databaseReference = firebaseDatabase.getReference("selection");
            databaseReference.orderByChild("id").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    selectedPlaces.clear();
                    SelectedPlace selectedPlace;
                    String id;
                    String name;
                    String latLng;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        id = postSnapshot.child("id").getValue().toString();
                        name = postSnapshot.child("name").getValue().toString();
                        latLng = postSnapshot.child("latLng").getValue().toString();
                        selectedPlace = new SelectedPlace(id, name, latLng);

                        for (DataSnapshot childSnapshot : postSnapshot.child("userId").getChildren()) {
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


    @Override
    public void removeCompleteSelectionDatabase() {
        User user = service.getUser();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("selection");
        DatabaseReference userReference = firebaseDatabase.getReference("users");
            for (int i = 0; i < selectedPlaces.size(); i++) {
                if (selectedPlaces.get(i).getUserId().size() > 1){
                    for (String userId : selectedPlaces.get(i).getUserId()) {
                        if (user.getId().contains(userId)) {
                            selectedPlace = selectedPlaces.get(i);
                            selectedPlaces.get(i).getUserId().remove(user.getId());
                            databaseReference.child(selectedPlace.getId()).setValue(selectedPlace);
                            user.setChoice(null);
                            userReference.child(user.getId()).child("choice").removeValue();
                            break;
                        }
                    }
                } else {
                    selectedPlaces.remove(selectedPlace);
                    databaseReference.child(selectedPlace.getId()).removeValue();
                    user.setChoice(null);
                    userReference.child(user.getId()).child("choice").removeValue();
                }
            }
    }

    @Override
    public void setUserRadius(String radius) {
        User user = service.getUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(user.getId()).child("radius").setValue(radius);
    }

    @Override
    public void setUserLikedPlaces(List<String> userLikedPlaces) {
        User user = service.getUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(user.getId()).child("likedPlacesId").setValue(userLikedPlaces);
    }
}
