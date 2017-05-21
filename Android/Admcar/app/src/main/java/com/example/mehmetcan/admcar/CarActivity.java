package com.example.mehmetcan.admcar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class CarActivity extends Activity implements SurfaceHolder.Callback {

    public static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothAdapter phoneBluetooth = null;
    public BluetoothSocket btSocket = null;
    public String address = null;
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    private ImageButton goForwardBtn;
    private ImageButton goBackwardBtn;
    private ImageButton goLeftBtn;
    private ImageButton goRightBtn;
    private SurfaceView surfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MjpegStream mMjpegStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_car);

        // Get the address of your bluetooth device.
        Intent newIntent = getIntent();
        address = newIntent.getStringExtra(BluetoothList.EXTRA_ADDRESS);

        new ConnectBluetooth().execute(); // Bluetooth connection

        this.initializeComponents();
        this.initializeListeners();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        // Start the video stream next time the application is ran.
        this.finish();
    }

    /**
     * Initialize all view components.
     */
    private void initializeComponents() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = surfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        this.goForwardBtn = (ImageButton) findViewById(R.id.goForwardBtn);
        this.goBackwardBtn = (ImageButton) findViewById(R.id.goBackwardBtn);
        this.goLeftBtn = (ImageButton) findViewById(R.id.goLeftBtn);
        this.goRightBtn = (ImageButton) findViewById(R.id.goRightBtn);
    }

    /**
     * Bind all button listeners. (called during the initialization)
     */
    private void initializeListeners() {
        /*
        ******** Forward **********
        */
        this.goForwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        goForward();
                        return true;
                    case MotionEvent.ACTION_UP:
                        stop();
                        return true;
                }
                return false;
            }
        });

        /*
        ******** Backward **********
        */
        this.goBackwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        goBackward();
                        return true;
                    case MotionEvent.ACTION_UP:
                        stop();
                        return true;
                }
                return false;
            }
        });

        /*
        ******** Left **********
        */
        this.goLeftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        goLeft();
                        return true;
                    case MotionEvent.ACTION_UP:
                        stop();
                        return true;
                }
                return false;
            }
        });

        /*
        ******** Right **********
        */
        this.goRightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        goRight();
                        return true;
                    case MotionEvent.ACTION_UP:
                        stop();
                        return true;
                }
                return false;
            }
        });

    }

    /**
     * Send a request to the car to go forward.
     */
    private void goForward() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(10);
            } catch (IOException e) {
                message("Bluetooth interrupt");
            }
        }
    }

    /**
     * Send a request to the car to go backward.
     */
    private void goBackward() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(20);
            } catch (IOException e) {
                message("Bluetooth interrupt");
            }
        }
    }

    /**
     * Send a request to the car to go to the left.
     */
    private void goLeft() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(30);
            } catch (IOException e) {
                message("Bluetooth interrupt");
            }
        }
    }

    /**
     * Send a request to the car to go to the right.
     */
    private void goRight() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(40);
            } catch (IOException e) {
                message("Bluetooth interrupt");
            }
        }
    }

    /**
     * Send a request to the car to stop.
     */
    private void stop() {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(50);
            } catch (IOException e) {
                message("Bluetooth interrupt");
            }
        }
    }


    /**
     * Connecting and sending data via socket.
     */
    private class ConnectBluetooth extends AsyncTask<Void, Void, Void> {
        private boolean connectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(CarActivity.this, "Connecting...", "Please wait");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
                    phoneBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = phoneBluetooth.getRemoteDevice(address);
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                connectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!connectSuccess) {
                message("Connection error, Please try again");
                finish();
            } else {
                message("Connection successful");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }


    /**
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * THIS PART INCLUDES CAMERA DESCRIPTION
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMjpegStream = new MjpegStream("http://192.168.1.1:8080?action=stream");
        mMjpegStream.setCallback(new MjpegStream.Callback() {
            public void onFrameRead(Bitmap bitmap) {
                Canvas canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    if (canvas != null) {
                        try {
                            canvas.drawBitmap(bitmap, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
                        } catch (Exception e) {
                        }
                    }

                } finally {
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        });
        mMjpegStream.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mMjpegStream.stop();
    }

    /**
     * Error message method.
     *
     * @param msg
     */
    private void message(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
}