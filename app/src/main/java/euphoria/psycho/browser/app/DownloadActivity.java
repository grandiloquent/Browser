package euphoria.psycho.browser.app;

import android.Manifest;
import android.Manifest.permission;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.Share;

public class DownloadActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        requestPermissions(new String[]{
                permission.WRITE_EXTERNAL_STORAGE
        }, REQUEST_PERMISSIONS_CODE);


        //

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        initialize();
    }

    private void initialize() {
        checkStaticFiles();
        NativeHelper.startServer(Share.getDeviceIP(this), "12345", Share.getExternalStoragePath("FileServer"));
    }

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
                if (!fileName.endsWith(".css") && !fileName.endsWith(".js") && !fileName.endsWith(".html")) {
                    break;
                }
                try {
                    String assetMd5 = Share.getMD5Checksum(getAssets().open("static/" + f));
                    if (Share.getMD5Checksum(fileName).equals(assetMd5)) {
                        break;
                    }
                } catch (Exception e) {

                    Log.e("TAG/" + DownloadActivity.this.getClass().getSimpleName(), "Error: checkStaticFiles, " + e.getMessage() + " " + e.getCause());

                }
            }
            try {
                Share.copyAssetFile(this, "static/" + f, fileName);
            } catch (IOException e) {
                Log.e("TAG/" + DownloadActivity.this.getClass().getSimpleName(), "Error: checkStaticFiles, " + e.getMessage() + " " + e.getCause());
            }
        }


    }

}
