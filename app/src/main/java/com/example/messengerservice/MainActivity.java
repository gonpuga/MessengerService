package com.example.messengerservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private Messenger mBoundServiceMessenger;
    private boolean mServiceConnected = false;
    private TextView mTimestampText;
    private final Messenger mActivityMessenger = new Messenger(
            new ActivityHandler(this));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTimestampText = (TextView) findViewById(R.id.timestamptext);
        Button printTimestampButton = (Button) findViewById(R.id.btnPrintTimeStamp);
        Button stopServiceButon = (Button) findViewById(R.id.btnStopService);
        printTimestampButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServiceConnected) {
                    try {
                        Message msg = Message.obtain(null,
                                MessengerService.MSG_GET_TIMESTAMP, 0, 0);
                        msg.replyTo = mActivityMessenger;
                        mBoundServiceMessenger.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        stopServiceButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mServiceConnected) {
                    unbindService(mServiceConnection);
                    mServiceConnected = false;
                }
                Intent intent = new Intent(MainActivity.this,
                        MessengerService.class);
                stopService(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MessengerService.class);
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceConnected) {
            unbindService(mServiceConnection);
            mServiceConnected = false;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBoundServiceMessenger = null;
            mServiceConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBoundServiceMessenger = new Messenger(service);
            mServiceConnected = true;
        }
    };

    static class ActivityHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public ActivityHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessengerService.MSG_GET_TIMESTAMP: {
                    mActivity.get().mTimestampText.setText(msg.getData().getString(
                            "timestamp"));
                }
            }
        }

    }
}