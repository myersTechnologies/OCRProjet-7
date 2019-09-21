package dasilva.marco.go4lunch.ui.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.ChatMessage;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private Go4LunchService service = DI.getService();
    private DataBaseService dataBaseService;
    private List<ChatMessage> chatMessages;
    private List<User> usersList;

    public ChatAdapter(List<ChatMessage> messages) {
        this.chatMessages = messages;
        dataBaseService = DI.getDatabaseService();
        usersList = dataBaseService.getUsersList();

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_message, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.setIsRecyclable(false);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ChatMessage message = chatMessages.get(i);
        viewHolder.txtMessage.setText(message.getMessageText());
        String format = "dd-MM-yyyy (HH:mm:ss)";
        viewHolder.txtTime.setText(DateFormat.format(format,
                message.getMessageTime()));

        Glide.with(viewHolder.itemView.getContext()).load(viewHolder.usersImg.getContext().getResources().getDrawable(R.drawable.user))
                .apply(RequestOptions.circleCropTransform()).into(viewHolder.usersImg);
        viewHolder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);

        viewHolder.txtInfo.setText(viewHolder.usersImg.getContext().getString(R.string.deleted_user));
        viewHolder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);

        LinearLayout.LayoutParams layoutUnknownUser =
                (LinearLayout.LayoutParams) viewHolder.avatarInfo.getLayoutParams();
        layoutUnknownUser.gravity = Gravity.START;
        viewHolder.avatarInfo.setLayoutParams(layoutUnknownUser);

        LinearLayout.LayoutParams layoutParamsUnkownUser =
                (LinearLayout.LayoutParams) viewHolder.contentWithBG.getLayoutParams();
        layoutParamsUnkownUser.gravity = Gravity.START;
        viewHolder.contentWithBG.setLayoutParams(layoutParamsUnkownUser);

        RelativeLayout.LayoutParams lpUnkownUser =
                (RelativeLayout.LayoutParams) viewHolder.content.getLayoutParams();
        lpUnkownUser.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        lpUnkownUser.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        viewHolder.content.setLayoutParams(lpUnkownUser);

        layoutParamsUnkownUser = (LinearLayout.LayoutParams) viewHolder.userImg.getLayoutParams();
        layoutParamsUnkownUser.gravity = Gravity.START;
        viewHolder.userImg.setLayoutParams(layoutParamsUnkownUser);

        layoutParamsUnkownUser = (LinearLayout.LayoutParams) viewHolder.txtMessage.getLayoutParams();
        layoutParamsUnkownUser.gravity = Gravity.START;
        viewHolder.txtMessage.setLayoutParams(layoutParamsUnkownUser);

        layoutParamsUnkownUser = (LinearLayout.LayoutParams) viewHolder.txtTime.getLayoutParams();
        layoutParamsUnkownUser.gravity = Gravity.END;
        viewHolder.txtTime.setLayoutParams(layoutParamsUnkownUser);

        layoutParamsUnkownUser = (LinearLayout.LayoutParams) viewHolder.txtInfo.getLayoutParams();
        layoutParamsUnkownUser.gravity = Gravity.START;
        viewHolder.txtInfo.setLayoutParams(layoutParamsUnkownUser);

        for (int j = 0; j < usersList.size(); j++){
            User users = usersList.get(j);

            if (message.getMessageUser().equals(service.getUser().getId())) {

                viewHolder.txtInfo.setText(String.valueOf(service.getUser().getUserName()));
                Glide.with(viewHolder.itemView.getContext()).load(service.getUser().getImageUrl())
                        .apply(RequestOptions.circleCropTransform()).into(viewHolder.userImg);
                viewHolder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);

                viewHolder.usersImg.setVisibility(View.GONE);
                LinearLayout.LayoutParams layout =
                        (LinearLayout.LayoutParams) viewHolder.avatarInfo.getLayoutParams();
                layout.gravity = Gravity.END;
                viewHolder.avatarInfo.setLayoutParams(layout);

                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) viewHolder.contentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.END;
                viewHolder.contentWithBG.setLayoutParams(layoutParams);

                RelativeLayout.LayoutParams lp =
                        (RelativeLayout.LayoutParams) viewHolder.content.getLayoutParams();
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                viewHolder.content.setLayoutParams(lp);

                layoutParams = (LinearLayout.LayoutParams) viewHolder.txtTime.getLayoutParams();
                layoutParams.gravity = Gravity.START;
                viewHolder.txtTime.setLayoutParams(layoutParams);


                layoutParams = (LinearLayout.LayoutParams) viewHolder.userImg.getLayoutParams();
                layoutParams.gravity = Gravity.END;
                viewHolder.userImg.setLayoutParams(layoutParams);

                layoutParams = (LinearLayout.LayoutParams) viewHolder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.END;
                viewHolder.txtMessage.setLayoutParams(layoutParams);

                layoutParams = (LinearLayout.LayoutParams) viewHolder.txtInfo.getLayoutParams();
                layoutParams.gravity = Gravity.END;
                viewHolder.txtInfo.setLayoutParams(layoutParams);

            } else {
                if (message.getMessageUser().equals(users.getId())) {
                    viewHolder.txtInfo.setText(users.getUserName());
                    Glide.with(viewHolder.itemView.getContext()).load(users.getImageUrl())
                            .apply(RequestOptions.circleCropTransform()).into(viewHolder.usersImg);
                    viewHolder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);
                    viewHolder.userImg.setVisibility(View.GONE);

                    LinearLayout.LayoutParams layout =
                            (LinearLayout.LayoutParams) viewHolder.avatarInfo.getLayoutParams();
                    layout.gravity = Gravity.START;
                    viewHolder.avatarInfo.setLayoutParams(layout);

                    LinearLayout.LayoutParams layoutParams =
                            (LinearLayout.LayoutParams) viewHolder.contentWithBG.getLayoutParams();
                    layoutParams.gravity = Gravity.START;
                    viewHolder.contentWithBG.setLayoutParams(layoutParams);

                    RelativeLayout.LayoutParams lp =
                            (RelativeLayout.LayoutParams) viewHolder.content.getLayoutParams();
                    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    viewHolder.content.setLayoutParams(lp);

                    layoutParams = (LinearLayout.LayoutParams) viewHolder.userImg.getLayoutParams();
                    layoutParams.gravity = Gravity.START;
                    viewHolder.userImg.setLayoutParams(layoutParams);

                    layoutParams = (LinearLayout.LayoutParams) viewHolder.txtMessage.getLayoutParams();
                    layoutParams.gravity = Gravity.START;
                    viewHolder.txtMessage.setLayoutParams(layoutParams);

                    layoutParams = (LinearLayout.LayoutParams) viewHolder.txtTime.getLayoutParams();
                    layoutParams.gravity = Gravity.END;
                    viewHolder.txtTime.setLayoutParams(layoutParams);

                    layoutParams = (LinearLayout.LayoutParams) viewHolder.txtInfo.getLayoutParams();
                    layoutParams.gravity = Gravity.START;
                    viewHolder.txtInfo.setLayoutParams(layoutParams);
                }
            }

        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtMessage;
        private TextView txtInfo;
        private TextView txtTime;
        private ImageView userImg;
        private ImageView usersImg;
        private LinearLayout content;
        private LinearLayout contentWithBG;
        private LinearLayout avatarInfo;

        private ViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            content =  itemView.findViewById(R.id.content);
            contentWithBG = itemView.findViewById(R.id.contentWithBackground);
            txtInfo = itemView.findViewById(R.id.txtInfo);
            txtTime =  itemView.findViewById(R.id.txtTime);
            userImg =  itemView.findViewById(R.id.item_chat_avatar);
            usersImg =  itemView.findViewById(R.id.item_chat_avatar_user);
            avatarInfo =  itemView.findViewById(R.id.avatarInfo);


        }
    }



}
