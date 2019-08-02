package dasilva.marco.go4lunch.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{


    private Go4LunchService service = DI.getService();
    private DataBaseService dataBaseService;
    private List<ChatMessage> chatMessages;
    private ChatMessage message;


    public ChatAdapter(List<ChatMessage> messages) {
        this.chatMessages = messages;
        dataBaseService = DI.getDatabaseService();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_message, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        message = chatMessages.get(i);
        // Set their text

        for (User users : dataBaseService.getUsersList()){
            if (message.getMessageUser().equals(users.getId())){
                if (message.getMessageUser().equals(service.getUser().getId())){
                    Glide.with(viewHolder.itemView.getContext()).load(users.getImageUrl())
                            .apply(RequestOptions.circleCropTransform()).into(viewHolder.currentUserAvatar);
                    viewHolder.userMessage.setText(users.getUserName());
                    viewHolder.userMessageText.setText(message.getMessageText());
                    viewHolder.userMessageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                            message.getMessageTime()));
                } else {
                Glide.with(viewHolder.itemView.getContext()).load(users.getImageUrl())
                        .apply(RequestOptions.circleCropTransform()).into(viewHolder.userAvatar);
                viewHolder.messageUser.setText(users.getUserName());
                    viewHolder.messageText.setText(message.getMessageText());
                    viewHolder.messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                            message.getMessageTime()));
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView messageText;
        private TextView messageUser;
        private TextView messageTime;
        private ImageView userAvatar;
        private TextView userMessageText;
        private TextView userMessage;
        private TextView userMessageTime;
        private ImageView currentUserAvatar;

        private ViewHolder(View itemView) {
            super(itemView);

            // Get references to the views of message.xml
           messageText = (TextView)itemView.findViewById(R.id.message_text);
           messageUser = (TextView)itemView.findViewById(R.id.message_user);
           messageTime = (TextView)itemView.findViewById(R.id.message_time);
           userAvatar = (ImageView) itemView.findViewById(R.id.item_chat_avatar);

           userMessageText = (TextView)itemView.findViewById(R.id.user_message_text);
           userMessage = (TextView)itemView.findViewById(R.id.user_message);
           userMessageTime = (TextView)itemView.findViewById(R.id.user_message_time);
           currentUserAvatar = (ImageView) itemView.findViewById(R.id.user_chat_avatar);

        }
    }



}
