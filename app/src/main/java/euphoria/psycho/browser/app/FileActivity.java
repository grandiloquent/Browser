package euphoria.psycho.browser.app;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FileActivity extends AppCompatActivity {

    private FileManager mFileManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFileManager = new FileManager(this);
        setContentView(mFileManager.getView());
    }
}

