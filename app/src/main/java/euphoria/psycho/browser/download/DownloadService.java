package euphoria.psycho.browser.download;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;

public class DownloadService extends Service {
    Executor mExecutor;
    DownloadDatabase mDatabase;
    private static final String DATABASE_NAME = "download.db";

    public static final String ACTION_DOWNLOAD_ADD = "download_add";
    public static final String EXTRA_URI = "uri";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutor = Executors.newCachedThreadPool();
        File database = new File(getExternalCacheDir(), DATABASE_NAME);
        Log.e("TAG/", "Debug: onCreate, \n" + database);
        mDatabase = new DownloadDatabase(this, database.getAbsolutePath());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {

                    case ACTION_DOWNLOAD_ADD:
                        String uri = intent.getStringExtra(EXTRA_URI);
                        addDownloadTask(uri);
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void addDownloadTask(String uri) {
        File download = new File(Environment.getExternalStorageDirectory(), "Downloads");
        if (!download.exists()) {
            download.mkdir();
        }
        download = new File(download, DownloadHelper.getFileNameFromUri(uri));
        DownloadInfo downloadInfo = new DownloadInfo(
                DownloadHelper.STATUS_RUNNING,
                0,
                0,
                uri,
                null,
                download.getAbsolutePath(),
                null,
                0
        );
        mDatabase.addDownloadInfo(downloadInfo);
        Log.e("TAG/", "Debug: addDownloadTask, \n" + download);
    }


}
