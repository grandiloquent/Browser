package euphoria.psycho.browser.file;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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

import euphoria.psycho.browser.R;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.share.DialogUtils;

public class FileActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_CODE = 1 << 1;
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
        if (needPermissions.size() > 0) {
            requestPermissions(needPermissions.toArray(new String[0]), REQUEST_PERMISSIONS_CODE);
        } else {
            initialize();
        }
        //createDebugFiles();
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