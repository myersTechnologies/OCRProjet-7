package dasilva.marco.go4lunch.service;


import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class Go4LunchTest {

    private User user;

    @Before
    public void setUp(){
    }

    @Test
    public void checkIfUserIsAddedWithSuccess(){
        user = new User("lmp25klo", "Marco da Silva", "marco@gmail.com", "imageUrl");
        List<User> users = new ArrayList<>();
        users.add(user);
        assertTrue(users.get(0).getId().equals(user.getId()));

    }

    @Test
    public void checkIfPlaceMarkerIsAddedWithSuccessToList(){
        PlaceMarker placeMarker = new PlaceMarker(null);
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        List<PlaceMarker> placeMarkers = new ArrayList<>();
        placeMarkers.add(placeMarker);
        assertEquals(placeMarkers.get(0).getId(), placeMarker.getId());
    }

    @Test
    public void checkIfSelectedPlaceIsAddedWithSucess(){
        SelectedPlace selectedPlace = new SelectedPlace("1", "new selected place", "12.30, 1420", "5");
        List<SelectedPlace> selectedPlaces = new ArrayList<>();
        selectedPlaces.add(selectedPlace);
        assertEquals(selectedPlaces.get(0).getId(), selectedPlace.getId());
    }

    @Test
    public void countSelectedPlacesShouldGiveNumberOfUsersLikes(){
        List<PlaceMarker> places = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<SelectedPlace> selectedPlaces = new ArrayList<>();

        User currentUser = new User("5", "Marco", "marco@gmail.com", "https://jhjhjhd.png");
        users.add(currentUser);

        PlaceMarker placeMarker = new PlaceMarker(null);
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        places.add(placeMarker);

        SelectedPlace selected = new SelectedPlace("1", "new place marker", "12.30, 1420", "5");
        selectedPlaces.add(selected);

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

        assertEquals(places.get(0).getSelectedTimes(), 1);
    }

    @Test
    public void countRestaurantLikesFromUser(){
        List<PlaceMarker> places = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<SelectedPlace> selectedPlaces = new ArrayList<>();

        SelectedPlace selected = new SelectedPlace("1", "new place marker", "12.30, 1420", "5");
        selectedPlaces.add(selected);

        PlaceMarker placeMarker = new PlaceMarker(null);
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        places.add(placeMarker);

        User currentUser = new User("5", "Marco", "marco@gmail.com", "https://jhjhjhd.png");
        currentUser.setLikedPlacesId("1");
        users.add(currentUser);


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

        assertEquals(places.get(0).getLikes(), 1);
    }

    @Test
    public void getSplittedRealLatLng(){
        SelectedPlace place = new SelectedPlace("1", "Etablishment", "lat/lng: (46.794237, 4.848902)", "5");
        String[] coordinates = place.getLatLng().split(",");
        String[] coordinatesLat = coordinates[0].split(Pattern.quote("("));
        String[] coordinatesLng = coordinates[1].split(Pattern.quote(")"));
        String lngString = coordinatesLng[0];
        String latString = coordinatesLat[1];
        double lat = Double.valueOf(latString);
        double lng = Double.valueOf(lngString);
        LatLng realLatLng = new LatLng(lat, lng);
        assertEquals(realLatLng.latitude + ", " + realLatLng.longitude, "46.794237, 4.848902");
    }

    @Test
    public void setUserChoiceWithSuccess(){
        List<SelectedPlace> selectedPlaces = new ArrayList<>();
        List<User> users = new ArrayList<>();

        User currentUser = new User("5", "Marco", "marco@gmail.com", "https://jhjhjhd.png");
        users.add(currentUser);
        currentUser.setChoice("Etablishment");

        SelectedPlace place = new SelectedPlace("1", "Etablishment", "lat/lng: (46.794237, 4.848902)", "5");
        selectedPlaces.add(place);
        place.setUserId("5");

        assertEquals(selectedPlaces.get(0).getUserId(), "5");
    }

    @Test
    public void deletingLunchShouldRemoveLunchChoiceFromUserAndRemoveItFromTheList(){
        List<SelectedPlace> selectedPlaces = new ArrayList<>();
        List<User> users = new ArrayList<>();

        User currentUser = new User("5", "Marco", "marco@gmail.com", "https://jhjhjhd.png");
        users.add(currentUser);
        currentUser.setChoice("Etablishment");
        SelectedPlace place = new SelectedPlace("1", "Etablishment", "lat/lng: (46.794237, 4.848902)", "5");
        selectedPlaces.add(place);
        place.setUserId("5");

        selectedPlaces.remove(place);
        currentUser.setChoice(null);

        assertTrue(selectedPlaces.isEmpty());
        assertTrue(currentUser.getChoice() == null);

    }

    @Test
    public void deleteUserIdFromSelectedPlace(){
        List<SelectedPlace> selectedPlaces = new ArrayList<>();
        List<User> users = new ArrayList<>();

        User currentUser = new User("5", "Marco", "marco@gmail.com", "https://jhjhjhd.png");
        user = new User("3", "André", "andre@gmail.com", "https://picture.png");
        users.add(currentUser);
        users.add(user);
        user.setChoice("Etablishment");
        currentUser.setChoice("Etablishment");

        SelectedPlace place = new SelectedPlace("1", "Etablishment", "lat/lng: (46.794237, 4.848902)", "5");
        selectedPlaces.add(place);
        place.setUserId("3,5");

        if (selectedPlaces.size() > 0) {
            for (int i = 0; i < selectedPlaces.size(); i++) {
                for (String userId : selectedPlaces.get(i).getUserId().split(",")) {
                    if (currentUser.getId().contains(userId)) {
                        place = selectedPlaces.get(i);
                    }
                }
            }
        }

        String[] places = place.getUserId().split(",");
        if (places[0].contains(user.getId())){
            String usersId = place.getUserId().replace(user.getId() + ",", "");
            place.setUserId(usersId);
            user.setChoice(null);
        } else {
            String usersId = place.getUserId().replace("," + user.getId(), "");
            place.setUserId(usersId);
            user.setChoice(null);
        }

        assertEquals(selectedPlaces.get(0).getUserId(), "5");
        assertEquals(user.getChoice(), null);
    }

    @Test
    public void checkIfDistanceIsCorrect(){


    }
}