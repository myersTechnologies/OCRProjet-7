package dasilva.marco.go4lunch.service;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;

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
    public void setUp() {
    }

    @Test
    public void checkTimeOpening(){
        int time1 = 10 * 60 + 30;
        int time2 = 11 * 60 + 30;
        assertEquals(Math.abs(time1 - time2), 60 );
        if (Math.abs(time2 - time1) >= 60 && Math.abs(time2 - time1) <=61) {
            assertTrue(true);
        }
    }

    @Test
    public void checkIfUserIsAddedWithSuccess() {
        user = new User("1", "Marco da Silva", "marco@gmail.com", "https://picture.png");
        List<User> users = new ArrayList<>();
        users.add(user);
        assertTrue(users.get(0).getId().equals("1"));

    }

    @Test
    public void checkIfPlaceMarkerIsAddedWithSuccessToList() {
        PlaceMarker placeMarker = new PlaceMarker();
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        List<PlaceMarker> placeMarkers = new ArrayList<>();
        placeMarkers.add(placeMarker);
        assertEquals(placeMarkers.get(0).getId(), placeMarker.getId());
    }

    @Test
    public void checkIfSelectedPlaceIsAddedWithSucess() {
        SelectedPlace selectedPlace = new SelectedPlace("1", "new selected place", "12.30, 1420");
        selectedPlace.setUserId("5");
        List<SelectedPlace> selectedPlaces = new ArrayList<>();
        selectedPlaces.add(selectedPlace);
        assertEquals(selectedPlaces.get(0).getId(), selectedPlace.getId());
    }

    @Test
    public void countSelectedPlacesShouldGiveNumberOfUsersLikes() {
        List<PlaceMarker> places = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<SelectedPlace> selectedPlaces = new ArrayList<>();

        User currentUser = new User("5", "Marco", "marco@gmail.com", "https://picture.png");
        users.add(currentUser);

        PlaceMarker placeMarker = new PlaceMarker();
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        places.add(placeMarker);

        SelectedPlace selected = new SelectedPlace("1", "new place marker", "12.30, 1420");
        selected.setUserId("5");
        selectedPlaces.add(selected);

        for (PlaceMarker marker : places) {
            int count = 0;
            for (User user : users) {
                for (SelectedPlace selectedPlace : selectedPlaces) {
                    for (String userId : selectedPlace.getUserId())
                        if (user.getId().compareTo(userId) == 0) {
                            if (marker.getId().contains(selectedPlace.getId())) {
                                count++;
                                marker.setSelectedTimes(count);
                            }
                        }
                }
            }
        }

        assertEquals(places.get(0).getSelectedTimes(), 1);
    }

    @Test
    public void countRestaurantLikesFromUser() {
        List<PlaceMarker> places = new ArrayList<>();
        List<User> users = new ArrayList<>();
        List<SelectedPlace> selectedPlaces = new ArrayList<>();

        SelectedPlace selected = new SelectedPlace("1", "new place marker", "12.30, 1420");
        selected.setUserId("5");
        selectedPlaces.add(selected);

        PlaceMarker placeMarker = new PlaceMarker();
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        places.add(placeMarker);

        User currentUser = new User("5", "Marco", "marco@gmail.com", "https://picture.png");
        currentUser.setLikedPlacesId("1");
        users.add(currentUser);


        for (PlaceMarker marker : places) {
            int count = 0;
            for (User user : users) {
                if (user.getLikedPlacesId() != null) {
                    for (String likedPlaces : user.getLikedPlacesId()) {
                        if (marker.getId().equals(likedPlaces)) {
                            count++;
                            marker.setLikes(count);
                        }
                    }
                }
            }
        }

        assertEquals(places.get(0).getLikes(), 1);
    }

    @Test
    public void getSplittedRealLatLng() {
        SelectedPlace place = new SelectedPlace("1", "Etablishment", "lat/lng: (46.794237, 4.848902)");
        place.setUserId("5");
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
    public void setUserChoiceWithSuccess() {
        List<SelectedPlace> selectedPlaces = new ArrayList<>();
        List<User> users = new ArrayList<>();

        User currentUser = new User("5", "Marco", "marco@gmail.com", "https://picture.png");
        users.add(currentUser);
        currentUser.setChoice("Etablishment");

        SelectedPlace place = new SelectedPlace("1", "Etablishment", "lat/lng: (46.794237, 4.848902)");
        place.setUserId("5");
        selectedPlaces.add(place);

        assertEquals(selectedPlaces.get(0).getUserId().get(0), "5");
    }

    @Test
    public void deletingLunchShouldRemoveLunchChoiceFromUserAndRemoveItFromTheList() {
        List<SelectedPlace> selectedPlaces = new ArrayList<>();
        List<User> users = new ArrayList<>();

        User currentUser = new User("5", "Marco", "marco@gmail.com", "url.png");
        users.add(currentUser);
        currentUser.setChoice("Etablishment");
        SelectedPlace place = new SelectedPlace("1", "Etablishment", "lat/lng: (46.794237, 4.848902)");
        place.setUserId("5");
        selectedPlaces.add(place);

        selectedPlaces.remove(place);
        currentUser.setChoice(null);

        assertTrue(selectedPlaces.isEmpty());
        assertTrue(currentUser.getChoice() == null);

    }

    @Test
    public void deleteUserIdFromSelectedPlace() {
        List<SelectedPlace> selectedPlaces = new ArrayList<>();
        List<User> users = new ArrayList<>();

        User currentUser = new User("5", "Marco", "marco@gmail.com", "https://picture.png");
        user = new User("3", "André", "andre@gmail.com", "https://picture.png");
        users.add(currentUser);
        users.add(user);
        user.setChoice("Etablishment");
        currentUser.setChoice("Etablishment");

        SelectedPlace place = new SelectedPlace("1", "Etablishment", "lat/lng: (46.794237, 4.848902)");
        place.setUserId("3");
        place.setUserId("5");
        selectedPlaces.add(place);

        for (int i = 0; i < selectedPlaces.size(); i++) {
            if (selectedPlaces.get(i).getUserId().size() > 1){
                selectedPlaces.get(i).getUserId().remove(user.getId());
                user.setChoice(null);
                break;
            } else {
                selectedPlaces.remove(selectedPlaces.get(i));
                user.setChoice(null);
            }
        }

        assertEquals(selectedPlaces.get(0).getUserId().get(0), "5");
        assertEquals(user.getChoice(), null);
    }

    @Test
    public void checkIfDistanceIsCorrect() {
        Location current = Mockito.mock(Location.class);
        Location target = Mockito.mock(Location.class);
        Mockito.when(target.distanceTo(current)).thenReturn(Float.parseFloat("194"));
        double actual = target.distanceTo(current);
        String distance = String.valueOf(actual);
        String[] distanceSeparator = distance.split("\\.");
        String placeDistance = distanceSeparator[0];
        assertEquals(placeDistance, String.valueOf(194));
    }

    @Test
    public void getAmClosingHour() {
        String openUntil = "";

        PlaceMarker placeMarker = new PlaceMarker();
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        placeMarker.addWeekToList("Sunday: 09:00 – 14:30");

        if (placeMarker.getWeekdayHours() != null) {
            Calendar calendar = Mockito.mock(Calendar.class);
            calendar.set(2019, Calendar.JULY, 22, 8, 0);
            Mockito.when(Calendar.getInstance()).thenCallRealMethod();
            Mockito.when(calendar.get(Calendar.DAY_OF_WEEK)).thenReturn(1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEEE");
            Mockito.when(dateFormat.format(calendar.getTime())).thenCallRealMethod();
            String dayOfTheWeek = dateFormat.format(calendar.getTime());
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            List<String> openHourList = placeMarker.getWeekdayHours();
            int count = 0;
            for (String today : openHourList) {
                count++;
                if (count == day) {
                    if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                        try {
                            String[] openHours = today.split(",");
                            String[] openedUntil = openHours[0].split("–");
                            openUntil = openedUntil[1];
                        } catch (ArrayIndexOutOfBoundsException e) {
                            String[] openHours = today.split("–");
                            String[] openHourWithoutDay = openHours[0].split(dayOfTheWeek + ":");

                            openUntil = openHourWithoutDay[1];
                        }
                    }
                }
            }
        }

        assertEquals(openUntil, " 14:30");

    }

    @Test
    public void getPmClosingHoursHours() {
        String openUntil = "";

        PlaceMarker placeMarker = new PlaceMarker();
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        placeMarker.addWeekToList("Sunday: 18:00 – 00:30");

        if (placeMarker.getWeekdayHours() != null) {
            Calendar calendar = Mockito.mock(Calendar.class);
            calendar.set(2019, Calendar.JULY, 22, 15, 0);
            Mockito.when(Calendar.getInstance()).thenCallRealMethod();
            Mockito.when(calendar.get(Calendar.DAY_OF_WEEK)).thenReturn(1);
            Mockito.when(calendar.get(Calendar.AM_PM)).thenReturn(Calendar.PM);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEEE");
            Mockito.when(dateFormat.format(calendar.getTime())).thenCallRealMethod();
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            List<String> openHourList = placeMarker.getWeekdayHours();
            int count = 0;
            for (String today : openHourList) {
                count++;
                if (count == day) {
                    if (calendar.get(Calendar.AM_PM) == Calendar.PM) {
                        try {
                            String[] openHours = today.split(",");
                            String[] openedUntil = openHours[1].split("–");
                            openUntil = openedUntil[1];
                        } catch (ArrayIndexOutOfBoundsException e) {
                            String[] openHours = today.split("–");
                            openUntil = openHours[1];
                        }
                    }
                }
            }
        }
        assertEquals(" 00:30", openUntil);
    }
    @Test
    public void getAmOpeningHour(){
        String openUntil = "";

        PlaceMarker placeMarker = new PlaceMarker();
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        placeMarker.addWeekToList("Sunday: 09:00 – 14:30");

        if (placeMarker.getWeekdayHours() != null) {
            Calendar calendar = Mockito.mock(Calendar.class);
            calendar.set(2019, Calendar.JULY, 22, 8, 0);
            Mockito.when(Calendar.getInstance()).thenCallRealMethod();
            Mockito.when(calendar.get(Calendar.DAY_OF_WEEK)).thenReturn(1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEEE");
            Mockito.when(dateFormat.format(calendar.getTime())).thenCallRealMethod();
            String dayOfTheWeek = "Sunday";
            int day = 1;
            List<String> openHourList = placeMarker.getWeekdayHours();
            int count = 0;
            for (String today : openHourList) {
                count++;
                if (count == day) {
                    if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                        try {
                            String[] openHours = today.split(",");
                            String[] openedUntil = openHours[0].split("–");
                            String [] openHourWithoutDay = openedUntil[0].split(dayOfTheWeek + ":");
                            openUntil = openHourWithoutDay[1];
                        } catch (ArrayIndexOutOfBoundsException e){
                            String[] openHours = today.split("–");
                            String[] openedUntil = openHours[0].split(dayOfTheWeek + ":");
                            openUntil = openedUntil[0];
                        }
                    }
                }
            }
        }
        assertEquals(" 09:00 ", openUntil);
    }
    @Test
    public void getPmOpeningHour(){
        String openUntil = "";

        PlaceMarker placeMarker = new PlaceMarker();
        placeMarker.setName("new place marker");
        placeMarker.setId("1");
        placeMarker.addWeekToList("Sunday: 18:30 – 00:30");

        if (placeMarker.getWeekdayHours() != null) {
            Calendar calendar = Mockito.mock(Calendar.class);
            calendar.set(2019, Calendar.JULY, 22, 8, 0);
            Mockito.when(Calendar.getInstance()).thenCallRealMethod();
            Mockito.when(calendar.get(Calendar.AM_PM)).thenReturn(Calendar.PM);
            Mockito.when(calendar.get(Calendar.DAY_OF_WEEK)).thenReturn(1);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEEE");
            Mockito.when(dateFormat.format(calendar.getTime())).thenCallRealMethod();
            String dayOfTheWeek = "Sunday";
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            List<String> openHourList = placeMarker.getWeekdayHours();
            int count = 0;
            for (String today : openHourList) {
                count++;
                if (count == day) {
                    if (calendar.get(Calendar.AM_PM) == Calendar.PM) {
                        try {
                            String[] openHours = today.split(",");
                            String[] openedUntil = openHours[0].split("–");
                            String [] openHourWithoutDay = openedUntil[0].split(dayOfTheWeek + ":");
                            openUntil = openHourWithoutDay[1];
                        } catch (ArrayIndexOutOfBoundsException e){
                            String[] openHours = today.split("–");
                            String[] openedUntil = openHours[0].split(dayOfTheWeek + ":");
                            openUntil = openedUntil[1];
                        }
                    }
                }
            }
        }
        assertEquals(" 18:30 ", openUntil);
    }

}