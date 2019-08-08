package dasilva.marco.go4lunch.ui.map.utils.details;


import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.utils.DownloadUrl;

public class PlaceDetailsTask extends AsyncTask<Object, String, String> {

    private PlaceMarker placeMarker;
    private String googleDetailsData;
    Go4LunchService service = DI.getService();

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
        for (int i = 0; i < detailsPlaceList.size(); i++) {
            HashMap<String, String> googleDetails = detailsPlaceList.get(i);

            if (placeMarker.getName() == null) {
                placeMarker.setName(googleDetails.get("name"));
            }

            placeMarker.setAdress(googleDetails.get("vicinity"));

            double lat;
            double lng;

            if (googleDetails.get("lat") != null && googleDetails.get("lng") != null) {
                lat = Double.parseDouble(googleDetails.get("lat"));
                lng = Double.parseDouble(googleDetails.get("lng"));

                LatLng latLng = new LatLng(lat, lng);
                placeMarker.setLatLng(latLng);
            }

            placeMarker.setOpeningHours(Boolean.parseBoolean(googleDetails.get("open_now")));

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

        if (service.getListMarkers() != null) {
            if (!service.getListMarkers().contains(placeMarker)) {
                service.getListMarkers().add(placeMarker);
            }
        } else {
            service.setListMarkers(new ArrayList<PlaceMarker>());
            service.getListMarkers().add(placeMarker);
        }
        try{
            service.countPlaceSelectedByUsers();
            service.countPlacesLikes();
        } catch (NullPointerException e){
            Log.d("No Data", e.toString());
        }

    }



}

