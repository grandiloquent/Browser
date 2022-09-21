package euphoria.psycho.browser.file;

import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;



import java.io.File;
import java.io.FileFilter;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import euphoria.psycho.browser.R;

public class VideoService extends Service {
    private static final String TAG = "TAG/" + VideoService.class.getSimpleName();
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
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            updateForegroundNotification(R.string.server_running);
        }
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mCpuWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, TAG);
        mCpuWakeLock.acquire(60 * 60 * 1000L /*60 minutes*/);
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
        if (intent != null) {
            String directory = intent.getStringExtra("directory");
            new Thread(() -> {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                covertVideos(directory);
            }).start();
        }
        return START_STICKY;
    }

    private void covertVideo(String source, String destination) {
//        String arg = String.format("-i \"%s\" -c:v mpeg4 \"%s\"", source, destination);
//        FFmpegSession session = FFmpegKit.execute(arg);
//        if (ReturnCode.isSuccess(session.getReturnCode())) {
//            File f = new File(source);
//            File dir = f.getParentFile();
//            dir = new File(dir, "Recycle");
//            if (!dir.exists()) {
//                dir.mkdir();
//            }
//            f.renameTo(new File(dir, f.getName()));
//        }
    }

    private void covertVideos(String directory) {
        File[] files = new File(directory).listFiles(
                file -> file.isFile() && file.getName().endsWith(".mp4")
        );
        if (files == null) return;
        for (File file : files) {
            File st = new File("/storage/FD12-1F1D/Movies", file.getName());
            if (st.exists()) continue;
            covertVideo(file.getAbsolutePath(),
                    st.getAbsolutePath());
        }
    }

    @RequiresApi(api = VERSION_CODES.O)
    private void updateForegroundNotification(final int message) {
        final String NOTIFICATION_CHANNEL_ID = "Video Service";
        mNotificationManager.createNotificationChannel(new NotificationChannel(
                NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                NotificationManager.IMPORTANCE_DEFAULT));
        startForeground(1, new Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_cloud_queue)
                .setContentText(getString(message))
                //.setContentIntent(mConfigureIntent)
                .build());
    }
}
