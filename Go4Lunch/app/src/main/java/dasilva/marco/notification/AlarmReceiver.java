package dasilva.marco.notification;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class AlarmReceiver extends BroadcastReceiver {

    private Go4LunchService service = DI.getService();
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder notifbuild = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle("Go4Lunch")
                .setContentText("It's time to lunch!")
                .setSmallIcon(R.drawable.go_lunch_24dp)
                .setAutoCancel(true);

        NotificationManager notif = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notif.notify(0, notifbuild.build());
        service.removeCompleteSelectionDatabase();
    }
}
