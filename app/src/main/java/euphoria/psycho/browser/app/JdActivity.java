package euphoria.psycho.browser.app;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.IOException;

import euphoria.psycho.share.Log;

public class JdActivity extends Activity {

    private static final int MENU_JD = 1;
    private static final String URL_JD = "https://m.jd.com";
    WebView mWebView;
    EditText mEditText;

    private int mMode = 0;


    private void evaluateJavascript() {

        String javaScript = "(function() {\n" +
                "    var Jd = function Jd() {};\n" +
                "    Jd.prototype.initialize = function() {\n" +
                "        //this.hideItems();\n" +
                "        var that = this;\n" +
                "        window.addEventListener('scroll', function(event) {\n" +
                "            that.hideItems();\n" +
                "        });\n" +
                "    }\n" +
                "    Jd.prototype.hideItems = function() {\n" +
                "\n" +
                "        var elements = document.querySelectorAll('.search_interlude');\n" +
                "        for (var i = elements.length - 1; i >= 0; i--) {\n" +
                "            var element = elements[i];\n" +
                "            if (element) {\n" +
                "                element.style.display = 'none';\n" +
                "            }\n" +
                "        };\n" +
                "\n" +
                "        var items = document.querySelectorAll('.search_prolist_item');\n" +
                "\n" +
                "        for (var i = items.length - 1; i >= 0; i--) {\n" +
                "            var item = items[i];\n" +
                "            var element = item.querySelector('.search_prolist_other .mod_tag img');\n" +
                "            if (!element) {\n" +
                "                item.parentNode.removeChild(item);\n" +
                "            } else {\n" +
                "                var src = element.getAttribute('src');\n" +
                "                if (!src || !src.endsWith('c5ab4d78f8bf4d90.png')) {\n" +
                "                    item.style.display = \"none\";\n" +
                "                    item.parentNode.removeChild(item);\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "        }\n" +
                "    };\n" +
                "\n" +
                "    var jd = new Jd();\n" +
                "    jd.initialize();\n" +
                "})();";

        //Log.e(TAG, "Debug: evaluateJavascript, " + javaScript);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.evaluateJavascript(javaScript, value -> {
                //Toast.makeText(Browsers.this, value, Toast.LENGTH_SHORT).show();
            });
        }

    }

    private void initialize() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        EditText editText = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(editText, layoutParams);
        editText.setMaxLines(1);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    switch (mMode) {
                        case 0:
                            mWebView.loadUrl(String.format("https://so.m.jd.com/ware/search.action?keyword=%s&filt_type=col_type,L0M0;redisstore,1;&sort_type=sort_dredisprice_asc&sf=11&as=1&qp_disable=no", mEditText.getText().toString()));
                            return true;
                    }
                    return true;
                }
                return false;
            }
        });
        ;

        mEditText = editText;
        mWebView = setupWebView(linearLayout);

        setContentView(linearLayout, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void menuJd() {
        mWebView.loadUrl(URL_JD);
    }

    private WebView setupWebView(LinearLayout linearLayout) {
        WebView webView = new WebView(this);


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        String appCachePath = getApplication().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);


        //Log.e(TAG, "Debug: setupWebView, " + appCachePath);

        webSettings.setDatabaseEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onLoadResource(WebView view, String url) {
//                super.onLoadResource(view, url);
//            }

            @Override
            public void onPageFinished(WebView view, String url) {
                evaluateJavascript();
            }


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                //Log.e(TAG, "Debug: shouldOverrideUrlLoading, " + url);

                if (url.startsWith("openapp.jdmobile:")) return false;
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {


                // Log.e(TAG, "Debug: onConsoleMessage, " + consoleMessage.message());

                return super.onConsoleMessage(consoleMessage);
            }
        });

        LinearLayout.LayoutParams webViewLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        webViewLayoutParams.weight = 1;
        linearLayout.addView(webView, webViewLayoutParams);

        return webView;
    }

    private static final String TAG = "TAG/" + JdActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialize();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_JD, 0, "京东");

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_JD:
                menuJd();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }
}

// "C:\Program Files\Android\Android Studio\jre\bin\java" -jar apktool.jar d base.apk
//