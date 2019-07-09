package dasilva.marco.go4lunch.ui.map;

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

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.model.User;

public class RviewWorkmatesAdapter extends RecyclerView.Adapter<RviewWorkmatesAdapter.ViewHolder> {


    List<User> listOfUsers;

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
            viewHolder.userName.setText(user.getUserName() + " is eating at (" + user.getChoice() + ")");
            Glide.with(viewHolder.itemView.getContext()).load(user.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(viewHolder.userAvatar);
        } else {
            viewHolder.userName.setHint(user.getUserName() + " hasn't decided yet");
            Glide.with(viewHolder.itemView.getContext()).load(user.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(viewHolder.userAvatar);
        }

    }

    @Override
    public int getItemCount() {
        Log.d("ListSize", String.valueOf(listOfUsers.size()));
        return listOfUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userName;
        public ImageView userAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            userAvatar = (ImageView) itemView.findViewById(R.id.item_list_avatar);
            userName = (TextView) itemView.findViewById(R.id.item_list_name);
        }
    }

}
