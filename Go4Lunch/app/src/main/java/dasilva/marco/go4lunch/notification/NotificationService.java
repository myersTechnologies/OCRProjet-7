package dasilva.marco.go4lunch.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.IBinder;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import dasilva.marco.go4lunch.R;

public class NotificationService extends Service {

    private long timeToCheck;
    private long currentTime;
    private PendingIntent pendingIntent;

    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException(Resources.getSystem().getString(R.string.on_bind_string));
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        runTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelAlarm();
    }

    public void alarmToNoticateUser(){
        Intent intent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, timeToCheck, AlarmManager.INTERVAL_DAY , pendingIntent);
    }
    public void cancelAlarm() {
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    public void runTimer(){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                checkIfTimeMatchesToNotify();
                if(timeToCheck == currentTime) {
                    alarmToNoticateUser();
                }
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }

    public void checkIfTimeMatchesToNotify(){
        Calendar timeNow = Calendar.getInstance();
        Calendar defaultTime = Calendar.getInstance();
        defaultTime.set(Calendar.HOUR_OF_DAY, 12);
        defaultTime.set(Calendar.MINUTE, 0);
        defaultTime.set(Calendar.SECOND, 0);
        currentTime = timeNow.getTimeInMillis();
        timeToCheck = defaultTime.getTimeInMillis();
    }

}
