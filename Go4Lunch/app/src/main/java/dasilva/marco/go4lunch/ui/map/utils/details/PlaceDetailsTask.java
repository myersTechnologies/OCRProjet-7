package dasilva.marco.go4lunch.ui.map.utils.details;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dasilva.marco.go4lunch.BuildConfig;
import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.utils.DownloadUrl;

public class PlaceDetailsTask extends AsyncTask<Object, String, String> {

    private PlaceMarker placeMarker;
    private String googleDetailsData;
    private Go4LunchService service = DI.getService();
    private Context context;

    protected String doInBackground(Object... objects) {
        String jSonUrl = (String) objects[0];
        placeMarker = (PlaceMarker)objects[1];
        context = (Context) objects[2];


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

    private void setPlaceMarkerMoreInfo(List<HashMap<String, String>> detailsPlaceList) {
        for (int i = 0; i < detailsPlaceList.size(); i++) {
            HashMap<String, String> googleDetails = detailsPlaceList.get(i);

            placeMarker.setName(googleDetails.get("place_name"));

            double lat;
            double lng;

            if (googleDetails.get("lat") != null && googleDetails.get("lng") != null) {
                lat = Double.parseDouble(googleDetails.get("lat"));
                lng = Double.parseDouble(googleDetails.get("lng"));

                LatLng latLng = new LatLng(lat, lng);

                placeMarker.setLatLng(latLng);

            }

            placeMarker.setAdress(googleDetails.get("vicinity"));

            placeMarker.setOpeningHours(Boolean.parseBoolean(googleDetails.get("open_now")));

            placeMarker.setTelephone(googleDetails.get("phone_number"));

            placeMarker.setWebSite(googleDetails.get("website"));

            if (googleDetails.get("weekday_text") != null) {
                String[] openHours = googleDetails.get("weekday_text").split("\",\"");
                for (int j = 0; j < openHours.length; j++) {
                    for (int k = 0; k < openHours[j].split("\\[\"").length; k++) {
                        for (int o = 0; o < openHours[j].split("\\[\"")[k].split("\"\\]").length; o++) {
                            placeMarker.addWeekToList(openHours[j].split("\\[\"")[k].split("\"\\]")[o]);
                        }
                    }
                }
            }

            placeMarker.setPhotoUrl(getPhotoUrl(googleDetails.get("photo_reference")));
        }

        if (service.getListMarkers() == null) {
            List<PlaceMarker> placeMarkers = new ArrayList<>();
            service.setListMarkers(placeMarkers);
            service.getListMarkers().add(placeMarker);
        } else{
            if (service.getListMarkers() != null) {
                int count = 0;
                for (int i = 0; i < service.getListMarkers().size(); i++) {
                    if (!service.getListMarkers().get(i).getId().equals(placeMarker.getId())) {
                        count++;
                        if (count == service.getListMarkers().size()){
                            service.getListMarkers().add(placeMarker);
                        }
                    }
                }
            }
        }

        service.countPlaceSelectedByUsers();

        service.countPlacesLikes();

    }

    private String getPhotoUrl(String reference){
        String url = context.getString(R.string.google_photo_url) + reference
                + context.getString(R.string.and_key) + BuildConfig.GOOGLEAPIKEY;
        return url;
    }
}

