package dasilva.marco.go4lunch.ui.map.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.events.DetailsEvent;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.adapters.RviewListAdapter;

public class ListViewFragment extends Fragment {

    private Go4LunchService service = DI.getService();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        RecyclerView listRecyclerView = view.findViewById(R.id.list_of_places);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        listRecyclerView.setLayoutManager(mLayoutManager);
        if (service.getListMarkers() != null) {
            List<PlaceMarker> places = service.getListMarkers();
            RviewListAdapter adapter = new RviewListAdapter(places);
            listRecyclerView.setAdapter(adapter);
        }
        return view;
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

}
