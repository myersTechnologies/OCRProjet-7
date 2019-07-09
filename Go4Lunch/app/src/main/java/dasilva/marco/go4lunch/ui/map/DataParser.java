package dasilva.marco.go4lunch.ui.map;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class DataParser {

    public HashMap<String, String> getPlace(JSONObject googlePlaceJson){
        HashMap<String, String > googlePlaceMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String placeId = "-NA-";
        boolean opened = false;
        String rate ="";
        String photo = "-NA-";
        Go4LunchService service = DI.getService();
        List<PlaceMarker> places = new ArrayList<>();


        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")){
                vicinity = googlePlaceJson.getString("vicinity");
            }
            if (!googlePlaceJson.isNull("place_id")){
                placeId = googlePlaceJson.getString("place_id");
            }

            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            if(!googlePlaceJson.isNull("photos")){
                JSONArray photos = googlePlaceJson.getJSONArray("photos");
                for(int i=0;i<photos.length();i++){
                    photo = ((JSONObject)photos.get(i)).getString("photo_reference");
                }
            }

            JSONObject openingHours = googlePlaceJson.getJSONObject("opening_hours");
            opened = openingHours.getBoolean("open_now");

            rate = googlePlaceJson.getString("rating");

            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("place_id", placeId);
            googlePlaceMap.put("rating", rate);
            googlePlaceMap.put("photo_reference", photo);
            googlePlaceMap.put("open_now", String.valueOf(opened));

        } catch (JsonIOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlaceMap;
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray){
        int count = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placemap = null;

        for (int i = 0; i < count; i++){
            try {
                placemap = getPlace((JSONObject) jsonArray.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            placesList.add(placemap);
        }

        return placesList;
    }

    public List<HashMap<String, String>> parse(String string){
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(string);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }



}
