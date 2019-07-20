package dasilva.marco.go4lunch.ui.details;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.model.User;


public class DetailsRecyclerViewAdapter extends RecyclerView.Adapter<DetailsRecyclerViewAdapter.ViewHolder>  {
    private List<User> userList;


    public DetailsRecyclerViewAdapter(List<User> listOfUsers){
        this.userList = listOfUsers;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.details_users_list, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        User user = userList.get(i);

        String userIsJoining = user.getFirtName() + " " + viewHolder.itemView.getContext().getString(R.string.is_joining);
        viewHolder.usersNames.setText(userIsJoining);
        Glide.with(viewHolder.itemView.getContext()).load(user.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(viewHolder.usersAvatars);




    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView usersNames;
        private ImageView usersAvatars;

        private ViewHolder(View itemView) {
            super(itemView);
            usersNames = itemView.findViewById(R.id.item_details_name);
            usersAvatars = itemView.findViewById(R.id.item_details_avatar);
        }
    }
}
