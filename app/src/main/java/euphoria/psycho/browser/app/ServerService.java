package euphoria.psycho.browser.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import androidx.annotation.Nullable;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.Share;

public class ServerService extends Service {
    private static final String TAG = "TAG/" + ServerService.class.getSimpleName();
    private WakeLock mCpuWakeLock;
    NotificationManager mNotificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        updateForegroundNotification(R.string.server_running);
        Log.e("TAG/", "Debug: onCreate, \n");

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
        mCpuWakeLock.acquire();
        NativeHelper.startServer(Share.getDeviceIP(this), "12345", Share.getExternalStoragePath("FileServer"), SettingsManager.getInstance().getVideoDirectory());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCpuWakeLock != null) {
            mCpuWakeLock.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("TAG/", "Debug: onStartCommand, \n");

        return START_STICKY;
    }

    private void updateForegroundNotification(final int message) {
        final String NOTIFICATION_CHANNEL_ID = "Server";

        mNotificationManager.createNotificationChannel(new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT));
        startForeground(1, new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_cloud_queue)
                .setContentText(getString(message))
                //.setContentIntent(mConfigureIntent)
                .build());
    }
}
