package euphoria.psycho.browser.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.CompletableFuture;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.Share;
import euphoria.psycho.browser.base.ThreadUtils;

public class ServerActivity extends Activity {
    private TextView mTextView;
    private Handler mHandler;
    private ProgressBar mProgressBar;

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

                }
            } else {
                try {
                    Share.copyAssetFile(this, "static/" + f, fileName);
                } catch (IOException e) {
                }
            }

        }


    }

    private void initialize() {
        mHandler = new Handler();
        new Thread(() -> {
            String ip = Share.getDeviceIP(ServerActivity.this);
            checkStaticFiles();
            Intent serverService = new Intent(this, ServerService.class);
            startService(serverService);
            mHandler.post(() -> {
                mProgressBar.setVisibility(View.GONE);
                mTextView.setText(String.format("http://%s:%s", ip, "12345"));
                mTextView.setVisibility(View.VISIBLE);
            });
        }).start();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_server);
        mTextView = findViewById(R.id.title);
        mProgressBar = findViewById(R.id.progress_circular);
        this.initialize();
    }


    @Override
    protected void onResume() {
        super.onResume();


    }
}
