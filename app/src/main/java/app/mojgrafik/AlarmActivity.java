package app.mojgrafik;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmActivity extends Activity {
    Handler h = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        final String label = getIntent().getStringExtra("label") == null ? "Смена" : getIntent().getStringExtra("label");
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setGravity(Gravity.CENTER);
        l.setPadding(48, 48, 48, 48);
        l.setBackgroundColor(Color.parseColor("#16233B"));
        TextView t = new TextView(this);
        t.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        t.setTextColor(Color.WHITE);
        t.setTextSize(64);
        TextView s = new TextView(this);
        s.setText(label);
        s.setTextColor(Color.parseColor("#C8D3F0"));
        s.setTextSize(24);
        s.setPadding(0, 8, 0, 64);
        Button snz = new Button(this);
        snz.setText("Отложить");
        snz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(AlarmActivity.this, AlarmService.class).setAction("SNOOZE").putExtra("label", label));
                finish();
            }
        });
        Button stop = new Button(this);
        stop.setText("Выключить");
        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService(new Intent(AlarmActivity.this, AlarmService.class).setAction("STOP"));
                finish();
            }
        });
        l.addView(t);
        l.addView(s);
        l.addView(snz, new LinearLayout.LayoutParams(-1, 160));
        l.addView(stop, new LinearLayout.LayoutParams(-1, 160));
        setContentView(l);
        h.postDelayed(new Runnable() {
            public void run() {
                if (!AlarmService.running) finish(); else h.postDelayed(this, 1000);
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
