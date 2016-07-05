package com.arima.flashlight;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private SosButton sosListener;
    private int back = 0;
    private RelativeLayout bgLight = null;
    private CameraManager mCameraManager;
    private boolean hasOpen = false;
    private Button lightBtn = null;
    private Mybutton mButton = null;
    private Button sosBtn = null;
    private NotificationManager mNotificationManager;
    private CameraDevice cameraDevice;
    private SurfaceHolder holder;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        RelativeLayout.LayoutParams localLayoutParams1 = new RelativeLayout.LayoutParams(-2, -2);
        RelativeLayout.LayoutParams localLayoutParams2 = new RelativeLayout.LayoutParams(-2, -2);
        localLayoutParams1.setMargins((int) (0.42F * size.x), (int) (0.68F * size.y), 0, 0);
        localLayoutParams2.setMargins((int) (0.42F * size.x), (int) (0.46F * size.y), 0, 0);

        bgLight = ((RelativeLayout) findViewById(R.id.btn_light));
        lightBtn = ((Button) findViewById(R.id.button1));
        mButton = new Mybutton(true);
        lightBtn.setOnClickListener(mButton);
        lightBtn.setLayoutParams(localLayoutParams2);
        sosBtn = ((Button) findViewById(R.id.button2));
        sosListener = new SosButton(false);
        sosBtn.setOnClickListener(sosListener);
        sosBtn.setLayoutParams(localLayoutParams1);
        showNotification();
    }


    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasOpen) {
            new OpenLightTask().execute();
            new OpenLightTaskT().execute();
            hasOpen = true;
        }
    }

    private void openLight() {
        try {
            //mCameraManager.setTorchMode("0", true);
            String[] list = mCameraManager.getCameraIdList();
            mCameraManager.setTorchMode(list[0], true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeLight() {
        try {
            //mCameraManager.setTorchMode("0", false);
            String[] list = mCameraManager.getCameraIdList();
            mCameraManager.setTorchMode(list[0], false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private class Mybutton implements View.OnClickListener {
        public boolean open;

        Mybutton(boolean bool) {
            open = bool;
            if (open) {
                openIt();
                return;
            }
            closeIt();
        }

        void closeIt() {
            lightBtn.setBackgroundResource(R.drawable.turn_off);
            bgLight.setBackgroundResource(R.drawable.shou_off);
            closeLight();
        }

        void openIt() {
            if (sosListener != null) {
                sosListener.close();
            }
            bgLight.setBackgroundResource(R.drawable.shou_on);
            lightBtn.setBackgroundResource(R.drawable.turn_on);
            openLight();
        }

        public void onClick(View v) {
            if (open) {
                closeIt();
                open = false;
                return;
            }
            open = true;
            openIt();
        }
    }

    private class OpenLightTask extends AsyncTask<Void, Void, Void> {
        OpenLightTask() {
        }

        protected Void doInBackground(Void[] voids) {

            try {
                Thread.sleep(300L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            lightBtn.setBackgroundResource(R.drawable.turn_on);
            bgLight.setBackgroundResource(R.drawable.shou_on);
            openLight();
            mButton.open = true;
        }
    }

    private class OpenLightTaskT extends AsyncTask<Void, Void, Void> {
        OpenLightTaskT() {
        }

        protected Void doInBackground(Void[] voids) {
            try {
                Thread.sleep(600L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            lightBtn.setBackgroundResource(R.drawable.turn_on);
            bgLight.setBackgroundResource(R.drawable.shou_on);
            openLight();
            mButton.open = true;
        }
    }

    private class SosButton implements View.OnClickListener {
        public boolean open;
        private SosThread sosThread = null;

        SosButton(boolean bool) {
            open = bool;
            if (open) {
                openIt();
                return;
            }
            closeIt();
        }

        void close() {
            closeIt();
            open = false;
        }

        void closeIt() {
            bgLight.setBackgroundResource(R.drawable.shou_off);
            sosBtn.setBackgroundResource(R.drawable.sos_off);
            if (sosThread != null) {
                sosThread.Stop();
                sosThread = null;
            }
        }

        void openIt() {
            bgLight.setBackgroundResource(R.drawable.shou_on);
            lightBtn.setBackgroundResource(R.drawable.turn_off);
            sosBtn.setBackgroundResource(R.drawable.sos_on);
            sosThread = new SosThread();
            sosThread.start();
        }

        public void onClick(View view) {
            if (open) {
                closeIt();
                open = false;
                return;
            }
            open = true;
            openIt();
        }
    }

    private class SosThread extends Thread {
        int i;
        int j;
        int k;

        private boolean stopFlag = false;

        SosThread() {
        }

        void Stop() {
            stopFlag = true;
        }

        void sleepExt(long t) {
            try {
                Thread.sleep(t);
            } catch (Exception e) {
                // TODO: handle exception
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
                while (!stopFlag && i < 3) {
                    openLight();
                    sleepExt(300L);
                    closeLight();
                    sleepExt(300L);
                    i++;
                }

                sleepExt(800L);

                while (j < 3 && !stopFlag) {
                    openLight();
                    sleepExt(800L);
                    closeLight();
                    sleepExt(800L);
                    j++;
                }

                while (!stopFlag && k < 3) {
                    openLight();
                    sleepExt(300L);
                    closeLight();
                    sleepExt(300L);
                    k++;
                }
                sleepExt(300L);
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
        bgLight.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        int newUiOptions = getWindow().getDecorView().getSystemUiVisibility();
        //int newUiOptions = uiOptions;
        newUiOptions &= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullScreen();
    }


    private void Myback() {
        closeLight();
        mNotificationManager.cancel(0);
        finish();
        Process.killProcess(Process.myPid());
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            back += 1;
            switch (back) {
                case 1:
                    Toast.makeText(this, getString(R.string.again_exit), Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    back = 0;
                    Myback();
                    break;
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
