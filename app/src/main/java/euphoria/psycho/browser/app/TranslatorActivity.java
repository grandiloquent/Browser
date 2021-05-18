package euphoria.psycho.browser.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import euphoria.psycho.browser.R;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.share.DialogUtils;
import euphoria.psycho.share.Log;
import euphoria.psycho.share.StringUtils;
import euphoria.psycho.share.ThreadUtils;

public class TranslatorActivity extends Activity {
    EditText mEditText;
    TextView mTextView;
    Button mButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);
        mEditText = findViewById(R.id.editText);
        mTextView = findViewById(R.id.textView);
        mButton=findViewById(R.id.submit);
        mButton.setOnClickListener(v -> {
            Activity activity = TranslatorActivity.this;
            String value = mEditText.getText().toString();
            ThreadUtils.postOnBackgroundThread(() -> {
                String[] lines = value.split("\\.");
                StringBuilder stringBuilder = new StringBuilder();
                for (String l : lines) {
                    Log.e("TAG/", "[TranslatorActivity]: onCreate"+l);
                    String result = NativeHelper.google(l + ".", true);
                    stringBuilder
                            .append(l)
                            .append('\n')
                            .append('\n')
                            .append(result)
                            .append('\n')
                            .append('\n');
                }
                activity.runOnUiThread(() -> {
                    mTextView.setText(stringBuilder.toString());
                    mEditText.setText("");
                });
            });
        });
        mTextView.setOnClickListener(v -> {
            ClipboardManager clipboardManager = getSystemService(ClipboardManager.class);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, mTextView.getText().toString()));
        });
//        mEditText.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
//
//                }
//                return false;
//            }
//        });
    }
}
