package euphoria.psycho.browser.app;

import android.Manifest;
import android.Manifest.permission;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.Share;

public class DownloadActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

//        requestPermissions(new String[]{
//                permission.INTERNET,
//                permission.ACCESS_NETWORK_STATE
//        }, 0);

        Log.e("TAG/", "Debug: onCreate, \n" + Share.getDeviceIP(this));

        NativeHelper.startServer(Share.getDeviceIP(this), "12345");

    }
}
