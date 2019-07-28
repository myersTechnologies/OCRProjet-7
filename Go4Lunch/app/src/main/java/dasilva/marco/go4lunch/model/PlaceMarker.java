package dasilva.marco.go4lunch.model;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;


public class PlaceMarker {

    private String id;
    private LatLng latLng;
    private String adress;
    private String city;
    private String country;
    private String name;
    private String telephone;
    private String webSite;
    private String photoUrl;
    private boolean openingHours;
    private int likes = 0;
    private int selectedTimes = 0;
    private List<String> weekdayList;

    public PlaceMarker(){

    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getOpeninHours() {
        return openingHours;
    }

    public void setOpeningHours(boolean openingHours) {
        this.openingHours = openingHours;
    }

    public String getPhotoUrl() {
        String url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoUrl
                + "&key=" + "AIzaSyDKZnjJaY7UQxDrXsskimpfMb_vY4s6ltc";
        return url;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }


    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdress() {
        String[] newAdress = adress.split(",");
        adress = newAdress[0];
        return adress;
    }

    public String getCity() {
        String[] refarctorAdress = adress.split(",");
        city = refarctorAdress[1];
        return city;
    }

    public String getCountry() {
        return country;
    }



    public String getTelephone() {
        return telephone;
    }

    public String getWebSite() {
        return webSite;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getSelectedTimes() {
        return selectedTimes;
    }

    public void setSelectedTimes(int selectedTimes) {
        this.selectedTimes = selectedTimes;
    }

    public void addWeekToList(String day){
        if (weekdayList == null) {
            weekdayList = new ArrayList<String>();
        }
        weekdayList.add(day);

    }
    public List<String> getWeekdayHous() {
        return weekdayList;
    }


}
