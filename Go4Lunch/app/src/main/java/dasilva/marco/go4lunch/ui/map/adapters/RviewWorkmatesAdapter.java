package dasilva.marco.go4lunch.ui.map.adapters;

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

public class RviewWorkmatesAdapter extends RecyclerView.Adapter<RviewWorkmatesAdapter.ViewHolder> {


    private List<User> listOfUsers;

    public RviewWorkmatesAdapter(List<User> listOfUsers){
        this.listOfUsers = listOfUsers;
    }

    @NonNull
    @Override
    public RviewWorkmatesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_of_workmates, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RviewWorkmatesAdapter.ViewHolder viewHolder, int i) {
        User user = listOfUsers.get(i);
        if (user.getChoice() != null) {
            String choiceText = user.getFirtName() + " " + viewHolder.itemView.getContext().getString(R.string.joining_true) + user.getChoice() + ")";
            viewHolder.userName.setText(choiceText);
            Glide.with(viewHolder.itemView.getContext()).load(user.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(viewHolder.userAvatar);
        } else {
            viewHolder.userName.setHint(user.getFirtName() + " " + viewHolder.itemView.getContext().getString(R.string.joining_false));
            Glide.with(viewHolder.itemView.getContext()).load(user.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(viewHolder.userAvatar);
        }

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
