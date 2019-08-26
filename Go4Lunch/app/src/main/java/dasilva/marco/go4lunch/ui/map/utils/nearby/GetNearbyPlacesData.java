package dasilva.marco.go4lunch.ui.map.utils.nearby;


import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import dasilva.marco.go4lunch.BuildConfig;
import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.ui.map.utils.DownloadUrl;
import dasilva.marco.go4lunch.ui.map.utils.details.PlaceDetailsTask;


public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private Context context;
    private static final String API_KEY = BuildConfig.GOOGLEAPIKEY;

    @Override
    protected String doInBackground(Object... objects) {
        String url = (String) objects[0];
        context = (Context)objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();

        try {
            googlePlacesData = downloadUrl.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList){
        for (int i = 0; i < nearbyPlaceList.size(); i++) {
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);
            setPlaceMarkerInfo(googlePlace);
        }
    }

    private void setPlaceMarkerInfo(HashMap<String, String> googlePlace){
        PlaceMarker placeMarker = new PlaceMarker();
        placeMarker.setId(googlePlace.get("place_id"));
        String uri = context.getString(R.string.url_begin) + placeMarker.getId() +
                context.getString(R.string.and_key) + API_KEY;
        Object dataTransfer[] = new Object[3];
        dataTransfer[0] = uri;
        dataTransfer[1] = placeMarker;
        dataTransfer[2] = context;

        PlaceDetailsTask getNearbyPlacesData = new PlaceDetailsTask();
        getNearbyPlacesData.execute(dataTransfer);

    }
}
