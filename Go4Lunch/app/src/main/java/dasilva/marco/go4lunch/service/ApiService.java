package dasilva.marco.go4lunch.service;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

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

    @Override
    public String getTodayClosingHour(PlaceMarker placeMarker) {
            String openUntil = "";
        if (placeMarker.getWeekdayHours() != null) {
            Calendar calendar = Calendar.getInstance();
            String dayOfTheWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
            List<String> openHourList = placeMarker.getWeekdayHours();
            for (String todays : openHourList) {
                if (todays.contains(dayOfTheWeek)) {
                    if (!todays.contains(OPEN24)) {
                        if (!todays.contains(CLOSED)) {
                            if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                                try {
                                    String[] openHours = todays.split(",");
                                    String[] openedUntil = openHours[0].split("–");
                                    Calendar time = Calendar.getInstance();
                                    time.add(Calendar.HOUR, Integer.parseInt(openedUntil[1].split(":")[0].trim()));
                                    time.add(Calendar.MINUTE, Integer.parseInt(openedUntil[1].split(":")[1].split(PM)[0].trim()));
                                    if (calendar.getTime().before(time.getTime())){
                                        openUntil = checkClosingHours(openedUntil[1]);
                                    } else {
                                        openedUntil = openHours[1].split("–");
                                        openUntil = checkClosingHours(openedUntil[1]);
                                    }
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    String[] openHours = todays.split("–");
                                    String[] openHourWithoutDay = openHours[0].split(dayOfTheWeek + ":");
                                    openUntil = checkClosingHours(openHourWithoutDay[1]);
                                }
                            } else {
                                try {
                                    String[] openHours = todays.split(",");
                                    String[] openedUntil = openHours[1].split("–");
                                    Calendar time = Calendar.getInstance();
                                    time.add(Calendar.HOUR, Integer.parseInt(openedUntil[1].split(":")[0].trim()));
                                    if (openedUntil[1].split(":")[1].contains(PM)) {
                                        time.add(Calendar.MINUTE, Integer.parseInt(openedUntil[1].split(":")[1].split(PM)[0].trim()));
                                    } else {
                                        time.add(Calendar.MINUTE, Integer.parseInt(openedUntil[1].split(":")[1].split(AM)[0].trim()));
                                    }
                                    if (calendar.getTime().before(time.getTime())){
                                        openUntil = checkClosingHours(openedUntil[1]);
                                    } else {
                                        openedUntil = openHours[0].split("–");
                                        openUntil = checkClosingHours(openedUntil[1]);
                                    }

                                } catch (ArrayIndexOutOfBoundsException e) {
                                    String[] openHours = todays.split("–");
                                    if (openHours.length == 1) {
                                        String[] openHourWithoutDay = openHours[0].split(dayOfTheWeek + ":");
                                        openUntil = checkClosingHours(openHourWithoutDay[0]);
                                    } else {
                                        openUntil = checkClosingHours(openHours[1]);

                                    }
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

    @Override
    public String getTodayOpenHour(PlaceMarker placeMarker) {
        String openUntil = "";
        if (placeMarker.getWeekdayHours() != null) {
            Calendar calendar = Calendar.getInstance();
            String dayOfTheWeek = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH);
            List<String> openHourList = placeMarker.getWeekdayHours();
            for (String days : openHourList){
                    if (days.contains(dayOfTheWeek)) {
                        if (!days.contains(OPEN24)) {
                            if (!days.contains(CLOSED)) {
                                if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
                                    try {
                                        String[] openHours = days.split(",");
                                        String[] openedUntil = openHours[0].split("–");
                                        String[] openHourWithoutDay = openedUntil[0].split(dayOfTheWeek + ":");
                                        if (!openHourWithoutDay[1].contains(AM)) {
                                            if (!openHourWithoutDay[1].contains(PM)) {
                                                openUntil = checkOpenHours(openHourWithoutDay[1] + PM);
                                            } else {
                                                openUntil = checkOpenHours(openHourWithoutDay[1]);
                                            }
                                        } else {
                                            openUntil = checkOpenHours(openHourWithoutDay[1]);
                                        }
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        String[] openHours = days.split("–");
                                        String[] openedUntil = openHours[0].split(dayOfTheWeek + ":");
                                        openUntil = checkOpenHours(openedUntil[1]);
                                    }
                                } else {
                                    try {
                                        String[] openHours = days.split(",");
                                        String[] openedUntil = openHours[1].split("–");
                                        if (!openedUntil[0].contains(AM)) {
                                            if (!openedUntil[0].contains(PM)) {
                                                openUntil = checkOpenHours(openedUntil[0] + PM);
                                            } else {
                                                openUntil = checkOpenHours(openedUntil[0]);
                                            }
                                        } else {
                                            openUntil = checkOpenHours(openedUntil[0]);
                                        }

                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        String[] openHours = days.split("–");
                                        String[] openHourWithoutDay = openHours[0].split(dayOfTheWeek + ":");
                                        if (!openHourWithoutDay[1].contains(AM)) {
                                            if (!openHourWithoutDay[1].contains(PM)) {
                                                openUntil = checkOpenHours(openHourWithoutDay[1] + PM);
                                            } else {
                                                openUntil = checkOpenHours(openHourWithoutDay[1]);
                                            }
                                        } else {
                                            openUntil = checkOpenHours(openHourWithoutDay[1]);
                                        }
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
        if (opening >= 660 && opening <= 720) {
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
