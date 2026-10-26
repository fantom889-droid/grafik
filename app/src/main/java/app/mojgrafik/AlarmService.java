package app.mojgrafik;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;

public class AlarmService extends Service {
    static final String CH = "alarm";
    static volatile boolean running = false;
    MediaPlayer mp;
    Vibrator vb;
    Handler h = new Handler(Looper.getMainLooper());

    static void ensureChannel(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CH, "Будильник смены", NotificationManager.IMPORTANCE_HIGH);
            ch.setSound(null, null);
            ch.enableVibration(false);
            ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(ch);
        }
    }

    static Notification notif(Context c, String label) {
        int f = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        Intent ai = new Intent(c, AlarmActivity.class).putExtra("label", label).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent full = PendingIntent.getActivity(c, 3, ai, f);
        PendingIntent stop = PendingIntent.getService(c, 4, new Intent(c, AlarmService.class).setAction("STOP"), f);
        PendingIntent snz = PendingIntent.getService(c, 5, new Intent(c, AlarmService.class).setAction("SNOOZE").putExtra("label", label), f);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(c, CH) : new Notification.Builder(c);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle(label).setContentText("Будильник")
            .setCategory(Notification.CATEGORY_ALARM).setOngoing(true)
            .setFullScreenIntent(full, true).setContentIntent(full)
            .addAction(0, "Отложить", snz).addAction(0, "Выключить", stop);
        if (Build.VERSION.SDK_INT < 26) b.setPriority(Notification.PRIORITY_MAX);
        return b.build();
    }

    @Override
    public int onStartCommand(Intent in, int flags, int id) {
        String act = in != null ? in.getAction() : null;
        String label = in != null ? in.getStringExtra("label") : null;
        if (label == null) label = "Смена";
        if ("STOP".equals(act)) { stopAll(); return START_NOT_STICKY; }
        if ("SNOOZE".equals(act)) {
            int m = getSharedPreferences(Alarms.PREF, 0).getInt("snooze", 10);
            Alarms.setAt(this, System.currentTimeMillis() + m * 60000L, label);
            stopAll();
            return START_NOT_STICKY;
        }
        ensureChannel(this);
        Notification n = notif(this, label);
        if (Build.VERSION.SDK_INT >= 29) startForeground(7, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        else startForeground(7, n);
        if (!running) {
            running = true;
            play();
            h.postDelayed(new Runnable() { public void run() { stopAll(); } }, 120000);
        }
        return START_NOT_STICKY;
    }

    void play() {
        try {
            Uri u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (u == null) u = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mp = new MediaPlayer();
            mp.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
            mp.setDataSource(this, u);
            mp.setLooping(true);
            mp.prepare();
            mp.start();
        } catch (Exception e) { }
        try {
            vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vb.vibrate(new long[]{0, 700, 600}, 0);
        } catch (Exception e) { }
    }

    void stopAll() {
        running = false;
        h.removeCallbacksAndMessages(null);
        if (mp != null) {
            try { mp.stop(); } catch (Exception e) { }
            mp.release();
            mp = null;
        }
        if (vb != null) vb.cancel();
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent i) { return null; }
}
