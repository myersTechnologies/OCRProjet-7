package dasilva.marco.go4lunch.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


import dasilva.marco.go4lunch.R;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String lunchTime = intent.getStringExtra(context.getString(R.string.choice));
        String lunchAdress = intent.getStringExtra(context.getString(R.string.choice_adress));
        String joiningUsers = intent.getStringExtra(context.getString(R.string.joining_users));

        String message;

        String [] usersLenght = joiningUsers.split(", ");


        if (lunchAdress == " "){
            message = lunchAdress + "\n" + context.getString(R.string.nobody_joining) + " " + context.getString(R.string.bon_appetit);
        } else {
            if (usersLenght.length == 1){
                message = lunchAdress + "\n" + joiningUsers + " " + context.getString(R.string.single_join) + " " + context.getString(R.string.bon_appetit);
            } else {
                message = lunchAdress + "\n" + joiningUsers +  " " + context.getString(R.string.multiple_join) + " " + context.getString(R.string.bon_appetit);
            }
        }

        Notification.Builder builder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.go_lunch_24dp)
                .setAutoCancel(true)
                .setContentTitle(context.getString(R.string.notification_text_1) + " " + lunchTime);
        builder.setStyle(new Notification.BigTextStyle()
                .bigText(message));

        NotificationManager notif = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notif.notify(0, builder.build());
    }
}
