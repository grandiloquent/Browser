package euphoria.psycho.browser.download;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

public class DownloadActivity extends Activity {

    private Button mButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButton = new Button(this);
        mButton.setOnClickListener(view -> {
            Intent service = new Intent(DownloadActivity.this, DownloadService.class);
            service.setAction(DownloadService.ACTION_DOWNLOAD_ADD);
            service.putExtra(DownloadService.EXTRA_URI, "");
            startService(service);
        });
        setContentView(mButton);
        Intent servcie = new Intent(this, DownloadService.class);
        startService(servcie);
    }
}
