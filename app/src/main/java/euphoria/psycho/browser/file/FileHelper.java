package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.core.util.Pair;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet;
import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.browser.app.SampleDownloadActivity;
import euphoria.psycho.browser.app.ServerActivity;
import euphoria.psycho.browser.app.SettingsActivity;
import euphoria.psycho.browser.app.TwitterHelper;
import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.base.Share;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FileHelper {
    public static final int TYPE_APK = 0;
    public static final int TYPE_EXCEL = 1;
    public static final int TYPE_FOLDER = 2;
    public static final int TYPE_IMAGE = 3;
    public static final int TYPE_MUSIC = 4;
    public static final int TYPE_OTHERS = 5;
    public static final int TYPE_PDF = 6;
    public static final int TYPE_PPS = 7;
    public static final int TYPE_TEXT = 8;
    public static final int TYPE_VCF = 9;
    public static final int TYPE_VIDEO = 10;
    public static final int TYPE_WORD = 11;
    public static final int TYPE_ZIP = 12;


    /*
    ["apk",
"excel",
"folder",
"image",
"music",
"others",
"pdf",
"pps",
"text",
"vcf",
"video",
"word",
"zip"]
    * */

    static ExecutorService sSingleThreadExecutor;

    public static Pair[] createBottomSheetItems(Context context) {
        return new Pair[]{
                Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                Pair.create(R.drawable.ic_sd_storage, context.getString(R.string.sd_storage)),
                Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.create_new_folder)),
                Pair.create(R.drawable.ic_film, context.getString(R.string.video_server)),
                Pair.create(R.drawable.ic_twitter, context.getString(R.string.twitter)),
                Pair.create(R.drawable.ic_youtube, context.getString(R.string.youtube)),
                Pair.create(R.drawable.ic_translate, context.getString(R.string.youdao)),
                Pair.create(R.drawable.ic_g_translate, context.getString(R.string.google)),
                Pair.create(R.drawable.ic_settings, context.getString(R.string.settings)),
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

    public static long getFileSize(File file) {
        if (file.isDirectory()) {
            return file.listFiles().length;
        }
        return file.length();
    }

    public static int getFileType(File file) {
        if (file.isDirectory()) {
            return TYPE_FOLDER;
        }
        String extension = Share.substringAfterLast(file.getName(), '.');

        switch (extension) {
            case "apk":
                return TYPE_APK;
            case "excel":
                return TYPE_EXCEL;
            case "bmp":
            case "gif":
            case "jpg":
            case "png":
            case "webp":
            case "heic":
            case "heif":
            case "jpeg":
                return TYPE_IMAGE;
            case "m4a":
            case "aac":
            case "flac":
            case "gsm":
            case "mid":
            case "xmf":
            case "mxmf":
            case "rtttl":
            case "rtx":
            case "ota":
            case "imy":
            case "mp3":
            case "wav":
            case "ogg":
                return TYPE_MUSIC;
            case "pdf":
                return TYPE_PDF;
            case "pps":
                return TYPE_PPS;
            case "txt":
            case "html":
            case "sql":
            case "c":
            case "css":
            case "cs":
            case "cc":
            case "h":
            case "js":
                return TYPE_TEXT;
            case "vcf":
                return TYPE_VCF;
            case "3gp":
            case "mp4":
            case "webm":
            case "ts":
            case "mkv":
                return TYPE_VIDEO;
            case "word":
                return TYPE_WORD;
            case "zip":
                return TYPE_ZIP;
            default:
                return TYPE_OTHERS;
        }
    }

    public static void openUrl(Activity activity, FileItem fileItem) {
        String url = fileItem.getUrl();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(url)),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        Share.substringAfterLast(url, '.')
                ));
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.open)));
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
                        case R.drawable.ic_settings:

                            Intent settingsActivity = new Intent(activity, SettingsActivity.class);
                            activity.startActivity(settingsActivity);
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
}
