package com.estimote.proximity;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity.estimote.ProximityContentManager;

import org.json.JSONObject;

import java.util.concurrent.LinkedBlockingDeque;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MainActivity extends AppCompatActivity {

    private ProximityContentManager proximityContentManager;
    Article article;
    TextView et_auditorium, et_entrance, et_gym;
    boolean process = true;
    NotificationManager notificationManager;
    public LinkedBlockingDeque<String> queue = new LinkedBlockingDeque();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        et_auditorium = findViewById(R.id.et_auditorium);
        et_entrance = findViewById(R.id.et_entrance);
        et_gym = findViewById(R.id.et_gym);

        et_auditorium.setOnClickListener(view -> {
            queue.push("1d9141470c04e7f17e9091a66414d21d");
        });
        et_entrance.setOnClickListener(view -> {
            queue.push("");
        });
        et_gym.setOnClickListener(view -> {
            queue.push("");
        });

        article = new Article();


        RequirementsWizardFactory
                .createEstimoteRequirementsWizard()
                .fulfillRequirements(this,
                        () -> {
                            Log.d("app", "requirements fulfilled");
                            startProximityContentManager();
                            return null;
                        },
                        requirements -> {
                            Log.e("app", "requirements missing: " + requirements);
                            return null;
                        },
                        throwable -> {
                            Log.e("app", "requirements error: " + throwable);
                            return null;
                        });

        buildNotification("test", "hi");


        new Thread(() -> {
            while (true) {
                String beacon_id = null;
                JSONObject obj = new JSONObject();
                if (proximityContentManager == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                try {
                    if (proximityContentManager.queue == null) {
                        continue;
                    }
                    if (!process) {
                        continue;
                    }
                    beacon_id = proximityContentManager.queue.take();
                    notificationManager.notify(1, buildNotification(article.getNotificationHead(beacon_id), article.getNotificationBody(beacon_id)));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            while (true) {
                String location = null;
                try {
                    if (queue == null) {
                        continue;
                    }
                    if (!process) {
                        continue;
                    }
                    location = queue.take();
                    ShowInfo(location);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startProximityContentManager() {
        proximityContentManager = new ProximityContentManager(this, ((MyApplication) getApplication()).cloudCredentials);
        proximityContentManager.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (proximityContentManager != null)
            proximityContentManager.stop();
    }

    void ShowInfo(String id) {
        try {
            String name = article.getName(id);
            String subject = article.getSubject(id);
            String info = article.getInfo(id);

            Intent intent = new Intent(getApplicationContext(), ContentActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("subject", subject);
            intent.putExtra("info", info);
            startActivityForResult(intent, 1001);
            process = false;
        } catch (Exception e) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        process = true;
    }

    private Notification buildNotification(String title, String text) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel contentChannel = new NotificationChannel(
                    "content_channel", "Things near you", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(contentChannel);
        }

        return new NotificationCompat.Builder(this, "content_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setOngoing(true)
                .setVibrate(new long[] { 300, 100 })
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
    }
}
