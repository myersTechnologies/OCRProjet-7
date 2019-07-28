package dasilva.marco.go4lunch.firebase;

import java.util.List;

import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;

public interface DataBaseService {

    void setUsersList();
    List<User> getUsersList();
    void deleteUserFromFireBase();
    List<SelectedPlace> getListOfSelectedPlaces();
    void setListOfSelectedPlaces();
    void removeCompleteSelectionDatabase();
    void setUserRadius(String radius);
    void setUserLikedPlaces(String userLikedPlaces);


}
