package dasilva.marco.go4lunch.ui.map.fragments;


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

import java.util.ArrayList;
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
    private Toolbar toolbar;
    private AutoCompleteTextView autoCompleteTextView;
    private List<User> users;


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
        setHasOptionsMenu(true);
        return view;
    }

    public void getAllUsers(){
            if (dataBaseService.getUsersList() != null) {
                users = dataBaseService.getUsersList();
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getId().equals(service.getUser().getId())) {
                        users.remove(users.get(i));
                }
            }
                RviewWorkmatesAdapter adapter = new RviewWorkmatesAdapter(users);
                workmatesRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.search:
                toolbar = ((AppCompatActivity) getActivity()).findViewById(R.id.toolbar_search);
                toolbar.setVisibility(View.VISIBLE);
                autoCompleteTextView = toolbar.findViewById(R.id.auto_complete_text);
                autoCompleteTextView.setHint(getString(R.string.search_workmates_text));
                usersPredictions();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void usersPredictions(){
        List<String> usersNames = new ArrayList<>();
        List<String> usersId = new ArrayList<>();
        for (User users : users){
            usersNames.add(users.getUserName());
            usersId.add(users.getId());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, usersNames);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                List<User> choosedUser = new ArrayList<>();
                for (User users : users){
                    if (users.getUserName().equals(item)){
                        choosedUser.add(users);
                        Toast.makeText(getContext(), users.getUserName(), Toast.LENGTH_SHORT).show();
                        RviewWorkmatesAdapter adapter = new RviewWorkmatesAdapter(choosedUser);
                        workmatesRecyclerView.setAdapter(adapter);
                        toolbar.setVisibility(View.GONE);
                    }
                }
            }
        });
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
