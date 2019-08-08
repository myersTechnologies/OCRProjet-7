package dasilva.marco.go4lunch.ui.map.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import dasilva.marco.go4lunch.BuildConfig;
import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.events.DetailsEvent;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.adapters.RviewListAdapter;
import dasilva.marco.go4lunch.ui.map.utils.details.PlaceDetailsTask;

public class ListViewFragment extends Fragment {

    private Go4LunchService service = DI.getService();
    private RviewListAdapter adapter;
    private RecyclerView listRecyclerView;
    private static final String API_KEY = BuildConfig.GOOGLEAPIKEY;
    private PlacesClient client;
    private AutoCompleteTextView autoCompleteTextView;
    private Toolbar toolbar;

    public ListViewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        listRecyclerView = view.findViewById(R.id.list_of_places);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        listRecyclerView.setLayoutManager(mLayoutManager);
        initList();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle( R.string.title_activity_map_view);
        setHasOptionsMenu(true);
        Places.initialize(getContext(), BuildConfig.GOOGLEAPIKEY);
        client = Places.createClient(getContext());

        return view;
    }

    public void initList(){
            if (service.getListMarkers() != null) {
                List<PlaceMarker> places = service.getListMarkers();
                adapter = new RviewListAdapter(places);
                service.addAdapter(adapter);
                listRecyclerView.setAdapter(adapter);
            }
    }

    @Override
    public void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void getSelectionToDetails(DetailsEvent event){
        service.setPlaceMarker(event.placeMarker);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.search:
                toolbar = ((AppCompatActivity) getActivity()).findViewById(R.id.toolbar_search);
                toolbar.setVisibility(View.VISIBLE);
                autoCompleteTextView = toolbar.findViewById(R.id.auto_complete_text);
                autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        googlePredictions(s.toString());
                    }
                });

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void googlePredictions(String query){
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(service.getCurrentLocation().getLatitude(), service.getCurrentLocation().getLongitude()),
                new LatLng(service.getCurrentLocation().getLatitude(), service.getCurrentLocation().getLongitude()));
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setCountry("fr")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        client.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onSuccess(FindAutocompletePredictionsResponse response) {
                final List<String> idList = new ArrayList<>();
                List<String> places = new ArrayList<>();
                for (AutocompletePrediction result : response.getAutocompletePredictions()){
                    if (result.getPlaceTypes().toString().toLowerCase().contains("restaurant")) {
                        idList.add(result.getPlaceId());
                        places.add(result.getPrimaryText(null) + "\n" + result.getSecondaryText(null));
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, places);
                autoCompleteTextView.setAdapter(adapter);
                autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getContext(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                        String [] predictions = parent.getItemAtPosition(position).toString().split("\n");
                        String predictionId = idList.get(position);
                        String predictionName = predictions[0];
                        PlaceMarker placeMarker = new PlaceMarker();
                        service.setPlaceMarker(placeMarker);
                        service.getPlaceMarker().setId(predictionId);
                        service.getPlaceMarker().setName(predictionName);
                        getMarkerDetails(service.getPlaceMarker());
                        toolbar.setVisibility(View.GONE);
                        autoCompleteTextView.setText("");

                    }
                });
                initList();
            }
        });

        client.findAutocompletePredictions(request).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getMarkerDetails(PlaceMarker marker){
        String uri = getContext().getString(R.string.url_begin) + marker.getId() +
                getContext().getString(R.string.and_key) + API_KEY;
        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = uri;
        dataTransfer[1] = marker;
        PlaceDetailsTask getNearbyPlacesData = new PlaceDetailsTask();
        getNearbyPlacesData.execute(dataTransfer);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (toolbar != null) {
            if (toolbar.getVisibility() == View.VISIBLE) {
                toolbar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDetach(){
        super.onDetach();
        if (toolbar != null) {
            if (toolbar.getVisibility() == View.VISIBLE) {
                toolbar.setVisibility(View.GONE);
            }
        }
    }
}
