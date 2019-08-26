package dasilva.marco.go4lunch.ui.map.utils.nearby;


import com.google.gson.JsonIOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DataParser {

    public HashMap<String, String> getPlace(JSONObject googlePlaceJson){
        HashMap<String, String > googlePlaceMap = new HashMap<>();

        String placeId = "-NA-";

        try {

            if (!googlePlaceJson.isNull("place_id")){
                placeId = googlePlaceJson.getString("place_id");
            }

            googlePlaceMap.put("place_id", placeId);

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
