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
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory;
import com.estimote.proximity.estimote.ProximityContentManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MainActivity extends AppCompatActivity {

    private ProximityContentManager proximityContentManager;
    Location location;
    boolean process = true;
    NotificationManager notificationManager;
    public LinkedBlockingDeque<String> queue = new LinkedBlockingDeque();

    public LocationMetaAdapter myadapter;
    ListView lv_item;
    ArrayList<LocationMeta> metaArrayList;

    public static String HOST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HOST = getResources().getString(R.string.SERVER_HOST);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        this.notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        lv_item = findViewById(R.id.lv_item);
        Thread thread = new Thread(() -> {
            try {
                metaArrayList = Location.getNameList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        myadapter = new LocationMetaAdapter(getApplicationContext(), R.layout.item, metaArrayList);
        lv_item.setAdapter(myadapter);

        lv_item.setOnItemClickListener((parent, view, position, id) -> {
            Thread thread1 = new Thread(() -> {
                try {
                    ShowInfo(metaArrayList.get(position).id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            thread1.start();
            try {
                thread1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


        location = new Location();

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
                    while (!proximityContentManager.queue.isEmpty()) {
                        beacon_id = proximityContentManager.queue.take();
                    }
                    Notification notification = buildNotification(beacon_id);
                    if (notification != null) {
                        notificationManager.notify(1, notification);
                    }
                    Thread.sleep(1000);
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
            JSONObject json = location.getData(id);

            String name = json.getString("name");
            String subject = json.getString("subject");
            String info = json.getString("info");

            Intent intent = new Intent(getApplicationContext(), ContentActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("subject", subject);
            intent.putExtra("info", info);
            startActivityForResult(intent, 1001);
            process = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        process = true;
    }

    private Notification buildNotification(String beacon_id) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel contentChannel = new NotificationChannel(
                    "content_channel", "Things near you", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(contentChannel);
        }

        JSONObject json = location.getData(beacon_id);
        if (json == null) {
            return null;
        }
        String name = null, subject = null, info = null, head = null, body = null;
        try {
            name = json.getString("name");
            subject = json.getString("subject");
            info = json.getString("info");
            head = json.getString("notification_head");
            body = json.getString("notification_body");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Intent intent = new Intent(this, ContentActivity.class);
        intent.putExtra("name", name);
        intent.putExtra("subject", subject);
        intent.putExtra("info", info);

        return new NotificationCompat.Builder(this, "content_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(head)
                .setContentText(body)
                .setOngoing(true)
                .setVibrate(new long[]{300, 100})
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();
    }
}
