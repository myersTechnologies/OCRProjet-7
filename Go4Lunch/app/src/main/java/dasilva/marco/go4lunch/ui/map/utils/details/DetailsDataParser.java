package dasilva.marco.go4lunch.ui.map.utils.details;

import android.util.Log;

import com.google.gson.JsonIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailsDataParser {

    public List<HashMap<String, String>> parse(String string){

        JSONObject jsonObject;
        JSONObject jResult = null;

        try {
            jsonObject = new JSONObject(string);
            jResult = jsonObject.getJSONObject("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jResult);
    }

    private List<HashMap<String, String>> getPlaces(JSONObject jsonObject){
        List<HashMap<String, String>> placesList = new ArrayList<>();
        try {
            int count = jsonObject.length();
            HashMap<String, String> placeMap;

            for (int i = 0; i < count; i++) {
                placeMap = getPlace(jsonObject);
                placesList.add(placeMap);
            }
        } catch (NullPointerException e){}

        return placesList;
    }


    public HashMap<String, String> getPlace(JSONObject googleDetailsJson) {
        HashMap<String, String> googleDetailsMap = new HashMap<>();

        String weekDayText;
        String phoneNumber;
        String website;
        String photo = "-NA-";
        String vicinity = "-NA-";
        String latitude;
        String longitude;
        String placeName = "-NA-";
        boolean opened = false;
        
        try {

            if (!googleDetailsJson.isNull("name")) {
                placeName = googleDetailsJson.getString("name");
            }


            if (!googleDetailsJson.isNull("vicinity")){
                vicinity = googleDetailsJson.getString("vicinity");
            }

            latitude = googleDetailsJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googleDetailsJson.getJSONObject("geometry").getJSONObject("location").getString("lng");


            if (!googleDetailsJson.isNull("opening_hours")) {
                JSONObject openingHours = googleDetailsJson.getJSONObject("opening_hours");
                opened = openingHours.getBoolean("open_now");
            }


            if (!googleDetailsJson.isNull("formatted_phone_number")) {
                phoneNumber = googleDetailsJson.getString("formatted_phone_number");
            } else {
                phoneNumber = " ";
            }

            if (!googleDetailsJson.isNull("website")) {
                website = googleDetailsJson.getString("website");
                googleDetailsMap.put("website", website);
            } else {
                website = " ";
            }

            JSONObject jsonObject = googleDetailsJson.getJSONObject("opening_hours");
            weekDayText = jsonObject.getJSONArray("weekday_text").toString();

            if(!googleDetailsJson.isNull("photos")){
                JSONArray photos = googleDetailsJson.getJSONArray("photos");
                photo = ((JSONObject)photos.get(1)).getString("photo_reference");
            }

            googleDetailsMap.put("place_name", placeName);
            googleDetailsMap.put("phone_number", phoneNumber);
            googleDetailsMap.put("website", website);
            googleDetailsMap.put("weekday_text", weekDayText);
            googleDetailsMap.put("photo_reference", photo);
            googleDetailsMap.put("vicinity", vicinity);
            googleDetailsMap.put("lat", latitude);
            googleDetailsMap.put("lng", longitude);
            googleDetailsMap.put("open_now", String.valueOf(opened));


        } catch (JsonIOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googleDetailsMap;
    }
}
