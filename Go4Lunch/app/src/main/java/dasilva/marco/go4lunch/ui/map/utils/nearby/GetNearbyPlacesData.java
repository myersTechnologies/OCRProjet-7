package dasilva.marco.go4lunch.ui.map.utils.nearby;


import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dasilva.marco.go4lunch.BuildConfig;
import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.utils.DownloadUrl;
import dasilva.marco.go4lunch.ui.map.utils.details.PlaceDetailsTask;


public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private GoogleMap mMap;
    private String placeName;
    private List<PlaceMarker> places = new ArrayList<>();
    private Go4LunchService service = DI.getService();
    private DataBaseService dataBaseService = DI.getDatabaseService();
    private Context context;
    private static final String API_KEY = BuildConfig.GOOGLEAPIKEY;

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        String url = (String) objects[1];
        context = (Context)objects[2];

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
        for (int i = 0; i < nearbyPlaceList.size(); i++){
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            placeName = googlePlace.get("place_name");

            double lat;
            double lng;

            if (googlePlace.get("lat") != null && googlePlace.get("lng") != null) {
                lat = Double.parseDouble(googlePlace.get("lat"));
                lng = Double.parseDouble(googlePlace.get("lng"));

                LatLng latLng = new LatLng(lat, lng);

                setPlaceMarkerInfo(googlePlace, latLng);

                if (!dataBaseService.getListOfSelectedPlaces().isEmpty()) {
                    for (int j = 0; j < dataBaseService.getListOfSelectedPlaces().size(); j++) {
                        for (int t = 0; t < places.size(); t++) {
                            if (dataBaseService.getListOfSelectedPlaces().get(j).getId().equals(places.get(t).getId())) {
                                SelectedPlace selectedPlace = dataBaseService.getListOfSelectedPlaces().get(j);
                                PlaceMarker placeMarker = places.get(t);
                                if (selectedPlace.getLatLng().equals(String.valueOf(placeMarker.getLatLng()))) {
                                    markerOptions.position(placeMarker.getLatLng());
                                    markerOptions.title(placeMarker.getName());
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    mMap.addMarker(markerOptions);
                                }

                            } else {
                               setMarker(markerOptions);
                            }
                        }

                    }

                } else {
                   setMarker(markerOptions);
                }
            }

        }
        if (service.getListMarkers() == null) {
            service.setListMarkers(places);
        } else {
            for (PlaceMarker placeMarker : places){
                service.getListMarkers().add(placeMarker);
            }
        }
    }

    private void setPlaceMarkerInfo(HashMap<String, String> googlePlace, LatLng latLng){
            PlaceMarker placeMarker = new PlaceMarker();
            service.setPlaceMarker(placeMarker);
            service.getPlaceMarker().setName(placeName);
            service.getPlaceMarker().setId(googlePlace.get("place_id"));
            service.getPlaceMarker().setAdress(googlePlace.get("vicinity"));
            service.getPlaceMarker().setOpeningHours(Boolean.parseBoolean(googlePlace.get("open_now")));
            service.getPlaceMarker().setLatLng(latLng);

            String uri = context.getString(R.string.url_begin) + placeMarker.getId() +
                    context.getString(R.string.and_key) + API_KEY;
            Object dataTransfer[] = new Object[2];
            dataTransfer[0] = uri;
            dataTransfer[1] = placeMarker;

            PlaceDetailsTask getNearbyPlacesData = new PlaceDetailsTask();
            getNearbyPlacesData.execute(dataTransfer);

            places.add(service.getPlaceMarker());
    }

    private void setMarker(MarkerOptions markerOptions){
        markerOptions.position(new LatLng(service.getPlaceMarker().getLatLng().latitude, service.getPlaceMarker().getLatLng().longitude));
        markerOptions.title(service.getPlaceMarker().getName());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        mMap.addMarker(markerOptions);
    }
}
