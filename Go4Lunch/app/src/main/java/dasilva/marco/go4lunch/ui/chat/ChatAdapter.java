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

import org.w3c.dom.Text;

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
        viewHolder.txtMessage.setText(message.getMessageText());
        viewHolder.txtTime.setText(DateFormat.format(format,
                message.getMessageTime()));
        for (User users : dataBaseService.getUsersList()){
            if (message.getMessageUser().equals(service.getUser().getId())){
                viewHolder.txtInfo.setText(String.valueOf(service.getUser().getUserName()));
                Glide.with(viewHolder.itemView.getContext()).load(service.getUser().getImageUrl())
                        .apply(RequestOptions.circleCropTransform()).into(viewHolder.userImg);
                viewHolder.contentWithBG.setBackgroundResource(R.drawable.in_message_bg);


                LinearLayout.LayoutParams layout =
                        (LinearLayout.LayoutParams) viewHolder.avatarInfo.getLayoutParams();
                layout.gravity = Gravity.RIGHT;
                viewHolder.avatarInfo.setLayoutParams(layout);

                LinearLayout.LayoutParams layoutParams =
                        (LinearLayout.LayoutParams) viewHolder.contentWithBG.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                viewHolder.contentWithBG.setLayoutParams(layoutParams);

                RelativeLayout.LayoutParams lp =
                        (RelativeLayout.LayoutParams) viewHolder.content.getLayoutParams();
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                viewHolder.content.setLayoutParams(lp);

                layoutParams = (LinearLayout.LayoutParams) viewHolder.txtTime.getLayoutParams();
                layoutParams.gravity = Gravity.LEFT;
                viewHolder.txtTime.setLayoutParams(layoutParams);


                layoutParams = (LinearLayout.LayoutParams) viewHolder.userImg.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                viewHolder.userImg.setLayoutParams(layoutParams);

                layoutParams = (LinearLayout.LayoutParams) viewHolder.txtMessage.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                viewHolder.txtMessage.setLayoutParams(layoutParams);

                layoutParams = (LinearLayout.LayoutParams) viewHolder.txtInfo.getLayoutParams();
                layoutParams.gravity = Gravity.RIGHT;
                viewHolder.txtInfo.setLayoutParams(layoutParams);
            } else {
                if (users.getId().contains(message.getMessageUser())) {
                    viewHolder.txtInfo.setText(users.getUserName());
                    Glide.with(viewHolder.itemView.getContext()).load(users.getImageUrl())
                            .apply(RequestOptions.circleCropTransform()).into(viewHolder.usersImg);
                    viewHolder.contentWithBG.setBackgroundResource(R.drawable.out_message_bg);

                    LinearLayout.LayoutParams layout =
                            (LinearLayout.LayoutParams) viewHolder.avatarInfo.getLayoutParams();
                    layout.gravity = Gravity.LEFT;
                    viewHolder.avatarInfo.setLayoutParams(layout);

                    LinearLayout.LayoutParams layoutParams =
                            (LinearLayout.LayoutParams) viewHolder.contentWithBG.getLayoutParams();
                    layoutParams.gravity = Gravity.LEFT;
                    viewHolder.contentWithBG.setLayoutParams(layoutParams);

                    RelativeLayout.LayoutParams lp =
                            (RelativeLayout.LayoutParams) viewHolder.content.getLayoutParams();
                    lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    viewHolder.content.setLayoutParams(lp);

                    layoutParams = (LinearLayout.LayoutParams) viewHolder.userImg.getLayoutParams();
                    layoutParams.gravity = Gravity.LEFT;
                    viewHolder.userImg.setLayoutParams(layoutParams);

                    layoutParams = (LinearLayout.LayoutParams) viewHolder.txtMessage.getLayoutParams();
                    layoutParams.gravity = Gravity.LEFT;
                    viewHolder.txtMessage.setLayoutParams(layoutParams);

                    layoutParams = (LinearLayout.LayoutParams) viewHolder.txtTime.getLayoutParams();
                    layoutParams.gravity = Gravity.RIGHT;
                    viewHolder.txtTime.setLayoutParams(layoutParams);

                    layoutParams = (LinearLayout.LayoutParams) viewHolder.txtInfo.getLayoutParams();
                    layoutParams.gravity = Gravity.LEFT;
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

        public TextView txtMessage;
        public TextView txtInfo;
        public TextView txtTime;
        public ImageView userImg;
        public ImageView usersImg;
        public LinearLayout content;
        public LinearLayout contentWithBG;
        public LinearLayout avatarInfo;

        private ViewHolder(View itemView) {
            super(itemView);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);
            content = (LinearLayout) itemView.findViewById(R.id.content);
            contentWithBG = (LinearLayout) itemView.findViewById(R.id.contentWithBackground);
            txtInfo = (TextView) itemView.findViewById(R.id.txtInfo);
            txtTime = (TextView) itemView.findViewById(R.id.txtTime);
            userImg = (ImageView) itemView.findViewById(R.id.item_chat_avatar);
            usersImg = (ImageView) itemView.findViewById(R.id.item_chat_avatar_user);
            avatarInfo = (LinearLayout) itemView.findViewById(R.id.avatarInfo);


        }
    }



}
