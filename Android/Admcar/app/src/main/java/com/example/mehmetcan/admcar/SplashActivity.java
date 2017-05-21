package com.example.mehmetcan.admcar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.mehmetcan.admcar.*;

/**
 * This class refers to intro screen of my application
 */
public class SplashActivity extends FragmentActivity {
    public Thread myThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);

        myThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(3000);
                    // Open new intent after elapsed time
                    Intent intent = new Intent(getApplicationContext(), BluetoothList.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }
}
