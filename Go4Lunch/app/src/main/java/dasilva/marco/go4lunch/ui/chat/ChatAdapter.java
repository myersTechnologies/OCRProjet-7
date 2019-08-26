package dasilva.marco.go4lunch.ui.chat;

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
    private static  String format = "dd-MM-yyyy (HH:mm:ss)";

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
        ChatMessage message = chatMessages.get(i);
        for (User users : dataBaseService.getUsersList()){
            if (message.getMessageUser().equals(service.getUser().getId())){
                Glide.with(viewHolder.itemView.getContext()).load(service.getUser().getImageUrl())
                        .apply(RequestOptions.circleCropTransform()).into(viewHolder.currentUserAvatar);
                viewHolder.userMessage.setText(service.getUser().getUserName());
                viewHolder.userMessageText.setText(message.getMessageText());
                viewHolder.userMessageTime.setText(DateFormat.format(format,
                        message.getMessageTime()));
                viewHolder.senderMessageUser.setVisibility(View.INVISIBLE);
                viewHolder.senderMessageText.setVisibility(View.INVISIBLE);
                viewHolder.senderMessageTime.setVisibility(View.INVISIBLE);
            } else {
                if (users.getId().contains(message.getMessageUser())) {
                    Glide.with(viewHolder.itemView.getContext()).load(users.getImageUrl())
                            .apply(RequestOptions.circleCropTransform()).into(viewHolder.senderAvatar);
                    viewHolder.senderMessageUser.setText(users.getUserName());
                    viewHolder.senderMessageText.setText(message.getMessageText());
                    viewHolder.senderMessageTime.setText(DateFormat.format(format,
                            message.getMessageTime()));
                    viewHolder.userMessageText.setVisibility(View.INVISIBLE);
                    viewHolder.userMessage.setVisibility(View.INVISIBLE);
                    viewHolder.userMessageTime.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView senderMessageText;
        private TextView senderMessageUser;
        private TextView senderMessageTime;
        private ImageView senderAvatar;
        private TextView userMessageText;
        private TextView userMessage;
        private TextView userMessageTime;
        private ImageView currentUserAvatar;

        private ViewHolder(View itemView) {
            super(itemView);

            // Get references to the views of message.xml
           senderMessageText = itemView.findViewById(R.id.message_text);
           senderMessageUser = itemView.findViewById(R.id.message_user);
           senderMessageTime = itemView.findViewById(R.id.message_time);
           senderAvatar = itemView.findViewById(R.id.item_chat_avatar);

           userMessageText = itemView.findViewById(R.id.user_message_text);
           userMessage = itemView.findViewById(R.id.user_message);
           userMessageTime = itemView.findViewById(R.id.user_message_time);
           currentUserAvatar = itemView.findViewById(R.id.user_chat_avatar);

        }
    }



}
