package euphoria.psycho.browser.app;

import android.Manifest.permission;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet.OnClickListener;
import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.base.Share;

public class DownloadActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_CODE = 1;
    private Handler mHandler = new Handler();
    private View mContainer;


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

    private void extractTwitterVideo() {
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
                runOnUiThread(() -> {
                    Share.showExceptionDialog(DownloadActivity.this, e);
                });
            }
        }).start();
    }


    private void extractYouTubeVideo() {
        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
//                if (ytFiles != null) {
//                    int itag = 22;
//                    String downloadUrl = ytFiles.get(itag).getUrl();
//
//                }

            }
        }.extract(Share.getClipboardString().toString(), true, true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mContainer = findViewById(R.id.container);

        requestPermissions(new String[]{
                permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_PERMISSIONS_CODE);

        new BottomSheet(this)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClicked(Pair<Integer, String> item) {
                        switch (item.first) {
                            case R.drawable.ic_twitter:
                                extractTwitterVideo();
                                break;
                            case R.drawable.ic_youtube:
                                extractYouTubeVideo();
                                break;
                        }
                    }
                })
                .showDialog();


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        initialize();
    }

    private class MessageThread extends Thread implements Handler.Callback {
        private Handler mHandler;

        public void post(Runnable r) {
            mHandler.post(r);
        }

        public void quit() {
            Objects.requireNonNull(Looper.myLooper()).quit();
        }

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
    }


}
