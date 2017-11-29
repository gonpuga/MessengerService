package com.example.messengerservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

import java.lang.ref.WeakReference;

public class MessengerService extends Service {
    private static String LOG_TAG = "MessengerService";
    private Chronometer mChronometer;
    static final int MSG_GET_TIMESTAMP = 1000;

    static class BoundServiceHandler extends Handler {
        private final WeakReference<MessengerService> mService;

        public BoundServiceHandler(MessengerService service) {
            mService = new WeakReference<MessengerService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_TIMESTAMP:
                    long elapsedMillis = SystemClock.elapsedRealtime()
                            - mService.get().mChronometer.getBase();
                    int hours = (int) (elapsedMillis / 3600000);
                    int minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
                    int seconds = (int) (elapsedMillis - hours * 3600000 - minutes * 60000) / 1000;
                    int millis = (int) (elapsedMillis - hours * 3600000 - minutes
                            * 60000 - seconds * 1000);
                    Messenger activityMessenger = msg.replyTo;
                    Bundle b = new Bundle();
                    b.putString("timestamp",
                            String.format("%02d:%02d:%02d:%03d", hours, minutes, seconds, millis));
                    Message replyMsg = Message.obtain(null, MSG_GET_TIMESTAMP);
                    replyMsg.setData(b);
                    try {
                        activityMessenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new BoundServiceHandler(this));

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "in onCreate");
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        return mMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "in onDestroy");
        mChronometer.stop();
    }
}