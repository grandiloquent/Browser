package euphoria.psycho.browser.file;

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
//
//        new Thread(() -> {
//            String result = NativeHelper.youdao("good morning", true, true);
//
//
//            Log.e("TAG/", "Debug: onCreate, \n" + result);
//
//        })
//                .start();
    }

    @Override
    public void onBackPressed() {
        if (!mFileManager.onBackPressed())
            super.onBackPressed();
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
        mFileManager.onPause();
    }
}

