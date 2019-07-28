package dasilva.marco.go4lunch.ui.map.adapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.events.DetailsEvent;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.details.DetailsActivity;

public class RviewWorkmatesAdapter extends RecyclerView.Adapter<RviewWorkmatesAdapter.ViewHolder> {


    private List<User> listOfUsers;
    private DataBaseService dataBaseService;
    private Go4LunchService go4LunchService;

    public RviewWorkmatesAdapter(List<User> listOfUsers){
        this.listOfUsers = listOfUsers;
        dataBaseService = DI.getDatabaseService();
        go4LunchService = DI.getService();
    }

    @NonNull
    @Override
    public RviewWorkmatesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_of_workmates, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RviewWorkmatesAdapter.ViewHolder viewHolder, int i) {
        final User user = listOfUsers.get(i);
        if (user.getChoice() != null) {
            String choiceText = user.getFirtName() + " " + viewHolder.itemView.getContext().getString(R.string.joining_true) + " " + user.getChoice();
            viewHolder.userName.setText(choiceText);
            Glide.with(viewHolder.itemView.getContext()).load(user.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(viewHolder.userAvatar);
        } else {
            viewHolder.userName.setHint(user.getFirtName() + " " + viewHolder.itemView.getContext().getString(R.string.joining_false));
            Glide.with(viewHolder.itemView.getContext()).load(user.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(viewHolder.userAvatar);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (SelectedPlace selectedPlace : dataBaseService.getListOfSelectedPlaces()) {
                    for (PlaceMarker placeMarker : go4LunchService.getListMarkers()){
                        if (selectedPlace.getId().equals(placeMarker.getId())) {
                            if (selectedPlace.getUserId().contains(user.getId())) {
                                EventBus.getDefault().post(new DetailsEvent(placeMarker));
                                Intent intent = new Intent(viewHolder.itemView.getContext(), DetailsActivity.class);
                                viewHolder.itemView.getContext().startActivity(intent);
                                go4LunchService.setPlaceMarker(placeMarker);
                            }
                        }
                    }

                }

            }
        });


    }

    @Override
    public int getItemCount() {
        return listOfUsers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView userName;
        private ImageView userAvatar;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            userAvatar =  itemView.findViewById(R.id.item_list_avatar);
            userName = itemView.findViewById(R.id.item_list_name);
        }
    }

}
