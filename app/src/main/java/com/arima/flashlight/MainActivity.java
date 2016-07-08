package com.arima.flashlight;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private int mBackKeyPressedTimes = 0;
    private int SOS_SIGNAL = 3;
    private Long SOS_SIGNAL_SHORT = 600L;
    private Long SOS_SIGNAL_LONG = 1800L;
    private boolean IsOpen = false;
    private RelativeLayout mBgLight;
    private CameraManager mCameraManager;
    private NotificationManager mNotificationManager;
    private Button mTorchBtn;
    private Button mSosBtn;
    private FlightListener mTorchListener;
    private FlightListener mSosListener;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        RelativeLayout.LayoutParams mTorchLayoutParams = new RelativeLayout.LayoutParams(-2, -2);
        RelativeLayout.LayoutParams mSosLayoutParams = new RelativeLayout.LayoutParams(-2, -2);
        mTorchLayoutParams.setMargins((int) (0.42F * size.x), (int) (0.46F * size.y), 0, 0);
        mSosLayoutParams.setMargins((int) (0.42F * size.x), (int) (0.68F * size.y), 0, 0);

        mBgLight = ((RelativeLayout) findViewById(R.id.torch));
        mTorchBtn = ((Button) findViewById(R.id.btn_torch));
        mTorchBtn.setLayoutParams(mTorchLayoutParams);
        mSosBtn = ((Button) findViewById(R.id.btn_sos));
        mSosBtn.setLayoutParams(mSosLayoutParams);
        mTorchListener = new FlightListener(true, mTorchBtn);
        mSosListener = new FlightListener(false, mSosBtn);
        mTorchBtn.setOnClickListener(mTorchListener);
        mSosBtn.setOnClickListener(mSosListener);
        showNotification();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!IsOpen) {
            new OpenLightTask().execute();
            IsOpen = true;
        }
    }

    private void openTorch() {
        try {
            String[] list = mCameraManager.getCameraIdList();
            mCameraManager.setTorchMode(list[0], true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeTorch() {
        try {
            String[] list = mCameraManager.getCameraIdList();
            mCameraManager.setTorchMode(list[0], false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private class OpenLightTask extends AsyncTask<Void, Void, Void> {
        OpenLightTask() {
        }

        protected Void doInBackground(Void[] voids) {
            return null;
        }

        protected void onPostExecute(Void v) {
            mTorchBtn.setBackgroundResource(R.drawable.turn_on);
            mBgLight.setBackgroundResource(R.drawable.shou_on);
            openTorch();
            mTorchListener.open = true;
        }
    }

    private class FlightListener implements View.OnClickListener {
        public boolean open;
        private SosThread mSosThread = null;

        FlightListener(boolean bool, View v) {
            open = bool;
            if (open) {
                open(v);
                return;
            }
            close(v);
        }

        private void open(View v) {
            switch (v.getId()) {
                case R.id.btn_torch:
                    if (mSosListener != null) {
                        mSosListener.close(v);
                    }
                    mTorchBtn.setBackgroundResource(R.drawable.turn_on);
                    mBgLight.setBackgroundResource(R.drawable.shou_on);
                    openTorch();
                    break;
                case R.id.btn_sos:
                    mTorchBtn.setBackgroundResource(R.drawable.turn_off);
                    mSosBtn.setBackgroundResource(R.drawable.sos_on);
                    mBgLight.setBackgroundResource(R.drawable.shou_on);
                    mSosThread = new SosThread();
                    mSosThread.start();
                    break;
            }
        }

        private void close(View v) {
            switch (v.getId()) {
                case R.id.btn_torch:
                    mTorchBtn.setBackgroundResource(R.drawable.turn_off);
                    mBgLight.setBackgroundResource(R.drawable.shou_off);
                    closeTorch();
                    break;
                case R.id.btn_sos:
                    mSosBtn.setBackgroundResource(R.drawable.sos_off);
                    mBgLight.setBackgroundResource(R.drawable.shou_off);
                    if (mSosThread != null) {
                        mSosThread.stopThread();
                        mSosThread = null;
                    }
                    break;
            }
        }

        public void onClick(View v) {
            if (open) {
                close(v);
                open = false;
                return;
            }
            open = true;
            open(v);
        }
    }

    private class SosThread extends Thread {
        private int i;
        private int j;
        private int k;
        private boolean stopFlag = false;

        SosThread() {
        }

        private void stopThread() {
            stopFlag = true;
        }

        private void sleepThread(long t) {
            try {
                Thread.sleep(t);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        public void run() {
            if (stopFlag) {
                return;
            }
            while (!stopFlag) {
                i = 0;
                j = 0;
                k = 0;
                while (!stopFlag && i < SOS_SIGNAL) {
                    openTorch();
                    sleepThread(SOS_SIGNAL_SHORT);
                    closeTorch();
                    sleepThread(SOS_SIGNAL_SHORT);
                    i++;
                }
                sleepThread(SOS_SIGNAL_LONG);
                while (j < SOS_SIGNAL && !stopFlag) {
                    openTorch();
                    sleepThread(SOS_SIGNAL_LONG);
                    closeTorch();
                    sleepThread(SOS_SIGNAL_LONG);
                    j++;
                }
                while (!stopFlag && k < SOS_SIGNAL) {
                    openTorch();
                    sleepThread(SOS_SIGNAL_SHORT);
                    closeTorch();
                    sleepThread(SOS_SIGNAL_SHORT);
                    k++;
                }
                sleepThread(SOS_SIGNAL_SHORT);
            }
        }
    }

    private void showNotification() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        PendingIntent contentIndent = PendingIntent.getActivity(MainActivity.this, 0, new Intent(MainActivity.this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        Notification mNotification = new Notification.Builder(MainActivity.this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.content))
                .setSmallIcon(R.drawable.ic_stat_flash)
                .setColor(getResources().getColor(R.color.colorNoti, null))
                .setOngoing(true)
                .setContentIntent(contentIndent)
                .build();
        mNotificationManager.notify(0, mNotification);
    }

    private void fullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullScreen();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (mBackKeyPressedTimes == 0) {
                mBackKeyPressedTimes = 1;
                Toast.makeText(this, getString(R.string.again_exit), Toast.LENGTH_SHORT).show();
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            mBackKeyPressedTimes = 0;
                        }
                    }
                }.start();
            } else {
                mBackKeyPressedTimes = 0;
                mNotificationManager.cancel(0);
                closeTorch();
                finish();
                Process.killProcess(Process.myPid());
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
