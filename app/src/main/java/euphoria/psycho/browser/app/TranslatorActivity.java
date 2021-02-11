package euphoria.psycho.browser.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import euphoria.psycho.browser.R;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.share.DialogUtils;
import euphoria.psycho.share.StringUtils;
import euphoria.psycho.share.ThreadUtils;

public class TranslatorActivity extends Activity {
    EditText mEditText;
    TextView mTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);
        mEditText=findViewById(R.id.editText);
        mTextView=findViewById(R.id.textView);
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    Activity activity=TranslatorActivity.this;
                    String value=mEditText.getText().toString();
                    ThreadUtils.postOnBackgroundThread(() -> {
                        String result = NativeHelper.youdao(value, true, value.contains(" "));
                        activity.runOnUiThread(() -> {
                            mTextView.setText(result);
                            mEditText.setText("");
                        });
                    });
                    return true;
                }
                return false;
            }
        });
    }
}
