package dasilva.marco.go4lunch.ui.map.utils;


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
        try {

            try {
                phoneNumber = googleDetailsJson.getString("formatted_phone_number");

            } catch (Exception e){
                phoneNumber = " ";
            }

            try {
                website = googleDetailsJson.getString("website");
                googleDetailsMap.put("website", website);
            } catch (Exception e){
                website = " ";
            }

            JSONObject jsonObject = googleDetailsJson.getJSONObject("opening_hours");
            weekDayText = jsonObject.getJSONArray("weekday_text").toString();

            if(!googleDetailsJson.isNull("photos")){
                JSONArray photos = googleDetailsJson.getJSONArray("photos");
                for(int i=0;i<photos.length();i++){
                    photo = ((JSONObject)photos.get(i)).getString("photo_reference");
                }
            } else{
                Log.d("PHOTONULL", "no photo");
            }

            googleDetailsMap.put("phone_number", phoneNumber);
            googleDetailsMap.put("website", website);
            googleDetailsMap.put("weekday_text", weekDayText);
            googleDetailsMap.put("photo_reference", photo);
        } catch (JsonIOException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googleDetailsMap;
    }
}
