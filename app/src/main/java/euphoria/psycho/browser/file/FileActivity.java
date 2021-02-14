package euphoria.psycho.browser.file;

import android.Manifest.permission;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import euphoria.psycho.Share;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.FloatingService;
import euphoria.psycho.browser.app.LocalFileService;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.share.DialogUtils;

public class FileActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_CODE = 1 << 1;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private FileManager mFileManager;

    private void initialize() {
        setContentView(R.layout.activity_file);
        FrameLayout container = findViewById(R.id.container);
        mFileManager = new FileManager(this);
        container.addView(mFileManager.getView(), 0);

//        Intent intent = new Intent(this, MovieActivity.class);
//        intent.setData(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "/Videos/231.mp4")));
//        startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> needPermissions = new ArrayList<>();
        if (!ContextUtils.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE)) {
            needPermissions.add(permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!ContextUtils.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE)) {
            needPermissions.add(permission.READ_EXTERNAL_STORAGE);
        }

        // https://developer.android.com/training/data-storage/manage-all-files
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
        }

        checkStartPermissionRequest();

        // startService(new Intent(this, LocalFileService.class));

        if (needPermissions.size() > 0) {
            requestPermissions(needPermissions.toArray(new String[0]), REQUEST_PERMISSIONS_CODE);
        } else {
            initialize();
        }
        startService(new Intent(this, FloatingService.class));
        //createDebugFiles();
    }

    public boolean checkStartPermissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                return false; // above will start new Activity with proper app setting
            }
        }
        return true; // on lower OS versions granted during apk installation
    }

    @Override
    protected void onDestroy() {
        mFileManager.onDestroy();
        mFileManager = null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFileManager != null)
            mFileManager.onPause();
    }

    @Override
    public void onBackPressed() {

        if (!mFileManager.onBackPressed())
            super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "" + permissions[i], Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }
            initialize();
        }
    }

    private void createDebugFiles() {


        Log.e("TAG/", "Debug: createDebugFiles, \n" + Environment.getDataDirectory());

        File dir = new File(Environment.getExternalStorageDirectory(), "aaa");
        dir.mkdir();
        for (int i = 0; i < 10; i++) {
            File a = new File(dir, i + ".mp3");
            try {
                a.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            a = new File(dir, i + ".pdf");
            try {
                a.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            a = new File(dir, i + ".mp4");
            try {
                a.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}