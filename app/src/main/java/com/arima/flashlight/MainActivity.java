package com.arima.flashlight;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Process;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private int mBackKeyPressedTimes = 0;
    private static int SOS_SIGNAL = 3;
    private static Long SOS_SIGNAL_INTERVAL_SHORT = 600L;
    private static Long SOS_SIGNAL_INTERVAL_LONG = 1800L;
    private boolean isFirstOpen = false;
    private RelativeLayout mBgLight;
    private CameraManager mCameraManager;
    private NotificationManager mNotificationManager;
    private Button mTorchBtn;
    private Button mSosBtn;
    private FlightListener mTorchListener;
    private FlightListener mSosListener;
    private CameraManager.TorchCallback mTorchCallback;
    private SosThread mSosThread = null;
    private AlertDialog.Builder mBuilder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        RelativeLayout.LayoutParams mTorchLayoutParams = new RelativeLayout.LayoutParams(-2, -2);
        RelativeLayout.LayoutParams mSosLayoutParams = new RelativeLayout.LayoutParams(-2, -2);
        mTorchLayoutParams.setMargins((int) (0.435F * size.x), (int) (0.46F * size.y), 0, 0);
        mSosLayoutParams.setMargins((int) (0.435F * size.x), (int) (0.68F * size.y), 0, 0);

        mBgLight = (RelativeLayout) findViewById(R.id.torch);
        mTorchBtn = (Button) findViewById(R.id.btn_torch);
        mTorchBtn.setLayoutParams(mTorchLayoutParams);
        mSosBtn = (Button) findViewById(R.id.btn_sos);
        mSosBtn.setLayoutParams(mSosLayoutParams);
        if (!hasBackFacingCamera()) {
            myAlertDialog(R.string.camera_no_support);
            return;
        }
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            myAlertDialog(R.string.flash_no_support);
            return;
        }
        mCameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        mTorchListener = new FlightListener();
        mSosListener = new FlightListener();
        mTorchBtn.setOnClickListener(mTorchListener);
        mSosBtn.setOnClickListener(mSosListener);

        mTorchCallback = new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeChanged(String cameraId, boolean enabled) {
                super.onTorchModeChanged(cameraId, enabled);
                if (enabled) {
                    mBgLight.setBackgroundResource(R.drawable.shou_on);
                    if (!mSosListener.isOpen) {
                        mTorchBtn.setBackgroundResource(R.drawable.turn_on);
                        mTorchListener.isOpen = enabled;
                    }
                } else {
                    mBgLight.setBackgroundResource(R.drawable.shou_off);
                    mTorchBtn.setBackgroundResource(R.drawable.turn_off);
                    mTorchListener.isOpen = enabled;
                }
            }
        };
        mCameraManager.registerTorchCallback(mTorchCallback, null);
        showNotification();
    }

    private void myAlertDialog(int messageId) {
        mBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(messageId)
                .setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        finish();
                        Process.killProcess(Process.myPid());
                    }
                });
        mBuilder.setCancelable(false);
        mBuilder.show();
    }

    private static boolean checkCameraFacing(final int facing) {
        if (getSdkVersion() < Build.VERSION_CODES.GINGERBREAD) {
            return false;
        }
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasBackFacingCamera() {
        final int CAMERA_FACING_BACK = 0;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    public static int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }

    @Override
    protected void onResume() {
        super.onResume();
        fullScreen();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!isFirstOpen) {
            AsyncTask mAsyncTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    return null;
                }

                @Override
                protected void onPostExecute(Object o) {
                    super.onPostExecute(o);
                    openTorch();
                    mTorchListener.isOpen = true;
                }
            };
            mAsyncTask.execute();
            isFirstOpen = true;
        }
    }

    private void openLight() {
        try {
            String[] list = mCameraManager.getCameraIdList();
            mCameraManager.setTorchMode(list[0], true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeLight() {
        try {
            String[] list = mCameraManager.getCameraIdList();
            mCameraManager.setTorchMode(list[0], false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openTorch() {
        if (mSosListener != null) {
            mSosListener.isOpen = false;
            closeSos();
        }
        mTorchBtn.setBackgroundResource(R.drawable.turn_on);
        mBgLight.setBackgroundResource(R.drawable.shou_on);
        openLight();
    }

    private void closeTorch() {
        mTorchBtn.setBackgroundResource(R.drawable.turn_off);
        mBgLight.setBackgroundResource(R.drawable.shou_off);
        closeLight();
    }

    private void openSos() {
        mTorchListener.isOpen = false;
        mTorchBtn.setBackgroundResource(R.drawable.turn_off);
        mSosBtn.setBackgroundResource(R.drawable.sos_on);
        mBgLight.setBackgroundResource(R.drawable.shou_on);
        mSosThread = new SosThread();
        mSosThread.start();
    }

    private void closeSos() {
        if (mSosThread != null) {
            mSosThread.stopThread();
            mSosThread = null;
        }
        mSosBtn.setBackgroundResource(R.drawable.sos_off);
        mBgLight.setBackgroundResource(R.drawable.shou_off);
    }

    private class FlightListener implements View.OnClickListener {
        private boolean isOpen;

        FlightListener() {
        }

        @Override
        public void onClick(View view) {
            if (isOpen) {
                if (view.getId() == R.id.btn_torch) {
                    closeTorch();
                } else {
                    closeSos();
                }
                isOpen = false;
                return;
            }
            isOpen = true;
            if (view.getId() == R.id.btn_torch) {
                openTorch();
            } else {
                openSos();
            }
        }
    }

    private class SosThread extends Thread {
        private int i;
        private int j;
        private int k;
        private boolean isStop = false;

        SosThread() {
        }

        private void stopThread() {
            isStop = true;
        }

        private void sleepThread(long t) {
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void sendSignal(int signal, long interval) {
            while (!isStop && signal < SOS_SIGNAL) {
                openLight();
                sleepThread(interval);
                if (isStop && mTorchListener.isOpen) {
                    break;
                }
                closeLight();
                sleepThread(interval);
                signal++;
            }
        }

        @Override
        public void run() {
            if (isStop) {
                return;
            }
            while (!isStop) {
                i = 0;
                j = 0;
                k = 0;
                sendSignal(i, SOS_SIGNAL_INTERVAL_SHORT);
                sleepThread(SOS_SIGNAL_INTERVAL_LONG);
                sendSignal(j, SOS_SIGNAL_INTERVAL_LONG);
                sendSignal(k, SOS_SIGNAL_INTERVAL_SHORT);
                sleepThread(SOS_SIGNAL_INTERVAL_SHORT);
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
    protected void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancel(0);
    }

    @Override
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
                mCameraManager.unregisterTorchCallback(mTorchCallback);
                closeLight();
                finish();
                Process.killProcess(Process.myPid());
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
