package dasilva.marco.go4lunch.ui.map.utils;


import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.utils.DataParser;
import dasilva.marco.go4lunch.ui.map.utils.DownloadUrl;


public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {


    String googlePlacesData;
    GoogleMap mMap;
    String url;
    String placeName;
    List<PlaceMarker> places = new ArrayList<>();
    Go4LunchService service = DI.getService();

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];

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
        List<HashMap<String, String>> nearbyPlaceList = null;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList){
        for (int i = 0; i < nearbyPlaceList.size(); i++){
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            placeName = googlePlace.get("place_name");

            double lat = 0;
            double lng = 0;

            if (googlePlace.get("lat") != null && googlePlace.get("lng") != null) {
                lat = Double.parseDouble(googlePlace.get("lat"));
                lng = Double.parseDouble(googlePlace.get("lng"));

                LatLng latLng = new LatLng(lat, lng);

                setPlaceMarkerInfo(googlePlace, latLng);

                if (!service.getListOfSelectedPlaces().isEmpty()) {
                    for (int j = 0; j < service.getListOfSelectedPlaces().size(); j++) {
                        for (int t = 0; t < places.size(); t++) {
                            if (service.getListOfSelectedPlaces().get(j).getId().equals(places.get(t).getId())) {
                                SelectedPlace selectedPlace = service.getListOfSelectedPlaces().get(j);
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
        service.setListMarkers(places);

    }

    public void setPlaceMarkerInfo(HashMap<String, String> googlePlace, LatLng latLng){
        service.setPlaceMarker(new PlaceMarker(null));
        service.getPlaceMarker().setName(placeName);
        service.getPlaceMarker().setId(googlePlace.get("place_id"));
        service.getPlaceMarker().setAdress(googlePlace.get("vicinity"));
        service.getPlaceMarker().setPhotoUrl(googlePlace.get("photo_reference"));
        service.getPlaceMarker().setOpeningHours(Boolean.parseBoolean(googlePlace.get("open_now")));
        service.getPlaceMarker().setLatLng(latLng);
        places.add(service.getPlaceMarker());
    }

    public void setMarker(MarkerOptions markerOptions){
        markerOptions.position(new LatLng(service.getPlaceMarker().getLatLng().latitude, service.getPlaceMarker().getLatLng().longitude));
        markerOptions.title(service.getPlaceMarker().getName());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        mMap.addMarker(markerOptions);
    }




}
