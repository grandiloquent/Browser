package euphoria.share;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;

import static android.content.Context.DOWNLOAD_SERVICE;

public class WebViewShare {
    public static void supportCookie(WebView webView) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptThirdPartyCookies(webView, true);
    }

    public static String getWebViewVersion() {
        PackageInfo webViewPackageInfo;
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            webViewPackageInfo = WebView.getCurrentWebViewPackage();
            return webViewPackageInfo.versionName;
        }
        return null;
    }

    public static WebResourceResponse tryBlockAds(String url) {
        if (url.startsWith("https://m.youtube.com/api/stats/ads")
                || url.startsWith("https://googleads.g.doubleclick.net")
                || url.startsWith("https://m.youtube.com/youtubei/v1/log_event")
                || url.startsWith("https://m.youtube.com/api/stats")
                || url.contains("googlesyndication.com")
                || url.startsWith("https://ad.doubleclick.net")
                || url.contains("/pagead/")
                || url.startsWith("https://static.doubleclick.net")
                || url.startsWith("https://m.youtube.com/ptracking")
                || url.startsWith("https://tpc.googlesyndication.com")
                || url.contains("googleusercontent.com")
                || url.contains("/pcs/")
                || url.startsWith("https://yt3.ggpht.com")
                || url.startsWith("https://www.google.com/js/th")
                || url.endsWith("/ad.js")
                || url.endsWith("/scheduler.js")
                || url.endsWith("/base.js")
                || url.endsWith("fetch_polyfill.js")
                || url.contains("player-plasma-ias-phone")
                || url.contains("/youtubei/")
                || url.startsWith("https://m.youtube.com/s/_/ytmweb")
                || url.startsWith("https://m.youtube.com/youtubei/v1/guide")
                || url.contains("/generate_")
                || url.contains("/videogoodput")
                || url.contains("&aitags=")
        )
            return new WebResourceResponse(
                    "text/plain",
                    "UTF-8",
                    new ByteArrayInputStream("".getBytes())
            );
        return null;
    }

    public static String getFileType(Context context, String url) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(Uri.parse(url)));
    }
    public static void setWebView(WebView webView, String appCachePath) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setAppCacheEnabled(true);
        settings.setAppCachePath(appCachePath);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

    }
    public static void downloadFile(Context context, String fileName, String url, String userAgent) {
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            String cookie = CookieManager.getInstance().getCookie(url);
            request.allowScanningByMediaScanner();
            request.setTitle(fileName)
                    .setDescription("正在下载")
                    .addRequestHeader("cookie", cookie)
                    .addRequestHeader("User-Agent", userAgent)
                    .setMimeType(getFileType(context, url))
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            downloadManager.enqueue(request);
            Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            Toast.makeText(context, "下载错误", Toast.LENGTH_SHORT).show();


        }
    }
}
