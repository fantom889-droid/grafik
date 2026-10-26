package app.mojgrafik;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent in) {
        String label = in.getStringExtra("label");
        if (label == null || label.isEmpty()) label = "Смена";
        Alarms.scheduleNext(c);
        try {
            Intent s = new Intent(c, AlarmService.class).putExtra("label", label);
            if (Build.VERSION.SDK_INT >= 26) c.startForegroundService(s); else c.startService(s);
        } catch (Exception e) {
            AlarmService.ensureChannel(c);
            ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(7, AlarmService.notif(c, label));
        }
    }
}
