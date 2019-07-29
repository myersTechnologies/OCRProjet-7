package dasilva.marco.go4lunch.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.widget.Toast;


import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import dasilva.marco.go4lunch.R;

public class NotificationService extends Service {

    private long timeToCheck;
    private long currentTime;
    private PendingIntent pendingIntent;
    private String choice;
    private String choiceAdress;
    private String joiningUsers;
    private SharedPreferences preferences;
    private boolean timesOn = false;

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
        preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        choice = preferences.getString(getString(R.string.choice), null);
        choiceAdress = preferences.getString(getString(R.string.choice_adress), null);
        joiningUsers = preferences.getString(getString(R.string.joining_users), null);
        Toast.makeText(this, getString(R.string.notification_toast), Toast.LENGTH_SHORT).show();
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
        intent.putExtra(getString(R.string.choice), choice);
        intent.putExtra(getString(R.string.choice_adress), choiceAdress);
        intent.putExtra(getString(R.string.joining_users), joiningUsers);
        sendBroadcast(intent);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        preferences.edit().remove(getString(R.string.choice)).apply();
        preferences.edit().remove(getString(R.string.choice_adress)).apply();
        choice = null;
        choiceAdress = null;
        joiningUsers = null;
        timesOn = true;
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
                if (currentTime == timeToCheck) {
                    if (!timesOn) {
                        alarmToNoticateUser();
                    }
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
