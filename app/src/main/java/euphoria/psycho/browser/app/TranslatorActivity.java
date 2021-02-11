package euphoria.psycho.browser.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import euphoria.psycho.browser.R;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.share.DialogUtils;
import euphoria.psycho.share.StringUtils;
import euphoria.psycho.share.ThreadUtils;

public class TranslatorActivity extends Activity {
    EditText mEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translator);
        mEditText=findViewById(R.id.editText);
        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    Activity activity=TranslatorActivity.this;
                    String value=mEditText.getText().toString();
                    ThreadUtils.postOnBackgroundThread(() -> {
                        String result = NativeHelper.youdao(value, true, value.contains(" "));
                        activity.runOnUiThread(() -> {
                            new AlertDialog.Builder(activity)
                                    .setMessage(result)
                                    .setPositiveButton(android.R.string.ok, (dlg, which) -> {
                                        ContextUtils.setClipboardString(result);
                                        dlg.dismiss();
                                    })
                                    .show();
                        });
                    });
                    return true;
                }
                return false;
            }
        });
    }
}
