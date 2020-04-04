package com.estimote.proximity.estimote;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.estimote.proximity.Location;
import com.estimote.proximity.MainActivity;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.estimote.proximity_sdk.api.ProximityZone;
import com.estimote.proximity_sdk.api.ProximityZoneBuilder;
import com.estimote.proximity_sdk.api.ProximityZoneContext;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class ProximityContentManager {

    private Context context;
    private EstimoteCloudCredentials cloudCredentials;
    private ProximityObserver.Handler proximityObserverHandler;
    public LinkedBlockingDeque<String> queue = new LinkedBlockingDeque();

    private int timeout = 30000;
    private int distance = 1;

    Map<String, Long> limitMap = new HashMap<>();

    public ProximityContentManager(Context context, EstimoteCloudCredentials cloudCredentials) {
        this.context = context;
        this.cloudCredentials = cloudCredentials;

        Thread thread = new Thread(() -> {
            try {
                timeout = Integer.parseInt(Location.download(MainActivity.HOST + "/timeout.php"));
                distance = Integer.parseInt(Location.download(MainActivity.HOST + "/distance.php"));
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

    }

    public void start() {

        ProximityObserver proximityObserver = new ProximityObserverBuilder(context, cloudCredentials)
                .onError(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        Log.e("app", "proximity observer error: " + throwable);
                        return null;
                    }
                })
                .withBalancedPowerMode()
                .build();

        ProximityZone zone = new ProximityZoneBuilder()
                .forTag("hyun-sun-suh-s-proximity-f-ote")
                .inCustomRange(distance)
                .onContextChange(new Function1<Set<? extends ProximityZoneContext>, Unit>() {
                    @Override
                    public Unit invoke(Set<? extends ProximityZoneContext> contexts) {
                        long currentTimestamp = new Date().getTime();
                        for (ProximityZoneContext proximityContext : contexts) {
                            String id = proximityContext.getDeviceId();
                            if (!limitMap.containsKey(id)) {
                                limitMap.put(id, 0L);
                            }
                            if (limitMap.get(id) <= currentTimestamp) {
                                queue.add(id);
                                limitMap.put(id, currentTimestamp + timeout);
                            }
                        }
                        return null;
                    }
                })
                .build();

        proximityObserverHandler = proximityObserver.startObserving(zone);
    }

    public void stop() {
        proximityObserverHandler.stop();
    }
}
