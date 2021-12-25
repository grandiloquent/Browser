package euphoria.psycho.browser.file;

import android.Manifest.permission;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
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

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.FloatingService;
import euphoria.psycho.browser.app.InputService;
import euphoria.psycho.browser.app.InputServiceHelper;
import euphoria.psycho.browser.app.LocalFileService;
import euphoria.psycho.browser.app.WebActivity;
import euphoria.psycho.share.ContextUtils;
import euphoria.share.FileShare;
import euphoria.share.Logger;
import euphoria.share.PreferenceShare;
// ImageActivity
public class FileActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_CODE = 1 << 1;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private FileManager mFileManager;

    private void initialize() {
        setContentView(R.layout.activity_file);
        FrameLayout container = findViewById(R.id.container);
        mFileManager = new FileManager(this);
        container.addView(mFileManager.getView(), 0);
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
//        if (!ContextUtils.checkSelfPermission(this, "android.permission.ACCESS_MOCK_LOCATION")) {
//            needPermissions.add("android.permission.ACCESS_MOCK_LOCATION");
//        }
        // https://developer.android.com/training/data-storage/manage-all-files
        if (VERSION.SDK_INT >= VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
        checkStartPermissionRequest();
        if (needPermissions.size() > 0) {
            requestPermissions(needPermissions.toArray(new String[0]), REQUEST_PERMISSIONS_CODE);
        } else {
            initialize();
        }
        PreferenceShare.initialize(this);
        if (PreferenceShare.getPreferences().getString(FileShare.KEY_TREE_URI, null) == null) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, 101);
        }
//        Location fake_location_gps = new Location(LocationManager.GPS_PROVIDER);
//        Location fake_location_network = new Location(LocationManager.NETWORK_PROVIDER);
//        fake_location_gps.setLatitude(127.15);
//        fake_location_gps.setLongitude(37.24);
//        fake_location_gps.setAccuracy((float) 0.001);
//        fake_location_gps.setAltitude(0);
//        fake_location_gps.setSpeed(0);
//        long system_time_now = System.currentTimeMillis();
//        fake_location_gps.setTime(system_time_now);
//        fake_location_gps.setElapsedRealtimeNanos(System.nanoTime());
//        fake_location_network.setLatitude(0);
//        fake_location_network.setLongitude(0);
//        fake_location_network.setAccuracy((float) 0.001);
//        fake_location_network.setAltitude(0);
//        fake_location_network.setSpeed(0);
//        long system_time_now2 = System.currentTimeMillis();
//        fake_location_network.setTime(system_time_now2);
//        fake_location_network.setElapsedRealtimeNanos(System.nanoTime());
//        LocationManager location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        location_manager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, true, true, true, 1, 1);
//        location_manager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
//        location_manager.setTestProviderLocation(LocationManager.GPS_PROVIDER, fake_location_gps);
//        location_manager.addTestProvider(LocationManager.NETWORK_PROVIDER, false, false, false, false, true, true, true, 1, 1);
//        location_manager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
//        location_manager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, fake_location_network);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            getContentResolver().takePersistableUriPermission(
                    data.getData(),
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );
            Logger.d(String.format("onActivityResult: %s", data.getData().toString()));
            PreferenceShare.getPreferences()
                    .edit().
                    putString(FileShare.KEY_TREE_URI,
                            data.getData().toString()).apply();

        }
    }
}