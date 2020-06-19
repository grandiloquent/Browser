package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.core.util.Pair;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet;
import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.browser.app.SampleDownloadActivity;
import euphoria.psycho.browser.app.ServerActivity;
import euphoria.psycho.browser.app.TwitterHelper;
import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.base.Share;

public class FileHelper {
    static ExecutorService sSingleThreadExecutor;

    public static final int TYPE_DIRECTORY = 1;
    public static final int TYPE_FILE_AUDIO = 2;
    public static final int TYPE_FILE_TEXT = 3;
    public static final int TYPE_FILE_APK = 4;
    public static final int TYPE_FILE_IMAGE = 5;

    public static final int TYPE_FILE_UNKNOWN = 0;
    public static VectorDrawableCompat sDirectoryDrawable;
    public static VectorDrawableCompat sAudioDrawable;
    public static VectorDrawableCompat sTextDrawable;
    public static VectorDrawableCompat sImageDrawable;

    public static VectorDrawableCompat sOthersDrawable;

    public static void initialize(Context context) {
        sDirectoryDrawable = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_folder,
                context.getTheme());
        sAudioDrawable = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_music,
                context.getTheme());
        sTextDrawable = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_text,
                context.getTheme());
        sOthersDrawable = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_others,
                context.getTheme());
     sImageDrawable  = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_image,
                context.getTheme());
    }

    public static Pair[] createBottomSheetItems(Context context) {
        return new Pair[]{
                Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                Pair.create(R.drawable.ic_sd_storage, context.getString(R.string.sd_storage)),
                Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.video_server)),
                Pair.create(R.drawable.ic_film, context.getString(R.string.video_server)),
                Pair.create(R.drawable.ic_twitter, context.getString(R.string.twitter)),
                Pair.create(R.drawable.ic_youtube, context.getString(R.string.youtube)),
                Pair.create(R.drawable.ic_translate, context.getString(R.string.youdao)),
                Pair.create(R.drawable.ic_g_translate, context.getString(R.string.google)),
        };
    }

    public static void downloadFromUrl(Context context, String youtubeDlUrl, String downloadTitle, String fileName) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public static void extractTwitterVideo(Activity activity) {
        new Thread(() -> {
            try {
                CharSequence twitterUrl = Share.getClipboardString();
                if (twitterUrl != null) {
                    String id = Share.substringAfterLast(twitterUrl.toString(), "/");
                    if (Share.isDigits(id)) {
                        List<TwitterVideo> twitterVideos = TwitterHelper.extractTwitterVideo(id);
                        activity.runOnUiThread(() -> {
                            TwitterHelper.showDialog(twitterVideos, activity);
                        });
                    }
                }
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    Share.showExceptionDialog(activity, e);
                });
            }
        }).start();
    }

    public static void showBottomSheet(Activity activity, Pair[] items, FileManager fileManager) {
        BottomSheet bottomSheet = new BottomSheet(activity)
                .setOnClickListener(item -> {
                    switch (item.first) {
                        case R.drawable.ic_twitter:
                            extractTwitterVideo(activity);
                            break;
                        case R.drawable.ic_youtube:
                            startYouTube(activity);
                            break;
                        case R.drawable.ic_film:
                            startVideoServer(activity);
                            break;
                        case R.drawable.ic_translate:
                            youdaoChinese(activity);
                            break;
                        case R.drawable.ic_g_translate:
                            google(activity);
                            break;
                    }
                    fileManager.setBottomSheet(null);
                });
        fileManager.setBottomSheet(bottomSheet);
        bottomSheet.showDialog(items);

    }

    public static void startVideoServer(Activity activity) {
        Intent intent = new Intent(activity, ServerActivity.class);
        activity.startActivity(intent);
    }

    public static void startYouTube(Activity activity) {
        Intent intent = new Intent(activity, SampleDownloadActivity.class);
        activity.startActivity(intent);
    }

    private static void google(Activity activity) {
        if (sSingleThreadExecutor == null)
            sSingleThreadExecutor = Executors.newSingleThreadExecutor();

        sSingleThreadExecutor.submit(() -> {
            CharSequence q = Share.getClipboardString();
            if (q == null) return;
            String query = q.toString().trim();
            String result = NativeHelper.google(query, false);
            activity.runOnUiThread(() -> {
                new AlertDialog.Builder(activity)
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            Share.setClipboardString(result);
                            dialog.dismiss();
                        })
                        .show();
            });
        });
    }

    private static void youdaoChinese(Activity activity) {
        if (sSingleThreadExecutor == null)
            sSingleThreadExecutor = Executors.newSingleThreadExecutor();

        sSingleThreadExecutor.submit(() -> {
            CharSequence q = Share.getClipboardString();
            if (q == null) return;
            String query = q.toString().trim();
            String result = NativeHelper.youdao(query, true, query.contains(" "));
            activity.runOnUiThread(() -> {
                new AlertDialog.Builder(activity)
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            Share.setClipboardString(result);
                            dialog.dismiss();
                        })
                        .show();
            });
        });
    }

    public static int getFileType(File file) {
        if (file.isDirectory()) {
            return TYPE_DIRECTORY;
        }
        String extension = Share.substringAfterLast(file.getName(), '.');
        if (extension.equals("mp3")) {

            return TYPE_FILE_AUDIO;
        }
        if (extension.equals("txt") || extension.equals("json")
                || extension.equals("html"))
            return TYPE_FILE_TEXT;
        if (extension.equals("jpg") || extension.equals("png"))
            return TYPE_FILE_IMAGE;

        return TYPE_FILE_UNKNOWN;
    }
}
