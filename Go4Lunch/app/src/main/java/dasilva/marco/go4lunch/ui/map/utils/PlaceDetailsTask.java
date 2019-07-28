package dasilva.marco.go4lunch.ui.map.utils;


import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import dasilva.marco.go4lunch.model.PlaceMarker;

public class PlaceDetailsTask extends AsyncTask<Object, String, String> {

    private PlaceMarker placeMarker;
    private String googleDetailsData;



    protected String doInBackground(Object... objects) {

        String jSonUrl = (String) objects[0];
        placeMarker = (PlaceMarker)objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googleDetailsData = downloadUrl.readUrl(jSonUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleDetailsData;

    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null){
            List<HashMap<String, String>> detailsPlaceList;
            DetailsDataParser parser = new DetailsDataParser();
            detailsPlaceList = parser.parse(result);
            setPlaceMarkerMoreInfo(detailsPlaceList);
        }
    }

    private void setPlaceMarkerMoreInfo(List<HashMap<String, String>> detailsPlaceList){
        for (int i = 0; i < detailsPlaceList.size(); i++){
            HashMap<String, String> googleDetails = detailsPlaceList.get(i);

            placeMarker.setTelephone(googleDetails.get("formatted_phone_number"));

            placeMarker.setWebSite(googleDetails.get("website"));

            String[] openHours = googleDetails.get("weekday_text").split("\"");

            placeMarker.addWeekToList(openHours[1]);
            placeMarker.addWeekToList(openHours[3]);
            placeMarker.addWeekToList(openHours[5]);
            placeMarker.addWeekToList(openHours[7]);
            placeMarker.addWeekToList(openHours[9]);
            placeMarker.addWeekToList(openHours[11]);
            placeMarker.addWeekToList(openHours[13]);

            placeMarker.setPhotoUrl(googleDetails.get("photo_reference"));

        }
    }


}

