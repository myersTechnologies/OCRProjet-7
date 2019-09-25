package dasilva.marco.go4lunch.service;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;

public class ApiService implements Go4LunchService {

    private PlaceMarker placeMarker;
    private User user;
    private List<PlaceMarker> places;
    private Location location;
    private DataBaseService dataBaseService;
    private String AM = "AM";
    private String PM ="PM";
    private String CLOSED = "Closed";
    private String CLOSED_TODAY = "Closed today";
    private String OPEN24 = "Open 24 hours";
    private String ALWAYS_OPEN = "24/7";

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
    public void setUserRadius(String radius) {
        user.setRadius(radius);
        dataBaseService.setUserRadius(radius);
    }

    @Override
    public void setUserLikedPlaces(List<String> userLikedPlaces) {
        dataBaseService.setUserLikedPlaces(userLikedPlaces);
    }

    //To count how many people are joining
    @Override
    public void countPlaceSelectedByUsers() {
        List<User> users = dataBaseService.getUsersList();
        List<SelectedPlace> selectedPlaces = dataBaseService.getListOfSelectedPlaces();
        for (PlaceMarker marker : places) {
            int count = 0;
            for (User user : users) {
                for (SelectedPlace selectedPlace : selectedPlaces) {
                    for (String userId : selectedPlace.getUserId()) {
                        if (user.getId().equals(userId)) {
                            if (marker.getId().contains(selectedPlace.getId())) {
                                count++;
                                marker.setSelectedTimes(count);
                            }
                        }
                    }
                }
            }
        }
    }

    //To convert Latitude and longitude String to LatLng Class
    @Override
    public LatLng getRealLatLng(SelectedPlace place) {
           String[] coordinates = place.getLatLng().split(",");
           String[] coordinatesLat = coordinates[0].split(Pattern.quote("("));
           String[] coordinatesLng = coordinates[1].split(Pattern.quote(")"));
           String lngString = coordinatesLng[0];
           String latString = coordinatesLat[1];
           double lat = Double.valueOf(latString);
           double lng = Double.valueOf(lngString);
        return new LatLng(lat, lng);
    }

    @Override
    public void countPlacesLikes() {
        List<User> users = dataBaseService.getUsersList();
        for (PlaceMarker marker : places){
            int count = 0;
            for (User user : users){
                if (user.getLikedPlacesId() != null) {
                    for (String likedPlaces : user.getLikedPlacesId()){
                        if (marker.getId().equals(likedPlaces)) {
                            count++;
                            marker.setLikes(count);
                        }
                    }
                }
            }
        }
    }

    private Calendar getCalendar(){
        return Calendar.getInstance();
    }

    private String getDayOfTheWeek(){
        return getCalendar().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
    }

    private List<String> getOpenHourList(PlaceMarker placeMarker){
        return placeMarker.getWeekdayHours();
    }

    //To check if restaurant is open 24/7 or closed today, then if it's not according to Time Now if its AM or PM
    // it could be in double format or in single format
    @Override
    public String getTodayClosingHour(PlaceMarker placeMarker) {
            String openUntil = "";
        if (placeMarker.getWeekdayHours() != null) {
            String dayOfTheWeek = getDayOfTheWeek();
            for (String todays : getOpenHourList(placeMarker)) {
                if (todays.contains(dayOfTheWeek)) {
                    if (!todays.contains(OPEN24)) {
                        if (!todays.contains(CLOSED)) {
                            if (getCalendar().get(Calendar.AM_PM) == Calendar.AM) {
                                try {
                                   openUntil = getClosingHoursDoubleFormatAM(todays);
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    openUntil = getClosingHoursSingleFormatWithoutDay(todays, dayOfTheWeek);
                                }
                            } else {
                                try {
                                   openUntil = getClosingHoursDoubleFormatPM(todays);

                                } catch (ArrayIndexOutOfBoundsException e) {
                                    openUntil = getSimplePMClosingHourFormat(todays, dayOfTheWeek);
                                }
                            }
                        } else {
                            openUntil = CLOSED_TODAY;
                        }
                    } else {
                        openUntil = ALWAYS_OPEN;
                    }

                    break;
                }
            }
        }
        return openUntil;
    }

