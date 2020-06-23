package euphoria.psycho.browser.app;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import androidx.annotation.Nullable;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.Share;
public class ServerActivity extends Activity {
    private TextView mTextView;
    private Handler mHandler;
    private ProgressBar mProgressBar;
    private void checkStaticFiles() {
        String[] files = new String[]{
                "share.js",
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
            }
            try {
                Share.copyAssetFile(this, "static/" + f, fileName);
            } catch (IOException e) {
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
        File videoDirectory = new File(Environment.getExternalStorageDirectory(), "Videos");
        if (!videoDirectory.isDirectory()) {
            return;
        }
        List<File> files = listFilesRecursive(videoDirectory);
        File imagesDirectory = new File(Share.getExternalStoragePath("FileServer"), "images");
        if (!imagesDirectory.isDirectory()) {
            imagesDirectory.mkdirs();
        }
        for (File file : files) {
            if (file.getName().endsWith(".mp4")) {
                try {
                    String filename = Share.md5(file.getAbsolutePath());
                    File target = new File(imagesDirectory, filename + ".jpg");
                    if (!target.isFile()) {
                        Bitmap bitmap = Share.createVideoThumbnail(file.getAbsolutePath());
                        Share.writeAllBytes(target.getAbsolutePath(), Share.compressToBytes(bitmap));
                    }
                } catch (Exception e) {
                }
            }
        }
    }
    static List<File> listFilesRecursive(File startDir) {
        final ArrayList<File> files = new ArrayList<>();
        final LinkedList<File> dirs = new LinkedList<File>();
        dirs.add(startDir);
        while (!dirs.isEmpty()) {
            final File dir = dirs.removeFirst();
            final File[] children = dir.listFiles();
            if (children == null) continue;
            for (File child : children) {
                if (child.isDirectory()) {
                    dirs.add(child);
                } else if (child.isFile()) {
                    files.add(child);
                }
            }
        }
        return files;
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}