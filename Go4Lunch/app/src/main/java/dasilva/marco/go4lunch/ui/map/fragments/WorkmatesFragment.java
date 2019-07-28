package dasilva.marco.go4lunch.ui.map.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.map.adapters.RviewWorkmatesAdapter;

public class WorkmatesFragment extends Fragment {

    private RecyclerView workmatesRecyclerView;
    private Go4LunchService service;
    private DataBaseService dataBaseService;

    public WorkmatesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        service = DI.getService();
        dataBaseService = DI.getDatabaseService();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        workmatesRecyclerView = (RecyclerView) view.findViewById(R.id.workmates_recyclerView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        workmatesRecyclerView.setLayoutManager(mLayoutManager);
        getAllUsers();
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.workmates_string);
        return view;
    }

    public void getAllUsers(){
            if (dataBaseService.getUsersList() != null) {
                List<User> users = dataBaseService.getUsersList();
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getId().equals(service.getUser().getId())) {
                        users.remove(users.get(i));
                }
            }
                RviewWorkmatesAdapter adapter = new RviewWorkmatesAdapter(users);
                workmatesRecyclerView.setAdapter(adapter);
        }
    }

}