    //To Format 10:00 - 14:00, 18:00 - 22:00 to get 14:00
    private String getClosingHoursDoubleFormatAM(String todays){
        String[] openHours = todays.split(",");
        String[] openedUntil = openHours[0].split("–");
        Calendar time = Calendar.getInstance();
        time.add(Calendar.HOUR, Integer.parseInt(openedUntil[1].split(":")[0].trim()));
        time.add(Calendar.MINUTE, Integer.parseInt(openedUntil[1].split(":")[1].split(PM)[0].trim()));
        if (getCalendar().getTime().before(time.getTime())){
            return checkClosingHours(openedUntil[1]);
        } else {
            openedUntil = openHours[1].split("–");
            return checkClosingHours(openedUntil[1]);
        }
    }

    //To Format 10:00 - 22:00 to get 22:00
    private String getClosingHoursSingleFormatWithoutDay(String todays, String dayOfTheWeek){
        String[] openHours = todays.split("–");
        String[] openHourWithoutDay = openHours[0].split(dayOfTheWeek + ":");
        return checkClosingHours(openHourWithoutDay[1]);
    }

    //To Format 10:00 - 14:00, 18:00 - 22:00 to get 22:00
    private String getClosingHoursDoubleFormatPM(String todays){
        String[] openHours = todays.split(",");
        String[] openedUntil = openHours[1].split("–");
        Calendar time = getCalendar();
        time.add(Calendar.HOUR, Integer.parseInt(openedUntil[1].split(":")[0].trim()));
        if (openedUntil[1].split(":")[1].contains(PM)) {
            time.add(Calendar.MINUTE, Integer.parseInt(openedUntil[1].split(":")[1].split(PM)[0].trim()));
        } else {
            time.add(Calendar.MINUTE, Integer.parseInt(openedUntil[1].split(":")[1].split(AM)[0].trim()));
        }
        if (getCalendar().getTime().before(time.getTime())){
            return checkClosingHours(openedUntil[1]);
        } else {
            openedUntil = openHours[0].split("–");
            return checkClosingHours(openedUntil[1]);
        }
    }

    //To Format 10:00 - 22:00 to get 22:00
    private String getSimplePMClosingHourFormat(String todays, String dayOfTheWeek){
        String[] openHours = todays.split("–");
        if (openHours.length == 1) {
            String[] openHourWithoutDay = openHours[0].split(dayOfTheWeek + ":");
            return checkClosingHours(openHourWithoutDay[0]);
        } else {
            return checkClosingHours(openHours[1]);

        }
    }

    //Math to  check if restaurant is closing soon, comparing date Now and Google details open hours
    private String checkClosingHours(String openUntil){
        String open;
        int closeHour = Integer.parseInt(openUntil.split(":")[0].trim());
        int closeMinutes;
        if (openUntil.split(":")[1].trim().contains(AM)) {
            closeMinutes = Integer.parseInt(openUntil.split(":")[1].split(AM)[0].trim());
        } else {
            closeMinutes = Integer.parseInt(openUntil.split(":")[1].split(PM)[0].trim());
        }
        Calendar time = Calendar.getInstance();
        int hourNow = time.get(Calendar.HOUR_OF_DAY);
        int minutesNow = time.get(Calendar.MINUTE);
        int timeClose = closeHour * 60 + closeMinutes;
        int currentTime = hourNow * 60 + minutesNow;
        int closing = Math.abs(timeClose - currentTime);
        if (closing <= 60) {
            open = "Closing soon";
        } else {
            open = openUntil;
        }
        return open;
    }

    //For date as 10:00 - 14:00, 18:00 - 22:00 to get 10:00
    private String openHourWithoutDayDoubleFormatAM(String days, String dayOfTheWeek){
        String[] openHours = days.split(",");
        String[] openedUntil = openHours[0].split("–");
        String[] openHourWithoutDay = openedUntil[0].split(dayOfTheWeek + ":");
        String day = openHourWithoutDay[1];
        if (!day.contains(AM)) {
            if (!openHourWithoutDay[1].contains(PM)) {
                return  checkOpenHours(openHourWithoutDay[1] + PM);
            } else {
                return  checkOpenHours(openHourWithoutDay[1]);
            }
        } else {
            return  checkOpenHours(openHourWithoutDay[1]);
        }
    }

    //For date as 10:00 - 22:00 to get 10:00
    private String openHourWithoutDaySingleFormatAM(String days, String dayOfTheWeek){
        String[] openHours = days.split("–");
        String[] openedUntil = openHours[0].split(dayOfTheWeek + ":");
        return checkOpenHours(openedUntil[1]);
    }

