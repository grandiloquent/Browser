package euphoria.psycho.browser.app;

import android.Manifest.permission;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.base.Share;

public class DownloadActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private static final int REQUEST_PERMISSIONS_CODE = 1;

    private void checkStaticFiles() {


        String[] files = new String[]{
                "video.css",
                "video.js",
                "videos.html",

        };
        Share.createDirectoryIfNotExists(Share.getExternalStoragePath("FileServer"));

        for (String f : files) {
            String fileName = Share.getExternalStoragePath("FileServer/" + f);

            if (Share.isFile(fileName)) {
                if (!fileName.endsWith(".css")
                        && !fileName.endsWith(".js")
                        && !fileName.endsWith(".html")) {
                    continue;
                }
                try {
                    String assetMd5 = Share.getMD5Checksum(getAssets().open("static/" + f));
                    if (Share.getMD5Checksum(fileName).equals(assetMd5)) {
                        continue;
                    }
                } catch (Exception e) {

                    Log.e("TAG/" + DownloadActivity.this.getClass().getSimpleName(), "Error: checkStaticFiles, " + e.getMessage() + " " + e.getCause());

                }
            } else {
                try {
                    Share.copyAssetFile(this, "static/" + f, fileName);
                } catch (IOException e) {
                    Log.e("TAG/" + DownloadActivity.this.getClass().getSimpleName(), "Error: checkStaticFiles, " + e.getMessage() + " " + e.getCause());
                }
            }

        }


    }

    private void initialize() {
        checkStaticFiles();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        requestPermissions(new String[]{
                permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_PERMISSIONS_CODE);


        new Thread(() -> {
            try {
                CharSequence twitterUrl = Share.getClipboardString();
                if (twitterUrl != null) {
                    String id = Share.substringAfterLast(twitterUrl.toString(), "/");
                    if (Share.isDigits(id)) {
                        List<TwitterVideo> twitterVideos = TwitterHelper.extractTwitterVideo(id);
                        mHandler.post(() -> {
                            TwitterHelper.showDialog(twitterVideos, DownloadActivity.this);
                        });
                    }

                }
            } catch (Exception e) {

                Log.e("TAG/" + DownloadActivity.this.getClass().getSimpleName(), "Error: onCreate, " + e.getMessage() + " " + e.getCause());

            }
        }).start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        initialize();
    }

    private class MessageThread extends Thread implements Handler.Callback {
        private Handler mHandler;

        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }

        @Override
        public void run() {
            Looper.prepare();
            mHandler = new Handler(this);
            Looper.loop();
        }

        public void post(Runnable r) {
            mHandler.post(r);
        }

        public void quit() {
            Objects.requireNonNull(Looper.myLooper()).quit();
        }
    }


}
