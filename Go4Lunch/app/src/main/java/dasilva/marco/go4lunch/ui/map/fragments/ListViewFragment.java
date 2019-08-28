package dasilva.marco.go4lunch.ui.map.fragments;


import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
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
    private static ListViewFragment listViewFragment;
    private RviewListAdapter adapter;
    private RecyclerView listRecyclerView;
    private static final String API_KEY = BuildConfig.GOOGLEAPIKEY;
    private PlacesClient client;
    private AutoCompleteTextView autoCompleteTextView;
    private Toolbar toolbar;
    private ArrayAdapter<String> predictionsAdapter;
    private List<PlaceMarker> places;
    private static String RESTAURANT = "restaurant";
    private static String FR = "fr";
    private List<String> idList;
    private LinearLayoutManager mLayoutManager;

    public ListViewFragment() {
        // Required empty public constructor
    }

    public static ListViewFragment getInstance(){
        if (listViewFragment == null){
            listViewFragment = new ListViewFragment();
        }
        return listViewFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        toolbar = ((AppCompatActivity)getActivity()).findViewById(R.id.toolbar_search);
        listRecyclerView = view.findViewById(R.id.list_of_places);
        mLayoutManager = new LinearLayoutManager(view.getContext());
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle( R.string.title_activity_map_view);
        setHasOptionsMenu(true);
        Places.initialize(getContext(), BuildConfig.GOOGLEAPIKEY);
        client = Places.createClient(getContext());
        initList();
        return view;
    }

    public void initList(){
        if (service.getListMarkers() != null) {
            service.countPlacesLikes();
            service.countPlaceSelectedByUsers();
            places = service.getListMarkers();
            listRecyclerView.setLayoutManager(mLayoutManager);
            adapter = new RviewListAdapter(places);
            service.addAdapter(adapter);
            listRecyclerView.setAdapter(adapter);
        }
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
                autoCompleteTextView = toolbar.findViewById(R.id.auto_complete_text);
                autoCompleteTextView.setHint(getString(R.string.search_restaurant_text));
                autoCompleteTextView.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        onItemClick();
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        googlePredictions(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        onItemClick();
                    }
                });
                toolbar.setVisibility(View.VISIBLE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void googlePredictions(String query){
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(service.getCurrentLocation().getLatitude(), service.getCurrentLocation().getLongitude()),
                new LatLng(service.getCurrentLocation().getLatitude(), service.getCurrentLocation().getLongitude()));

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(bounds)
                .setCountry(FR)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        client.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onSuccess(FindAutocompletePredictionsResponse response) {
                idList = new ArrayList<>();
                List<String> placesName = new ArrayList<>();
                for (AutocompletePrediction result : response.getAutocompletePredictions()){
                    if (result.getPlaceTypes().toString().toLowerCase().contains(RESTAURANT)) {
                        idList.add(result.getPlaceId());
                        placesName.add(result.getPrimaryText(null) + "\n" + result.getSecondaryText(null));
                    }
                }
                predictionsAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, placesName);
                autoCompleteTextView.setAdapter(predictionsAdapter);
            }
        });

        client.findAutocompletePredictions(request).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onItemClick(){
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String [] predictions = parent.getItemAtPosition(position).toString().split("\n");
                String predictionId = idList.get(position);
                String predictionName = predictions[0];
                final PlaceMarker placeMarker = new PlaceMarker();
                placeMarker.setId(predictionId);
                placeMarker.setName(predictionName);
                getMarkerDetails(placeMarker);
                autoCompleteTextView.getText().clear();
                toolbar.setVisibility(View.GONE);
                hideSoftKeyboard(getActivity());
                initList();

            }
        });
    }


    public void getMarkerDetails(PlaceMarker marker){
        String uri = getContext().getString(R.string.url_begin) + marker.getId() +
                getContext().getString(R.string.and_key) + API_KEY;
        Object dataTransfer[] = new Object[3];
        dataTransfer[0] = uri;
        dataTransfer[1] = marker;
        dataTransfer[2] = getContext();
        PlaceDetailsTask getNearbyPlacesData = new PlaceDetailsTask();
        getNearbyPlacesData.execute(dataTransfer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (autoCompleteTextView != null) {
            autoCompleteTextView.getText().clear();
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

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

}