    //For date as 10:00 - 14:00, 18:00 - 22:00 to get 18:00
    private String openHourPMDoubleFormat(String days){
        String[] openHours = days.split(",");
        String[] openedUntil = openHours[1].split("–");
        if (!openedUntil[0].contains(AM)) {
            if (!openedUntil[0].contains(PM)) {
                return checkOpenHours(openedUntil[0] + PM);
            } else {
               return checkOpenHours(openedUntil[0]);
            }
        } else {
            return checkOpenHours(openedUntil[0]);
        }
    }

    //To Format 10:00 - 22:00 to get 10:00
    private String openHourSingleDateFormatPM(String days, String dayOfTheWeek){
        String[] openHours = days.split("–");
        String[] openHourWithoutDay = openHours[0].split(dayOfTheWeek + ":");
        if (!openHourWithoutDay[1].contains(AM)) {
            if (!openHourWithoutDay[1].contains(PM)) {
                return checkOpenHours(openHourWithoutDay[1] + PM);
            } else {
                return checkOpenHours(openHourWithoutDay[1]);
            }
        } else {
            return checkOpenHours(openHourWithoutDay[1]);
        }
    }

    //the same logic as getClosingHours method
    @Override
    public String getTodayOpenHour(PlaceMarker placeMarker) {
        String openUntil = "";
        if (placeMarker.getWeekdayHours() != null) {
            String dayOfTheWeek = getDayOfTheWeek();
            for (String days : getOpenHourList(placeMarker)){
                    if (days.contains(dayOfTheWeek)) {
                        if (!days.contains(OPEN24)) {
                            if (!days.contains(CLOSED)) {
                                if (getCalendar().get(Calendar.AM_PM) == Calendar.AM) {
                                    try {
                                        openUntil = openHourWithoutDayDoubleFormatAM(days, dayOfTheWeek);
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        openUntil = openHourWithoutDaySingleFormatAM(days, dayOfTheWeek);
                                    }
                                } else {
                                    try {
                                       openUntil = openHourPMDoubleFormat(days);
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                      openUntil= openHourSingleDateFormatPM(days, dayOfTheWeek);
                                    }
                                }
                            } else {
                                openUntil = CLOSED_TODAY;
                            }
                        } else {
                            openUntil = ALWAYS_OPEN;
                        }
                        break;
                    }
            }
        }

        return openUntil;
    }

    //Math to check if restaurant is opening soon
    //between 660 and 720 for pm and below 60 for am
    private String checkOpenHours(String openUntil){
        String open;
        int openHour = Integer.parseInt(openUntil.split(":")[0].trim());
        int openMinutes;
        if (openUntil.split(":")[1].trim().contains(AM)) {
            openMinutes = Integer.parseInt(openUntil.split(":")[1].split(AM)[0].trim());
        } else {
            openMinutes = Integer.parseInt(openUntil.split(":")[1].split(PM)[0].trim());
        }
        Calendar time = Calendar.getInstance();
        int hourNow = time.get(Calendar.HOUR_OF_DAY);
        int minutesNow = time.get(Calendar.MINUTE);
        int timeOpen = openHour * 60 + openMinutes;
        int currentTime = hourNow * 60 + minutesNow;
        int opening = Math.abs(currentTime - timeOpen);
        Log.d("OpeningMath", String.valueOf(opening));
        if (opening >= 660 && opening <= 720 || opening <= 60) {
            open = "Opening soon";
        } else {
            open = openUntil;
        }
        return open;

    }

    @Override
    public void setDataBase(DataBaseService dataBase) {
        this.dataBaseService = dataBase;
    }

    @Override
    public void setUserLunchChoice(PlaceMarker placeMarker, Context context) {
        getAdress(placeMarker, context);

    }

    //get adress from geocoder in case if place marker as not one
    private void getAdress(PlaceMarker placeMarker, Context context){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(placeMarker.getLatLng().latitude, placeMarker.getLatLng().longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = null;
        if (addresses != null) {
            address = addresses.get(0).getAddressLine(0);
        }
        String city = null;
        if (addresses != null) {
            city = addresses.get(0).getLocality();
        }
        String country = null;
        if (addresses != null) {
            country = addresses.get(0).getCountryName();
        }
        placeMarker.setAdress(address + ", " + city + ", " + country);
    }

}
