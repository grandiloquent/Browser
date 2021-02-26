package euphoria.psycho.browser.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import androidx.annotation.Nullable;

import euphoria.psycho.browser.R;
import euphoria.psycho.share.Log;

public class WebActivity extends Activity {
    WebView mWebView;
    EditText mEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebView = findViewById(R.id.web);
        mEditText = findViewById(R.id.edit);

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
                Log.e("TAG/", "[onLoadResource]: " + url);
                if (url.contains(".m3u8")) {
                    getSystemService(ClipboardManager.class)
                            .setPrimaryClip(ClipData.newPlainText(null, url));
                }
            }
        });


        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    mWebView.loadUrl(mEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });

    }
}
