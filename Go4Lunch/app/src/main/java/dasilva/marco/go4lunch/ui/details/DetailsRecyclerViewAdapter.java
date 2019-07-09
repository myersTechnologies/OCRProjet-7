package dasilva.marco.go4lunch.ui.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class DetailsRecyclerViewAdapter extends RecyclerView.Adapter<DetailsRecyclerViewAdapter.ViewHolder>  {
    List<User> userList;
    List<SelectedPlace> selectedPlaces;
    Go4LunchService service = DI.getService();
    List<User>  users;
    public DetailsRecyclerViewAdapter(List<User> listOfUsers, List<SelectedPlace> selectedPlaces){
        this.userList = listOfUsers;
        this.selectedPlaces = selectedPlaces;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.details_users_list, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        User user = userList.get(i);
        for (int j = 0; j < selectedPlaces.size(); j++) {
            if (service.getPlaceMarker().getId().equals(selectedPlaces.get(j).getPlaceId())) {
                SelectedPlace place = selectedPlaces.get(j);
                users = new ArrayList<>();
                    if (place.getUserId().equals(user.getId())) {
                        users.add(user);
                        viewHolder.usersNames.setText(user.getUserName() + " is joining ");
                        Glide.with(viewHolder.itemView.getContext()).load(user.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(viewHolder.usersAvatars);
                        }
                    }
            }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView usersNames;
        ImageView usersAvatars;

        public ViewHolder(View itemView) {
            super(itemView);
            usersNames = (TextView) itemView.findViewById(R.id.item_details_name);
            usersAvatars = (ImageView) itemView.findViewById(R.id.item_details_avatar);
        }
    }
}
