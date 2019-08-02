package dasilva.marco.go4lunch.ui.map.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Arrays;
import java.util.List;

import dasilva.marco.go4lunch.BuildConfig;
import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.events.DetailsEvent;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.adapters.RviewListAdapter;
import dasilva.marco.go4lunch.ui.map.utils.details.PlaceDetailsTask;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ListViewFragment extends Fragment {

    private Go4LunchService service = DI.getService();
    private RviewListAdapter adapter;
    private RecyclerView listRecyclerView;
    private static final String API_KEY = BuildConfig.GOOGLEAPIKEY;

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
                final Toolbar toolbar = ((AppCompatActivity) getActivity()).findViewById(R.id.toolbar_chat);
                toolbar.setVisibility(View.VISIBLE);
                final AutoCompleteTextView autoCompleteTextView = toolbar.findViewById(R.id.auto_complete_text);
                String [] cities = new String[]{"London", "Lisbon", "Barcelona", "Madrid", "Paris", "Berlin", "Amsterdam"};
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1,  cities );
                autoCompleteTextView.setAdapter(adapter);
                autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(getContext(), parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                        toolbar.setVisibility(View.GONE);
                        autoCompleteTextView.setText("");
                    }
                });

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 7) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                if (!service.getListMarkers().contains(place.getId())){
                    PlaceMarker marker = new PlaceMarker();
                    marker.setId(place.getId());
                    marker.setName(place.getName());
                    marker.setAdress(place.getAddress());
                    getMarkerDetails(marker);
                    marker.setLatLng(place.getLatLng());
                    service.getListMarkers().add(marker);
                    adapter.notifyDataSetChanged();
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
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



}
