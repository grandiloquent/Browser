package euphoria.psycho.browser.file;

import android.os.Bundle;
import android.util.Log;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.browser.app.NativeHelper;

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
}

