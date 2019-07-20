package dasilva.marco.go4lunch.ui.map.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.adapters.RviewWorkmatesAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkmatesFragment extends Fragment {

    private RecyclerView workmatesRecyclerView;
    private RviewWorkmatesAdapter adapter;
    private Go4LunchService service = DI.getService();
    private List<User> users = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        workmatesRecyclerView = (RecyclerView) view.findViewById(R.id.workmates_recyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        workmatesRecyclerView.setLayoutManager(mLayoutManager);
        getAllUsers();
        return view;
    }

    public void getAllUsers(){

            if (service.getUsersList() != null) {
                users = service.getUsersList();
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getId().equals(service.getUser().getId())) {
                        users.remove(users.get(i));
                }

            }
                adapter = new RviewWorkmatesAdapter(users);
                workmatesRecyclerView.setAdapter(adapter);
        }
    }


}
