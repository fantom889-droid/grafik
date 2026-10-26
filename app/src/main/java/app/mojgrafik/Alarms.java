package app.mojgrafik;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

class Alarms {
    static final String PREF = "alarms";

    static void save(Context c, String json, int snooze) {
        c.getSharedPreferences(PREF, 0).edit().putString("list", json).putInt("snooze", snooze).apply();
        scheduleNext(c);
    }

    static PendingIntent pi(Context c, String label) {
        Intent i = new Intent(c, AlarmReceiver.class).putExtra("label", label);
        return PendingIntent.getBroadcast(c, 1, i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void setAt(Context c, long t, String label) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent show = PendingIntent.getActivity(c, 2, new Intent(c, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        am.setAlarmClock(new AlarmManager.AlarmClockInfo(t, show), pi(c, label));
    }

    static void scheduleNext(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREF, 0);
        long now = System.currentTimeMillis();
        try {
            JSONArray a = new JSONArray(p.getString("list", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                if (o.getLong("t") > now) {
                    setAt(c, o.getLong("t"), o.getString("l"));
                    return;
                }
            }
        } catch (Exception e) { }
        ((AlarmManager) c.getSystemService(Context.ALARM_SERVICE)).cancel(pi(c, ""));
    }
}
