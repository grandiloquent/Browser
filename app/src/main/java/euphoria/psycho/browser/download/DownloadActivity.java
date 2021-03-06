package euphoria.psycho.browser.download;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import euphoria.psycho.browser.music.MusicPlaybackService;

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
        Intent servcie = new Intent(this, MusicPlaybackService.class);
        startService(servcie);
    }
}
